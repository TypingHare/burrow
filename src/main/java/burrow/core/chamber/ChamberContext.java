package burrow.core.chamber;

import burrow.chain.Context;
import burrow.chain.ContextHook;
import burrow.core.command.Processor;
import burrow.core.config.Config;
import burrow.core.furniture.Renovator;
import org.springframework.lang.NonNull;

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

    @NonNull
    public Chamber getChamber() {
        return Hook.chamber.getNonNull(this);
    }

    public void setChamber(@NonNull final Chamber chamber) {
        Hook.chamber.set(this, chamber);
    }

    @NonNull
    public Path getRootPath() {
        return Hook.rootPath.getNonNull(this);
    }

    public void setRootPath(@NonNull final Path rootPath) {
        Hook.rootPath.set(this, rootPath);
    }

    @NonNull
    public Path getConfigPath() {
        return Hook.configPath.getNonNull(this);
    }

    public void setConfigPath(@NonNull final Path configPath) {
        Hook.configPath.set(this, configPath);
    }

    @NonNull
    public Config getConfig() {
        return Hook.config.getNonNull(this);
    }

    public void setConfig(@NonNull final Config config) {
        Hook.config.set(this, config);
    }

    @NonNull
    public Renovator getRenovator() {
        return Hook.renovator.getNonNull(this);
    }

    public void setRenovator(@NonNull final Renovator renovator) {
        Hook.renovator.set(this, renovator);
    }

    @NonNull
    public Processor getProcessor() {
        return Hook.processor.getNonNull(this);
    }

    public void setProcessor(@NonNull final Processor processor) {
        Hook.processor.set(this, processor);
    }
}
