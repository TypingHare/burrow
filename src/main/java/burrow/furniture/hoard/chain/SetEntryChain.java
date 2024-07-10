package burrow.furniture.hoard.chain;

import burrow.chain.Chain;
import burrow.furniture.hoard.Entry;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class SetEntryChain extends Chain<SetEntryContext> {
    @NotNull
    public SetEntryContext apply(
        @NotNull final Entry entry,
        @NotNull final Map<String, String> properties
    ) {
        final var context = new SetEntryContext();
        context.setEntry(entry);
        context.setProperties(properties);

        return apply(context);
    }
}
