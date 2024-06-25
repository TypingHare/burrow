package burrow.core.chain;

import burrow.chain.Context;
import burrow.chain.Hook;
import burrow.chain.IdentityChain;
import burrow.core.entry.Entry;

import java.util.Map;

public final class CreateEntryChain extends IdentityChain<Context> {
    public static final Hook<Entry> entryHook = Hook.of(ContextKey.ENTRY, Entry.class);
    public static final Hook<Map<String, String>> entryObjectHook = Hook.of(ContextKey.ENTRY_OBJECT);

    public static final class ContextKey {
        public static final String ENTRY = "ENTRY";
        public static final String ENTRY_OBJECT = "ENTRY_OBJECT";
    }
}
