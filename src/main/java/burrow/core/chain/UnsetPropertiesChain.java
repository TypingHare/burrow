package burrow.core.chain;

import burrow.chain.Context;
import burrow.chain.Hook;
import burrow.chain.IdentityChain;
import burrow.core.entry.Entry;
import org.springframework.lang.NonNull;

import java.util.Collection;

public final class UnsetPropertiesChain extends IdentityChain<Context> {
    public static final Hook<Entry> entryHook = Hook.of(ContextKey.ENTRY, Entry.class);
    public static final Hook<Collection<String>> keysHook = Hook.of(ContextKey.KEYS);

    @NonNull
    public Context createContext(
        @NonNull final Entry entry,
        @NonNull final Collection<String> keys
    ) {
        final var context = new Context();
        entryHook.set(context, entry);
        keysHook.set(context, keys);

        return context;
    }

    public static final class ContextKey {
        public static final String ENTRY = "ENTRY";
        public static final String KEYS = "KEYS";
    }
}
