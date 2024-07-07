package burrow.furniture.hoard.chain;

import burrow.chain.Chain;
import burrow.core.common.Environment;
import org.springframework.lang.NonNull;

import java.util.Map;

public class FormattedObjectToStringChain extends Chain<FormattedObjectToStringContext> {
    public FormattedObjectToStringContext apply(
        final int id,
        @NonNull final Map<String, String> formattedObject,
        @NonNull final Environment environment
    ) {
        final var context = new FormattedObjectToStringContext();
        context.setId(id);
        context.setFormattedObject(formattedObject);
        context.setEnvironment(environment);

        return apply(context);
    }
}
