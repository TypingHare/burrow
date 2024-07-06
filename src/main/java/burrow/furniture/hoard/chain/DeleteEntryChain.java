package burrow.furniture.hoard.chain;

import burrow.chain.Chain;
import burrow.furniture.hoard.Entry;
import org.springframework.lang.NonNull;

public class DeleteEntryChain extends Chain<DeleteEntryContext> {
    @NonNull
    public DeleteEntryContext apply(@NonNull final Entry entry) {
        final var context = new DeleteEntryContext();
        DeleteEntryContext.Hook.entry.set(context, entry);

        return apply(context);
    }
}