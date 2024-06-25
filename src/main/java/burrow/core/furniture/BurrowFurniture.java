package burrow.core.furniture;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BurrowFurniture {
    String simpleName() default "";

    String description() default "";

    Class<?>[] dependencies() default {};
}