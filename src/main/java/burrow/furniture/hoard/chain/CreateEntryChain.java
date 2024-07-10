package burrow.furniture.hoard.chain;

import burrow.chain.Chain;
import burrow.furniture.hoard.Entry;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class CreateEntryChain extends Chain<CreateEntryContext> {
    @NotNull
    public CreateEntryContext apply(
        @NotNull final Entry entry,
        @NotNull final Map<String, String> properties
    ) {
        final var context = new CreateEntryContext();
        CreateEntryContext.Hook.entry.set(context, entry);
        CreateEntryContext.Hook.properties.set(context, properties);

        return apply(context);
    }
}
