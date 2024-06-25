package burrow.core.chamber;

import burrow.chain.Context;
import burrow.core.Overseer;
import burrow.core.command.Processor;
import burrow.core.config.Config;
import burrow.core.entry.Hoard;
import burrow.core.furniture.Renovator;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ChamberContext extends Context {
    @NonNull
    public Chamber getChamber() {
        return Objects.requireNonNull(get(Key.CHAMBER, Chamber.class));
    }

    @NonNull
    public String getChamberName() {
        return Objects.requireNonNull(get(Key.CHAMBER_NAME, String.class));
    }

    @NonNull
    public Config getConfig() {
        return Objects.requireNonNull(get(Key.CONFIG, Config.class));
    }

    @NonNull
    public Overseer getOverseer() {
        return Objects.requireNonNull(get(Key.OVERSEER, Overseer.class));
    }

    @NonNull
    public Hoard getHoard() {
        return Objects.requireNonNull(get(Key.HOARD, Hoard.class));
    }

    @NonNull
    public Renovator getRenovator() {
        return Objects.requireNonNull(get(Key.RENOVATOR, Renovator.class));
    }

    @NonNull
    public Processor getProcessor() {
        return Objects.requireNonNull(get(Key.PROCESSOR, Processor.class));
    }

    @NonNull
    public Path getRootDir() {
        return Objects.requireNonNull(get(Key.ROOT_DIR, Path.class));
    }

    @NonNull
    public File getConfigFile() {
        return Objects.requireNonNull(get(Key.CONFIG_FILE, File.class));
    }

    @NonNull
    public File getHoardFile() {
        return Objects.requireNonNull(get(Key.HOARD_FILE, File.class));
    }

    /**
     * The builtin keys to chamber context. Refer to the context table in documentation for more
     * information.
     */
    public static final class Key {
        // Chamber object
        public static final String CHAMBER = "CHAMBER";

        // Chamber name
        public static final String CHAMBER_NAME = "CHAMBER_NAME";

        // Objects of builtin modules
        public static final String CONFIG = "CONFIG";
        public static final String OVERSEER = "OVERSEER";
        public static final String HOARD = "HOARD";
        public static final String RENOVATOR = "RENOVATOR";
        public static final String PROCESSOR = "PROCESSOR";

        // Root directories and crucial file paths
        public static final String ROOT_DIR = "ROOT_DIR";
        public static final String CONFIG_FILE = "CONFIG_FILE";
        public static final String HOARD_FILE = "HOARD_FILE";
    }
}
