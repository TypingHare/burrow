package burrow.furniture.hoard.chain;

import burrow.chain.Chain;
import burrow.furniture.hoard.Entry;
import org.springframework.lang.NonNull;

import java.util.Map;

public class CreateEntryChain extends Chain<CreateEntryContext> {
    @NonNull
    public CreateEntryContext apply(
        @NonNull final Entry entry,
        @NonNull final Map<String, String> entryObject
    ) {
        final var context = new CreateEntryContext();
        CreateEntryContext.Hook.entry.set(context, entry);
        CreateEntryContext.Hook.formattedObject.set(context, entryObject);

        return apply(context);
    }
}
