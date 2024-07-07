package burrow.furniture.hoard.chain;

import burrow.chain.Context;
import burrow.chain.ContextHook;
import burrow.furniture.hoard.Entry;
import org.springframework.lang.NonNull;

import java.util.Collection;

public class UnsetEntryContext extends Context {
    @NonNull
    public Entry getEntry() {
        return Hook.entry.getNonNull(this);
    }

    public void setEntry(@NonNull final Entry entry) {
        Hook.entry.set(this, entry);
    }

    @NonNull
    public Collection<String> getKeys() {
        return Hook.keys.getNonNull(this);
    }

    public void setKeys(@NonNull final Collection<String> keys) {
        Hook.keys.set(this, keys);
    }

    public @interface Hook {
        ContextHook<Entry> entry = hook("entry");
        ContextHook<Collection<String>> keys = hook("keys");
    }
}
