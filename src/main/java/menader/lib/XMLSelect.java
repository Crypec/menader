package menader.lib;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface XMLSelect {
  public String xPath() default "";

  public int priority() default 0;
}
