package burrow.furniture.hoard.chain;

import burrow.chain.Chain;
import burrow.furniture.hoard.Entry;
import org.springframework.lang.NonNull;

import java.util.Map;

public class RegisterEntryChain extends Chain<RegisterEntryContext> {
    @NonNull
    public RegisterEntryContext apply(
        @NonNull final Entry entry,
        @NonNull final Map<String, String> entryObject
    ) {
        final var context = new RegisterEntryContext();
        context.setEntry(entry);
        context.setEntryObject(entryObject);

        return apply(context);
    }
}
