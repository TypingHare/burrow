package burrow.furniture.hoard.chain;

import burrow.chain.Chain;
import burrow.furniture.hoard.Entry;
import org.jetbrains.annotations.NotNull;

public class DeleteEntryChain extends Chain<DeleteEntryContext> {
    @NotNull
    public DeleteEntryContext apply(@NotNull final Entry entry) {
        final var context = new DeleteEntryContext();
        context.setEntry(entry);

        return apply(context);
    }
}
