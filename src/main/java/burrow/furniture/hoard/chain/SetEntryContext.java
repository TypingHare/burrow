package burrow.furniture.hoard.chain;

import burrow.chain.Context;
import burrow.chain.ContextHook;
import burrow.furniture.hoard.Entry;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class SetEntryContext extends Context {
    @NotNull
    public Entry getEntry() {
        return Hook.entry.getNotNull(this);
    }

    public void setEntry(@NotNull final Entry entry) {
        Hook.entry.set(this, entry);
    }

    @NotNull
    public Map<String, String> getProperties() {
        return Hook.properties.getNotNull(this);
    }

    public void setProperties(@NotNull final Map<String, String> properties) {
        Hook.properties.set(this, properties);
    }

    public @interface Hook {
        ContextHook<Entry> entry = hook("entry");
        ContextHook<Map<String, String>> properties = hook("properties");
    }
}
