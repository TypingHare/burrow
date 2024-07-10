package burrow.furniture.hoard.chain;

import burrow.chain.Chain;
import burrow.furniture.hoard.Entry;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class ToFormattedObjectChain extends Chain<ToFormattedObjectContext> {
    @NotNull
    public ToFormattedObjectContext apply(@NotNull final Entry entry) {
        final var context = new ToFormattedObjectContext();
        context.setEntry(entry);
        context.setFormattedObject(new HashMap<>());

        return apply(context);
    }
}
