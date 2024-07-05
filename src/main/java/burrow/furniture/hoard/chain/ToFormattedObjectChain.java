package burrow.furniture.hoard.chain;

import burrow.chain.Chain;
import burrow.chain.Context;
import burrow.furniture.hoard.Entry;
import org.springframework.lang.NonNull;

import java.util.HashMap;

public class ToFormattedObjectChain extends Chain<ToFormattedObjectContext> {
    @NonNull
    public Context apply(@NonNull final Entry entry) {
        final var context = new ToFormattedObjectContext();
        ToFormattedObjectContext.Hook.entry.set(context, entry);
        CreateEntryContext.Hook.formattedObject.set(context, new HashMap<>());

        return apply(context);
    }
}
