package burrow.furniture.hoard.chain;

import burrow.chain.Context;
import burrow.chain.ContextHook;
import burrow.furniture.hoard.Entry;
import org.springframework.lang.NonNull;

import java.util.Map;

public class SetEntryContext extends Context {
    @NonNull
    public Entry getEntry() {
        return Hook.entry.getNonNull(this);
    }

    public void setEntry(@NonNull final Entry entry) {
        Hook.entry.set(this, entry);
    }

    @NonNull
    public Map<String, String> getProperties() {
        return Hook.properties.getNonNull(this);
    }

    public void setProperties(@NonNull final Map<String, String> properties) {
        Hook.properties.set(this, properties);
    }

    public @interface Hook {
        ContextHook<Entry> entry = hook("entry");
        ContextHook<Map<String, String>> properties = hook("properties");
    }
}
