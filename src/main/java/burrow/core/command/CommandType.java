package burrow.core.command;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CommandType {
    /**
     * The default command type if the command is not annotated by CommandType.
     */
    String OTHER = "Other";

    /**
     * Returns the type of the annotated command.
     * @return the type of the annotated command.
     */
    String value() default OTHER;
}