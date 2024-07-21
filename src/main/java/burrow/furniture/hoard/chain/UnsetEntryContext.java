package burrow.furniture.hoard.chain;

import burrow.chain.Context;
import burrow.chain.ContextHook;
import burrow.furniture.hoard.Entry;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class UnsetEntryContext extends Context {
    @NotNull
    public Entry getEntry() {
        return Hook.entry.getNotNull(this);
    }

    public void setEntry(@NotNull final Entry entry) {
        Hook.entry.set(this, entry);
    }

    @NotNull
    public Collection<String> getKeys() {
        return Hook.keys.getNotNull(this);
    }

    public void setKeys(@NotNull final Collection<String> keys) {
        Hook.keys.set(this, keys);
    }

    public @interface Hook {
        ContextHook<Entry> entry = hook("entry");
        ContextHook<Collection<String>> keys = hook("keys");
    }
}
