package burrow.furniture.hoard.chain;

import burrow.chain.Chain;
import burrow.furniture.hoard.Entry;
import org.springframework.lang.NonNull;

import java.util.Map;

public class SetEntryChain extends Chain<SetEntryContext> {
    @NonNull
    public SetEntryContext apply(
        @NonNull final Entry entry,
        @NonNull final Map<String, String> properties
    ) {
        final var context = new SetEntryContext();
        context.setEntry(entry);
        context.setProperties(properties);

        return apply(context);
    }
}
