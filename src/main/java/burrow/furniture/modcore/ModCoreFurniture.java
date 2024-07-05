package burrow.furniture.modcore;

import burrow.core.Burrow;
import burrow.core.chamber.Chamber;
import burrow.core.config.Config;
import burrow.core.furniture.BurrowFurniture;
import burrow.core.furniture.Furniture;
import burrow.core.furniture.InvalidFurnitureClassException;
import burrow.furniture.standard.StandardFurniture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.*;

@BurrowFurniture(
    simpleName = "Mod Core",
    description = "Mod Core allows developer to load mods (JAR files) into Burrow.",
    dependencies = {
        StandardFurniture.class
    }
)
public class ModCoreFurniture extends Furniture {
    private static final Logger logger = LoggerFactory.getLogger(ModCoreFurniture.class);
    public static final Path DEFAULT_MODS_DIR = Burrow.ROOT_DIR.resolve("mods");
    public static final String BURROW_JAR_PROPERTIES_FILE = "burrow.properties";
    public static final String COMMAND_TYPE = "Mods";

    /**
     * Mapping from paths of the mods (jar files) to the corresponding class loaders.
     */
    private final Map<Path, ClassLoader> pathClassLoaderMap = new HashMap<>();

    public ModCoreFurniture(@NonNull final Chamber chamber) {
        super(chamber);
    }

    @NonNull
    public Map<Path, ClassLoader> getPathClassLoaderMap() {
        return pathClassLoaderMap;
    }

    @Override
    public Collection<String> configKeys() {
        return List.of(ConfigKey.MODS_DIR);
    }

    @Override
    public void initializeConfig(@NonNull final Config config) {
        config.setIfAbsent(ConfigKey.MODS_DIR, DEFAULT_MODS_DIR.toString());
    }

    @Override
    public void beforeInitialization() {
        registerCommand(ModListCommand.class);
        registerCommand(ModFurnitureCommand.class);

        final var modsDir = new File(getConfig().getNonNull(ConfigKey.MODS_DIR));
        final var jarFileList = new ArrayList<File>();

        // Collect all jar files
        final var files = modsDir.listFiles();
        if (files != null) {
            for (final var file : files) {
                if (file.isFile() && file.getName().toLowerCase().endsWith(".jar")) {
                    jarFileList.add(file);
                }
            }
        }

        for (final var jarFile : jarFileList) {
            addMod(jarFile.toPath());
        }

        try {
            final var registrar = getRenovator().getRegistrar();
            registrar.rescanPackage();
        } catch (final InvalidFurnitureClassException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void addMod(@NonNull final Path jarPath) {
        try {
            final var jarFileUri = URI.create("file://" + jarPath);
            final var jarUrl = URI.create("jar:" + jarFileUri + "!/").toURL();
            final var urlArray = new URL[]{jarUrl};

            final var classLoader = new URLClassLoader(urlArray, Burrow.DEFAULT_CLASS_LOADER);
            final var resourceUrl = classLoader.getResource(BURROW_JAR_PROPERTIES_FILE);
            if (resourceUrl == null) {
                logger.error("Could not find properties file in JAR: {}", jarPath.toAbsolutePath());
                return;
            }

            final var registrar = getRenovator().getRegistrar();
            registrar.addClassLoader(classLoader);

            final var properties = new Properties();
            properties.load(resourceUrl.openStream());
            final var packageListString = (String) properties.get(JarPropertiesKey.PACKAGE_LIST);
            if (packageListString != null) {
                final var packageList = List.of(packageListString.split(":"));
                packageList.forEach(registrar::addPackage);
            }

            pathClassLoaderMap.put(jarPath, classLoader);
        } catch (final IOException ex) {
            logger.error("Fail to load jar: {}", jarPath.toAbsolutePath(), ex);
        }
    }

    @NonNull
    public List<Path> getModPathList() {
        return use(ModCoreFurniture.class).getPathClassLoaderMap().keySet().stream().toList();
    }

    public @interface ConfigKey {
        String MODS_DIR = "mods.dir";
    }

    public @interface JarPropertiesKey {
        String PACKAGE_LIST = "burrow.package-list";
    }
}
