package burrow.core;

import burrow.core.chamber.ChamberShepherd;
import burrow.core.furniture.FurnitureRegistrar;
import ch.qos.logback.classic.Level;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class Burrow {
    public static final Path ROOT_DIR = Path.of("/opt/burrow/");
    public static final Path CHAMBERS_ROOT_DIR = ROOT_DIR.resolve("chambers");
    public static final Path LOGS_ROOT_DIR = ROOT_DIR.resolve("logs");
    public static final Path BIN_ROOT = Burrow.ROOT_DIR.resolve("bin");

    public static final ClassLoader DEFAULT_CLASS_LOADER = Burrow.class.getClassLoader();
    public static final String DEFAULT_FURNITURE_PACKAGE = "burrow.furniture";
    private static final Logger logger = LoggerFactory.getLogger(Burrow.class);
    private final ChamberShepherd chamberShepherd;
    private final FurnitureRegistrar furnitureRegistrar;

    public Burrow() {
        changeLoggersLevel();

        final var start = Instant.now();

        try {
            initializeApp();

            chamberShepherd = new ChamberShepherd(this);
            furnitureRegistrar = new FurnitureRegistrar();
            furnitureRegistrar.scanPackage(DEFAULT_CLASS_LOADER, List.of(DEFAULT_FURNITURE_PACKAGE));

            // Initialize root chamber
            getChamberShepherd().startRootChamber();
        } catch (final Throwable ex) {
            logger.error("Failed to start Burrow", ex);
            throw new RuntimeException(ex);
        }

        final var durationMs = Duration.between(start, Instant.now()).toMillis();
        logger.info("Started Burrow in {} ms", durationMs);
    }

    /**
     * Change the level of some unimportant loggers.
     */
    private static void changeLoggersLevel() {
        final var reflectionsLogger =
            (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Reflections.class);
        reflectionsLogger.setLevel(Level.WARN);
    }

    private void initializeApp() throws IOException {
        // Check bin directory
        if (!BIN_ROOT.toFile().exists()) {
            Files.createDirectory(BIN_ROOT);

            final var permissions = PosixFilePermissions.fromString("rwxr--r--");
            Files.setPosixFilePermissions(BIN_ROOT, permissions);
        }
    }

    @NotNull
    public ChamberShepherd getChamberShepherd() {
        return chamberShepherd;
    }

    @NotNull
    public FurnitureRegistrar getFurnitureRegistrar() {
        return furnitureRegistrar;
    }

    @NotNull
    public void shutdown() {
        logger.info("Shutting down Burrow...");
        getChamberShepherd().terminateAll();

        logger.info("Successfully shut down Burrow.");
    }
}
