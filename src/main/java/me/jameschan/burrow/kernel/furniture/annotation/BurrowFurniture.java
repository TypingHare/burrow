package me.jameschan.burrow.kernel.furniture.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BurrowFurniture {
  Class<?>[] dependencies() default {};
}
