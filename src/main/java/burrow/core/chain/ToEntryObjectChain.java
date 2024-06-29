package burrow.core.chain;

import burrow.chain.Context;
import burrow.chain.Hook;
import burrow.chain.IdentityChain;
import burrow.core.entry.Entry;
import org.springframework.lang.NonNull;

import java.util.Map;

public final class ToEntryObjectChain extends IdentityChain<Context> {
    public static Hook<Entry> entryHook = Hook.of(UpdateEntryChain.ContextKey.ENTRY, Entry.class);
    public static final Hook<Map<String, String>> entryObjectHook =
        Hook.of(UpdateEntryChain.ContextKey.PROPERTIES);

    @NonNull
    public Context createContext(
        @NonNull final Entry entry,
        @NonNull final Map<String, String> entryObject
    ) {
        final var context = new Context();
        entryHook.set(context, entry);
        entryObjectHook.set(context, entryObject);

        return context;
    }

    public static final class ContextKey {
        public static final String ENTRY = "ENTRY";
        public static final String ENTRY_OBJECT = "ENTRY_OBJECT";
    }
}
