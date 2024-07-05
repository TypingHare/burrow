package burrow.core;

import burrow.core.chamber.ChamberShepherd;
import burrow.core.furniture.FurnitureRegistrar;
import burrow.core.furniture.InvalidFurnitureClassException;
import ch.qos.logback.classic.Level;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

public class Burrow {
    private static final Logger logger = LoggerFactory.getLogger(Burrow.class);

    public static final Path ROOT_DIR = Path.of("/opt/burrow/");
    public static final Path CHAMBERS_ROOT_DIR = ROOT_DIR.resolve("chambers");
    public static final ClassLoader DEFAULT_CLASS_LOADER = Burrow.class.getClassLoader();
    public static final String DEFAULT_FURNITURE_PACKAGE = "burrow.furniture";

    private final ChamberShepherd chamberShepherd;
    private final FurnitureRegistrar furnitureRegistrar;

    public Burrow() throws InvalidFurnitureClassException {
        changeLoggersLevel();

        final var start = Instant.now();

        chamberShepherd = new ChamberShepherd(this);
        furnitureRegistrar = new FurnitureRegistrar(this);
        furnitureRegistrar.addClassLoader(DEFAULT_CLASS_LOADER);
        furnitureRegistrar.addPackage(DEFAULT_FURNITURE_PACKAGE);
        furnitureRegistrar.scanPackages();

        final var durationMs = Duration.between(start, Instant.now()).toMillis();
        logger.info("Started Burrow in {} ms", durationMs);
    }

    @NonNull
    public ChamberShepherd getChamberShepherd() {
        return chamberShepherd;
    }

    @NonNull
    public FurnitureRegistrar getFurnitureRegistrar() {
        return furnitureRegistrar;
    }

    private static void changeLoggersLevel() {
        final var reflectionsLogger =
            (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Reflections.class);
        reflectionsLogger.setLevel(Level.WARN);
    }
}
