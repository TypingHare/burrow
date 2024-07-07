package burrow.furniture.hoard.chain;

import burrow.chain.Context;
import burrow.chain.ContextHook;
import burrow.furniture.hoard.Entry;
import org.springframework.lang.NonNull;

public class DeleteEntryContext extends Context {
    public @interface Hook {
        ContextHook<Entry> entry = hook("entry");
    }

    @NonNull
    public Entry getEntry() {
        return Hook.entry.getNonNull(this);
    }

    public void setEntry(@NonNull final Entry entry) {
        Hook.entry.set(this, entry);
    }
}
