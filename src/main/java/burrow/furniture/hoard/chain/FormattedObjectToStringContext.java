package burrow.furniture.hoard.chain;

import burrow.chain.Context;
import burrow.chain.ContextHook;
import burrow.core.common.Environment;

import java.util.Map;

public class FormattedObjectToStringContext extends Context {
    public @interface Hook {
        ContextHook<Integer> id = hook("id");
        ContextHook<Map<String, String>> formattedObject = hook("formattedObject");
        ContextHook<Environment> environment = hook("environment");
        ContextHook<String> result = hook("result");
    }
}
