package burrow.furniture.hoard.chain;

import burrow.chain.Context;
import burrow.chain.ContextHook;
import burrow.furniture.hoard.Entry;
import org.springframework.lang.NonNull;

import java.util.Map;

public class ToFormattedObjectContext extends Context {
    @NonNull
    public Entry getEntry() {
        return Hook.entry.getNonNull(this);
    }

    public void setEntry(@NonNull final Entry entry) {
        Hook.entry.set(this, entry);
    }

    @NonNull
    public Map<String, String> getFormattedObject() {
        return Hook.formattedObject.getNonNull(this);
    }

    public void setFormattedObject(@NonNull final Map<String, String> formattedObject) {
        Hook.formattedObject.set(this, formattedObject);
    }

    public @interface Hook {
        ContextHook<Entry> entry = hook("entry");
        ContextHook<Map<String, String>> formattedObject = hook("formattedObject");
    }
}
