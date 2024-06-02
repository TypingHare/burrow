package me.jameschan.burrow.kernel.furniture.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CommandType {
  String SPECIAL = "Special";
  String BUILTIN = "Builtin";
  String ENTRY = "Entry";
  String OTHER = "Other";

  /**
   * Returns the type of the annotated command.
   *
   * @return the type of the annotated command.
   */
  String value();
}
