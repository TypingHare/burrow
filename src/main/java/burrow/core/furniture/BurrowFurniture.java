package burrow.core.furniture;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BurrowFurniture {
    String simpleName();

    String description();

    String type();

    Class<? extends Furniture>[] dependencies() default {};

    /**
     * The type of the furniture.
     */
    @interface Type {
        // Indicates that this furniture is only for the root chamber
        String ROOT = "ROOT";

        // Indicates that this furniture serves as a main component for a chamber. Usually one
        // chamber is allowed to use one main furniture, or some conflicts may occur
        String MAIN = "MAIN";

        // Indicates that this furniture serves as a component. A chamber can use multiple pieces of
        // component furniture. However, some component furniture may conflict with one another
        String COMPONENT = "COMPONENT";
    }
}