package me.jameschan.burrow.furniture.annotation;

import org.springframework.stereotype.Indexed;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BurrowFurniture {
  Class<?>[] dependencies() default {};
}
