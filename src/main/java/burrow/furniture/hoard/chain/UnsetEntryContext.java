package burrow.furniture.hoard.chain;

import burrow.chain.Context;
import burrow.chain.ContextHook;
import burrow.furniture.hoard.Entry;

import java.util.Collection;

public class UnsetEntryContext extends Context {
    public @interface Hook {
        ContextHook<Entry> entry = hook("entry");
        ContextHook<Collection<String>> keys = hook("keys");
    }
}
