package burrow.furniture.hoard.chain;

import burrow.chain.Chain;
import burrow.furniture.hoard.Entry;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class RegisterEntryChain extends Chain<RegisterEntryContext> {
    @NotNull
    public RegisterEntryContext apply(
        @NotNull final Entry entry,
        @NotNull final Map<String, String> entryObject
    ) {
        final var context = new RegisterEntryContext();
        context.setEntry(entry);
        context.setEntryObject(entryObject);

        return apply(context);
    }
}
