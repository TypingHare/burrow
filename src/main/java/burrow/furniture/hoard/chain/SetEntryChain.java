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
        SetEntryContext.Hook.entry.set(context, entry);
        SetEntryContext.Hook.properties.set(context, properties);

        return apply(context);
    }
}
