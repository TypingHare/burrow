package burrow.furniture.modcore;

import burrow.core.Burrow;
import burrow.core.chamber.Chamber;
import burrow.core.config.Config;
import burrow.core.furniture.BurrowFurniture;
import burrow.core.furniture.Furniture;
import burrow.core.furniture.FurnitureRegistrar;
import burrow.core.furniture.exception.InvalidFurnitureClassException;
import burrow.furniture.modcore.exception.BurrowPropertiesNotFoundException;
import burrow.furniture.standard.StandardFurniture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

import java.beans.JavaBean;
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
    },
    type = BurrowFurniture.Type.ROOT
)
public class ModCoreFurniture extends Furniture {
    public static final Path DEFAULT_MODS_DIR = Burrow.ROOT_DIR.resolve("mods");
    public static final String BURROW_JAR_PROPERTIES_FILE = "burrow.properties";
    public static final String COMMAND_TYPE = "Mods";
    private static final Logger logger = LoggerFactory.getLogger(ModCoreFurniture.class);
    /**
     * Mapping from paths of the mods (jar files) to the corresponding class loaders.
     */
    private final Map<Path, Mod> pathModHashMap = new HashMap<>();

    public ModCoreFurniture(@NonNull final Chamber chamber) {
        super(chamber);
    }

    @NonNull
    public Map<Path, Mod> getPathModHashMap() {
        return pathModHashMap;
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
            try {
                final var jarPath = jarFile.toPath();
                final var mod = loadMod(jarPath);
                pathModHashMap.put(jarFile.toPath(), mod);
                logger.info("Loaded Mod JAR: {}", jarFile);
            } catch (final IOException ex) {
                logger.error("Failed to load Mod JAR: {}", jarFile, ex);
            } catch (final BurrowPropertiesNotFoundException ex) {
                logger.error(ex.getMessage(), ex);
            } catch (final InvalidFurnitureClassException ex) {
                logger.error("Fail to load furniture in Mod JAR: {}", jarFile, ex);
            }
        }
    }

    /**
     * Loads a mod, which is a JAR file, by scanning it properties and furniture.
     * @param jarPath The path to the JAR file to load.
     */
    public Mod loadMod(@NonNull final Path jarPath) throws
        IOException,
        InvalidFurnitureClassException, BurrowPropertiesNotFoundException {
        final var jarFileUri = URI.create("file://" + jarPath);
        final var jarUrl = URI.create("jar:" + jarFileUri + "!/").toURL();
        final var urlArray = new URL[]{jarUrl};

        final var classLoader = new URLClassLoader(urlArray, Burrow.DEFAULT_CLASS_LOADER);
        final var resourceUrl = classLoader.getResource(BURROW_JAR_PROPERTIES_FILE);
        if (resourceUrl == null) {
            throw new BurrowPropertiesNotFoundException(jarPath);
        }

        final var properties = new Properties();
        properties.load(resourceUrl.openStream());
        final var packageListString = (String) properties.get(JarPropertiesKey.PACKAGE_LIST);
        if (packageListString == null) {
            logger.error("Mod JAR does not specify package list: {}", jarPath.toAbsolutePath());
            throw new RuntimeException("");
        }

        // Scan the Mod JAR
        final var packageList = List.of(packageListString.split(":"));
        final var registrar = getRenovator().getRegistrar();
        final var info = registrar.scanPackage(classLoader, packageList);

        final var mod = new Mod();
        mod.setClassLoader(classLoader);
        mod.setProperties(properties);
        mod.setInfo(info);

        return mod;
    }

    @NonNull
    public List<Path> getModPathList() {
        return pathModHashMap.keySet().stream().toList();
    }

    @NonNull
    public Mod getMod(@NonNull final Path modPath) {
        return Objects.requireNonNull(pathModHashMap.get(modPath));
    }

    public @interface ConfigKey {
        String MODS_DIR = "mods.dir";
    }

    public @interface JarPropertiesKey {
        String PACKAGE_LIST = "burrow.package-list";
    }

    @JavaBean
    public static final class Mod {
        private URLClassLoader classLoader;
        private Properties properties;
        private FurnitureRegistrar.Info info;

        public URLClassLoader getClassLoader() {
            return classLoader;
        }

        public void setClassLoader(final URLClassLoader classLoader) {
            this.classLoader = classLoader;
        }

        public Properties getProperties() {
            return properties;
        }

        public void setProperties(final Properties properties) {
            this.properties = properties;
        }

        public FurnitureRegistrar.Info getInfo() {
            return info;
        }

        public void setInfo(final FurnitureRegistrar.Info info) {
            this.info = info;
        }
    }
}
