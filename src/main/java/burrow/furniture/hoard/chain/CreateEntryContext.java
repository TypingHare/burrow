package burrow.furniture.hoard.chain;

import burrow.chain.Context;
import burrow.chain.ContextHook;
import burrow.furniture.hoard.Entry;

import java.util.Map;

public class CreateEntryContext extends Context {
    public @interface Hook {
        ContextHook<Entry> entry = hook("entry");
        ContextHook<Map<String, String>> formattedObject = hook("formattedObject");
    }
}
