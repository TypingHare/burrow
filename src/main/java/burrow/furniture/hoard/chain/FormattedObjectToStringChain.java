package burrow.furniture.hoard.chain;

import burrow.chain.Chain;
import burrow.core.common.Environment;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class FormattedObjectToStringChain extends Chain<FormattedObjectToStringContext> {
    public FormattedObjectToStringContext apply(
        final int id,
        @NotNull final Map<String, String> formattedObject,
        @NotNull final Environment environment
    ) {
        final var context = new FormattedObjectToStringContext();
        context.setId(id);
        context.setFormattedObject(formattedObject);
        context.setEnvironment(environment);

        return apply(context);
    }
}
