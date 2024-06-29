package burrow.core.chain;

import burrow.chain.Context;
import burrow.chain.Hook;
import burrow.chain.IdentityChain;
import burrow.core.entry.Entry;
import org.springframework.lang.NonNull;

import java.util.Map;

public final class UpdateEntryChain extends IdentityChain<Context> {
    public static final Hook<Entry> entryHook = Hook.of(ContextKey.ENTRY, Entry.class);
    public static final Hook<Map<String, String>> propertiesHook = Hook.of(ContextKey.PROPERTIES);

    @NonNull
    public Context createContext(
        @NonNull final Entry entry,
        @NonNull final Map<String, String> properties
    ) {
        final var context = new Context();
        entryHook.set(context, entry);
        propertiesHook.set(context, properties);

        return context;
    }

    public static final class ContextKey {
        public static final String ENTRY = "ENTRY";
        public static final String PROPERTIES = "PROPERTIES";
    }
}
