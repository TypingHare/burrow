package burrow.core.chamber;

import burrow.chain.Context;
import burrow.chain.ContextHook;
import burrow.core.command.Processor;
import burrow.core.config.Config;
import burrow.core.furniture.Renovator;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public final class ChamberContext extends Context {
    @NotNull
    public Chamber getChamber() {
        return Hook.chamber.getNotNull(this);
    }

    public void setChamber(@NotNull final Chamber chamber) {
        Hook.chamber.set(this, chamber);
    }

    @NotNull
    public Path getRootPath() {
        return Hook.rootPath.getNotNull(this);
    }

    public void setRootPath(@NotNull final Path rootPath) {
        Hook.rootPath.set(this, rootPath);
    }

    @NotNull
    public Path getConfigPath() {
        return Hook.configPath.getNotNull(this);
    }

    public void setConfigPath(@NotNull final Path configPath) {
        Hook.configPath.set(this, configPath);
    }

    @NotNull
    public Config getConfig() {
        return Hook.config.getNotNull(this);
    }

    public void setConfig(@NotNull final Config config) {
        Hook.config.set(this, config);
    }

    @NotNull
    public Renovator getRenovator() {
        return Hook.renovator.getNotNull(this);
    }

    public void setRenovator(@NotNull final Renovator renovator) {
        Hook.renovator.set(this, renovator);
    }

    @NotNull
    public Processor getProcessor() {
        return Hook.processor.getNotNull(this);
    }

    public void setProcessor(@NotNull final Processor processor) {
        Hook.processor.set(this, processor);
    }

    public @interface Hook {
        ContextHook<Chamber> chamber = hook("chamber");
        ContextHook<Path> rootPath = hook("rootPath");
        ContextHook<Path> configPath = hook("configPath");
        ContextHook<Config> config = hook("config");
        ContextHook<Renovator> renovator = hook("renovator");
        ContextHook<Processor> processor = hook("processor");
    }
}
