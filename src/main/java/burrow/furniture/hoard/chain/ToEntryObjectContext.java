package burrow.furniture.hoard.chain;

import burrow.chain.Context;
import burrow.chain.ContextHook;
import burrow.furniture.hoard.Entry;
import org.springframework.lang.NonNull;

import java.util.Map;

public class ToEntryObjectContext extends Context {
    public @interface Hook {
        ContextHook<Entry> entry = hook("entry");
        ContextHook<Map<String, String>> entryObject = hook("entryObject");
    }

    @NonNull
    public Entry getEntry() {
        return Hook.entry.getNonNull(this);
    }

    public void setEntry(@NonNull final Entry entry) {
        Hook.entry.set(this, entry);
    }

    @NonNull
    public Map<String, String> getEntryObject() {
        return Hook.entryObject.getNonNull(this);
    }

    public void setEntryObject(@NonNull final Map<String, String> entryObject) {
        Hook.entryObject.set(this, entryObject);
    }
}
