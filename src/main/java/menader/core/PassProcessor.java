package menader.core;

import com.github.tomaslanger.chalk.Chalk;
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
import org.simmetrics.StringMetric;
import org.simmetrics.builders.StringMetricBuilder;
import org.simmetrics.metrics.DamerauLevenshtein;
import org.simmetrics.simplifiers.Simplifiers;

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
  private final List<String> xmlSelectors = new ArrayList<>();

  private final float WORD_SIMILARITY_TRESHOLD = 0.7f;

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
    if (this.isFinished) {
      return true;
    }

    this.isFinished = true;

    generateSafePasses(SafePass.class, env);
    generateUnsafePasses(UnsafePass.class, env);

    try {
      writePassMgrFile("Basilides");
      writeValidatorFile("Basilides");
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
          this.xmlSelectors.add(enclosedElem.getAnnotation(XMLSelect.class).xPath());
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
          this.xmlSelectors.add(enclosedElem.getAnnotation(XMLSelect.class).xPath());
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
    var passFile = processingEnv.getFiler().createSourceFile(passMgrImpl.name);
    try (var writer = passFile.openWriter()) {
      writer.write(src.toString());
    }
  }

  // NOTE(Simon): This would have been much cleaner if I could have used java 14. new rawStrings :c
  public void writeValidatorFile(String packageName) throws IOException {

    var cacheType = TypeVariableName.get("HashMap<String, String>");
    var buildValCache =
        MethodSpec.methodBuilder("buildValidationCache")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(Document.class, "doc")
            .addStatement(
                "$T<$T, $T> cache = new $T<$T, $T>()",
                HashMap.class,
                String.class,
                String.class,
                HashMap.class,
                String.class,
                String.class)
            .returns(cacheType);

    for (var xPath : this.xmlSelectors) {
      buildValCache.beginControlFlow("try");
      buildValCache.beginControlFlow("for (var node : doc.selectNodes($S))", xPath);
      buildValCache.addStatement("cache.put(node.getText(), $S)", xPath);
      buildValCache.endControlFlow();
      buildValCache.endControlFlow();
      buildValCache.beginControlFlow("catch ($T e)", Exception.class);
      buildValCache.endControlFlow();
    }
    buildValCache.addStatement("return cache");

    var treeWalk =
        MethodSpec.methodBuilder("treeWalk")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(cacheType, "cache")
            .addParameter(org.dom4j.Element.class, "elem")
            .addParameter(float.class, "wordSimilarityTreshold")
            .beginControlFlow("for ($T child : elem.elements())", org.dom4j.Element.class)
            .beginControlFlow("if (child.elements().size() == 0)")
            .beginControlFlow("for (var entry : cache.entrySet())")
            .addStatement("$T score = metric.compare(entry.getKey(), child.getText())", float.class)
            .beginControlFlow("if (score > wordSimilarityTreshold)")
            .addStatement(
                "$T topLevelMsg = $T.on($S).yellow().bold().toString() + $T.on($S).yellow().bold().toString() + $T.on($S + score + $S).red().bold().toString() + $T.on(child.getText()).red().bold().toString() + $S + $T.on(entry.getKey()).red().bold().toString()",
                String.class,
                Chalk.class,
                "[Warning]",
                Chalk.class,
                " Found potentially unremoved PII ",
                Chalk.class,
                "[",
                "]: ",
                Chalk.class,
                " == ",
                Chalk.class)
            .addStatement("$T.out.println(topLevelMsg)", System.class)
            .addStatement(
                "$T msg = $T.on($S).yellow().bold().toString() + $T.on($S).yellow().toString() + $T.on(entry.getValue()).magenta().toString()",
                String.class,
                Chalk.class,
                "[Warning]: ",
                Chalk.class,
                "This should have been removed by this xPath expr: ",
                Chalk.class)
            .addStatement("$T.out.println(msg)", System.class)
            .addStatement(
                "$T hint = $T.on($S).green().bold().toString() + $T.on($S).yellow().toString() + $T.on(child.getName()).magenta().toString()",
                String.class,
                Chalk.class,
                "[Hint]: ",
                Chalk.class,
                "The PII was found in a node with the name: ",
                Chalk.class)
            .addStatement("$T.out.println(hint)", System.class)
            .addStatement("$T.out.println()", System.class)
            .endControlFlow()
            .endControlFlow()
            .endControlFlow()
            .beginControlFlow("else")
            .addStatement("treeWalk(cache, child, wordSimilarityTreshold)")
            .endControlFlow()
            .endControlFlow();

    var validate =
        MethodSpec.methodBuilder("validate")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(Document.class, "doc")
            .addParameter(float.class, "wordSimilarityTreshold")
            .addStatement(
                "$T<$T, $T> cache = $L(doc)",
                HashMap.class,
                String.class,
                String.class,
                buildValCache.build().name)
            .addStatement("$T m = new $T()", Marshaller.class, Marshaller.class)
            .addStatement("PassManager.applyAll(doc, m)")
            .addStatement("treeWalk(cache, doc.getRootElement(), wordSimilarityTreshold)");

    var validateOverLoad =
        MethodSpec.methodBuilder("validate")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(Document.class, "doc")
            .addStatement("validate(doc, $Lf)", WORD_SIMILARITY_TRESHOLD);

    var metricField =
        FieldSpec.builder(StringMetric.class, "metric")
            .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
            .initializer(
                "$T.with(new $T()).simplify($T.toLowerCase($T.ENGLISH)).simplify($T.toLowerCase($T.GERMAN)).simplify($T.replaceNonWord()).build()",
                StringMetricBuilder.class,
                DamerauLevenshtein.class,
                Simplifiers.class,
                Locale.class,
                Simplifiers.class,
                Locale.class,
                Simplifiers.class);

    var passValidatorImpl =
        TypeSpec.classBuilder("PassValidator")
            .addMethod(validate.build())
            .addMethod(treeWalk.build())
            .addMethod(validateOverLoad.build())
            .addMethod(buildValCache.build())
            .addField(metricField.build())
            .build();

    var src = JavaFile.builder(packageName, passValidatorImpl).build();
    var passFile = processingEnv.getFiler().createSourceFile(passValidatorImpl.name);
    try (var writer = passFile.openWriter()) {
      writer.write(src.toString());
    }
  }
}
