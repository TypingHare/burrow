package burrow.furniture.hoard.chain;

import burrow.chain.Context;
import burrow.chain.ContextHook;
import burrow.furniture.hoard.Entry;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ToFormattedObjectContext extends Context {
    @NotNull
    public Entry getEntry() {
        return Hook.entry.getNonNull(this);
    }

    public void setEntry(@NotNull final Entry entry) {
        Hook.entry.set(this, entry);
    }

    @NotNull
    public Map<String, String> getFormattedObject() {
        return Hook.formattedObject.getNonNull(this);
    }

    public void setFormattedObject(@NotNull final Map<String, String> formattedObject) {
        Hook.formattedObject.set(this, formattedObject);
    }

    public @interface Hook {
        ContextHook<Entry> entry = hook("entry");
        ContextHook<Map<String, String>> formattedObject = hook("formattedObject");
    }
}
