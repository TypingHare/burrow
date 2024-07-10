package burrow.furniture.hoard.chain;

import burrow.chain.Context;
import burrow.chain.ContextHook;
import burrow.furniture.hoard.Entry;
import org.jetbrains.annotations.NotNull;

public class DeleteEntryContext extends Context {
    @NotNull
    public Entry getEntry() {
        return Hook.entry.getNonNull(this);
    }

    public void setEntry(@NotNull final Entry entry) {
        Hook.entry.set(this, entry);
    }

    public @interface Hook {
        ContextHook<Entry> entry = hook("entry");
    }
}
