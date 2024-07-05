package burrow.core.chamber;

import burrow.chain.Context;
import burrow.chain.ContextHook;
import burrow.core.command.Processor;
import burrow.core.config.Config;
import burrow.core.furniture.Renovator;

import java.nio.file.Path;

public final class ChamberContext extends Context {
    public @interface Hook {
        ContextHook<Chamber> chamber = hook("chamber");
        ContextHook<Path> rootPath = hook("rootPath");
        ContextHook<Path> configPath = hook("configPath");
        ContextHook<Config> config = hook("config");
        ContextHook<Renovator> renovator = hook("renovator");
        ContextHook<Processor> processor = hook("processor");
    }
}
