package burrow.furniture.hoard.chain;

import burrow.chain.Context;
import burrow.chain.ContextHook;
import burrow.furniture.hoard.Entry;

public class DeleteEntryContext extends Context {
    public @interface Hook {
        ContextHook<Entry> entry = hook("entry");
    }
}
