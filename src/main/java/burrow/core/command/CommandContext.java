package burrow.core.command;

import burrow.chain.Context;
import burrow.chain.ContextHook;
import burrow.core.chamber.ChamberContext;
import burrow.core.common.Environment;

import java.util.List;

public final class CommandContext extends Context {
    public @interface Hook {
        ContextHook<ChamberContext> chamberContext = hook("chamberContext");
        ContextHook<String> commandName = hook("commandName");
        ContextHook<List<String>> commandArgs = hook("commandArgs");
        ContextHook<Environment> environment = hook("environment");
        ContextHook<Integer> exitCode = hook("exitCode");
        ContextHook<StringBuilder> buffer = hook("buffer");
    }
}
