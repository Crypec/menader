package menader.core;

import com.squareup.javapoet.*;
import java.io.*;
import java.lang.annotation.Annotation;
import java.util.*;
import javax.annotation.processing.*;
import javax.lang.model.*;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.tools.Diagnostic;
import menader.lib.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.dom4j.Document;

@SupportedAnnotationTypes({"menader.lib.SafePass", "menader.lib.UnsafePass"})
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class PassProcessor extends AbstractProcessor {

  // NOTE(Simon): this is a bit hacky, but the the java annotation processor may run multiple times
  // during one compilation phase
  // NOTE(Simon): but we don't want to generate the same passes multiple times
  private boolean isFinished = false;
  private int cursor = 0;
  private final List<String> safePassNames = new ArrayList<>();
  private final List<String> unsafePassNames = new ArrayList<>();

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {

    var elems = env.getElementsAnnotatedWith(SafePass.class);
    if (elems.isEmpty() || this.isFinished) {
      return true;
    }

    this.isFinished = true;

    generateSafePasses(SafePass.class, env);
    generateUnsafePasses(UnsafePass.class, env);

    try {
      writePassMgrFile("Basilides");
    } catch (IOException e) {
      e.printStackTrace();
    }
    return true;
  }

  public void writePassFile(
      List<Element> methods, TypeElement clazz, String packageName, String className)
      throws IOException {
    var varName = "a" + RandomStringUtils.random(15, true, true);
    var apply =
        MethodSpec.methodBuilder("apply")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(void.class)
            .addParameter(Document.class, "doc")
            .addParameter(Marshaller.class, "m")
            .addStatement(
                "$L $L = new $L()", clazz.getSimpleName(), varName, clazz.getSimpleName());

    methods.sort(
        (a, b) -> {
          var ap = a.getAnnotation(XMLSelect.class).priority();
          var bp = b.getAnnotation(XMLSelect.class).priority();
          return bp - ap;
        });

    for (var method : methods) {
      var annotation = method.getAnnotation(XMLSelect.class);
      apply
          .beginControlFlow("try")
          .beginControlFlow("for (var node : doc.selectNodes($S))", annotation.xPath())
          .addStatement("$L.$L(node, m)", varName, method.getSimpleName())
          .endControlFlow()
          .endControlFlow()
          .beginControlFlow("catch ($T e)", Exception.class)
          .endControlFlow();
    }

    var applyInterfaceSpec = TypeSpec.interfaceBuilder("menader.lib.Pass").build();
    var applyInterfaceName = ClassName.get("", applyInterfaceSpec.name);

    var passImpl =
        TypeSpec.classBuilder(className)
            .addSuperinterface(applyInterfaceName)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addMethod(apply.build())
            .build();

    var src = JavaFile.builder(packageName, passImpl).build();
    var passFile = processingEnv.getFiler().createSourceFile(className);
    try (var writer = passFile.openWriter()) {
      writer.write(src.toString());
    }
  }

  public void generateSafePasses(Class<? extends Annotation> annotation, RoundEnvironment env) {
    for (var elem : env.getElementsAnnotatedWith(annotation)) {
      this.cursor++;
      if (elem.getKind() != ElementKind.CLASS) {
        processingEnv
            .getMessager()
            .printMessage(
                Diagnostic.Kind.ERROR,
                "You can use this annotation only on classes not interfaces or enums!");
        return;
      }

      var methods = new ArrayList<Element>();
      var typeElem = (TypeElement) elem;
      for (var enclosedElem : typeElem.getEnclosedElements()) {
        if (enclosedElem.getAnnotation(XMLSelect.class) != null) {
          if (enclosedElem.getKind() != ElementKind.METHOD) {
            processingEnv
                .getMessager()
                .printMessage(
                    Diagnostic.Kind.ERROR, "The XMLPath annotation can only be used on methods");
          }
          var methodElem = (ExecutableElement) enclosedElem;
          if (methodElem.getReturnType().getKind() != TypeKind.VOID) {
            processingEnv
                .getMessager()
                .printMessage(
                    Diagnostic.Kind.ERROR,
                    "Methods with the XMLSelector annoation have to return void.");
          }
          methods.add(enclosedElem);
        }
      }

      String packageName =
          processingEnv.getElementUtils().getPackageOf(typeElem.getEnclosingElement()).toString();
      String className = String.format("%sImpl%d", typeElem.getSimpleName(), this.cursor);
      this.safePassNames.add(String.format("%s.%s", packageName, className));
      try {
        writePassFile(methods, typeElem, packageName, className);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public void generateUnsafePasses(Class<? extends Annotation> annotation, RoundEnvironment env) {
    for (var elem : env.getElementsAnnotatedWith(annotation)) {
      this.cursor++;
      if (elem.getKind() != ElementKind.CLASS) {
        processingEnv
            .getMessager()
            .printMessage(
                Diagnostic.Kind.ERROR,
                "You can use this annotation only on classes not interfaces or enums!");
        return;
      }

      var methods = new ArrayList<Element>();
      var typeElem = (TypeElement) elem;
      for (var enclosedElem : typeElem.getEnclosedElements()) {
        if (enclosedElem.getAnnotation(XMLSelect.class) != null) {
          if (enclosedElem.getKind() != ElementKind.METHOD) {
            processingEnv
                .getMessager()
                .printMessage(
                    Diagnostic.Kind.ERROR, "The XMLPath annotation can only be used on methods");
          }
          var methodElem = (ExecutableElement) enclosedElem;
          if (methodElem.getReturnType().getKind() != TypeKind.VOID) {
            processingEnv
                .getMessager()
                .printMessage(
                    Diagnostic.Kind.ERROR,
                    "Methods with the XMLSelector annoation have to return void.");
          }
          methods.add(enclosedElem);
        }
      }

      String packageName =
          processingEnv.getElementUtils().getPackageOf(typeElem.getEnclosingElement()).toString();
      String className = String.format("%sImpl%d", typeElem.getSimpleName(), this.cursor);
      this.unsafePassNames.add(String.format("%s.%s", packageName, className));

      try {
        writePassFile(methods, typeElem, packageName, className);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public void writePassMgrFile(String packageName) throws IOException {
    var applySafe =
        MethodSpec.methodBuilder("applySafe")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(void.class)
            .addParameter(Document.class, "doc")
            .addParameter(Marshaller.class, "m");

    for (var clazz : this.safePassNames) {
      applySafe.addStatement("$L.apply(doc, m)", clazz);
    }

    var applyUnsafe =
        MethodSpec.methodBuilder("applyUnsafe")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(void.class)
            .addParameter(Document.class, "doc")
            .addParameter(Marshaller.class, "m");

    for (var clazz : this.unsafePassNames) {
      applyUnsafe.addStatement("$L.apply(doc, m)", clazz);
    }

    var applyAll =
        MethodSpec.methodBuilder("applyAll")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(void.class)
            .addParameter(Document.class, "doc")
            .addParameter(Marshaller.class, "m")
            .addStatement("applySafe(doc, m)")
            .addStatement("applyUnsafe(doc, m)")
            .build();

    var passMgrImpl =
        TypeSpec.classBuilder("PassManager")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addMethod(applySafe.build())
            .addMethod(applyUnsafe.build())
            .addMethod(applyAll)
            .build();

    var src = JavaFile.builder(packageName, passMgrImpl).build();
    var passFile = processingEnv.getFiler().createSourceFile("PassManager");
    try (var writer = passFile.openWriter()) {
      writer.write(src.toString());
    }
  }
}
