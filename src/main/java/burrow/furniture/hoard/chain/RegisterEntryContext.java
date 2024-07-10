package burrow.furniture.hoard.chain;

import burrow.chain.Context;
import burrow.chain.ContextHook;
import burrow.furniture.hoard.Entry;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class RegisterEntryContext extends Context {
    @NotNull
    public Entry getEntry() {
        return Hook.entry.getNonNull(this);
    }

    public void setEntry(@NotNull final Entry entry) {
        Hook.entry.set(this, entry);
    }

    @NotNull
    public Map<String, String> getEntryObject() {
        return Hook.entryObject.getNonNull(this);
    }

    public void setEntryObject(@NotNull final Map<String, String> entryObject) {
        Hook.entryObject.set(this, entryObject);
    }

    public @interface Hook {
        ContextHook<Entry> entry = hook("entry");
        ContextHook<Map<String, String>> entryObject = hook("properties");
    }
}
