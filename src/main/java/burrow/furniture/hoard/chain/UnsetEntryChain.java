package burrow.furniture.hoard.chain;

import burrow.chain.Chain;
import burrow.furniture.hoard.Entry;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class UnsetEntryChain extends Chain<UnsetEntryContext> {
    public UnsetEntryContext apply(
        @NotNull final Entry entry,
        @NotNull final Collection<String> keys
    ) {
        final var context = new UnsetEntryContext();
        context.setEntry(entry);
        context.setKeys(keys);

        return apply(context);
    }
}
