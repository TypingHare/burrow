package me.jameschan.burrow.furniture.annotation;

public @interface BurrowFurniture {
  Class<?>[] dependencies() default {};
}
