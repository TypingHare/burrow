package burrow.furniture.hoard.chain;

import burrow.chain.Chain;
import burrow.furniture.hoard.Entry;
import org.springframework.lang.NonNull;

import java.util.HashMap;

public class ToFormattedObjectChain extends Chain<ToFormattedObjectContext> {
    @NonNull
    public ToFormattedObjectContext apply(@NonNull final Entry entry) {
        final var context = new ToFormattedObjectContext();
        context.setEntry(entry);
        context.setFormattedObject(new HashMap<>());

        return apply(context);
    }
}
