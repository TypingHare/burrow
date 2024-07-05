package burrow.core.furniture;

import burrow.core.Burrow;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class FurnitureRegistrar {
    private final Burrow burrow;
    private final Set<ClassLoader> classLoaderSet = new HashSet<>();
    private final Map<ClassLoader, Set<Class<? extends Furniture>>> furnitureMap = new HashMap<>();
    private final Set<String> packageSet = new HashSet<>();
    private final Set<Class<? extends Furniture>> furnitureClassSet = new HashSet<>();

    public FurnitureRegistrar(@NonNull final Burrow burrow) {
        this.burrow = burrow;
    }

    @NonNull
    public Burrow getBurrow() {
        return burrow;
    }

    @NonNull
    public Set<Class<? extends Furniture>> getFurnitureClassSet() {
        return furnitureClassSet;
    }

    @Nullable
    public Class<? extends Furniture> getByName(@NonNull final String name) {
        for (final var furnitureClass : furnitureClassSet) {
            if (furnitureClass.getName().equals(name)) {
                return furnitureClass;
            }
        }

        return null;
    }

    public void addClassLoader(@NonNull final ClassLoader classLoader) {
        classLoaderSet.add(classLoader);
    }

    public void addPackage(@NonNull final String packageName) {
        packageSet.add(packageName);
    }

    public void rescanPackage() throws InvalidFurnitureClassException {
        for (final var packageName : packageSet) {
            scanPackage(packageName);
        }
    }

    private void scanPackage(
        @NonNull final String packageName
    ) throws InvalidFurnitureClassException {
        packageSet.add(packageName);

        for (final var classLoader : classLoaderSet) {
            final var configuration = new ConfigurationBuilder()
                .filterInputsBy(new FilterBuilder().includePackage(packageName))
                .addClassLoaders(classLoader);

            if (classLoader instanceof URLClassLoader) {
                configuration.setUrls(((URLClassLoader) classLoader).getURLs());
            } else {
                configuration.setUrls(ClasspathHelper.forPackage(packageName, classLoader));
            }

            final var reflections = new Reflections(configuration);
            final var allFurnitureClassSet = reflections.getSubTypesOf(Furniture.class);
            for (final var furnitureClass : allFurnitureClassSet) {
                FurnitureRegistrar.checkFurnitureClass(furnitureClass);
            }

            final var furnitureClassSet =
                furnitureMap.computeIfAbsent(classLoader, k -> new HashSet<>());
            furnitureClassSet.addAll(allFurnitureClassSet);
            this.furnitureClassSet.addAll(allFurnitureClassSet);
        }
    }

    public static void checkFurnitureClass(
        @NonNull final Class<? extends Furniture> furnitureClass) throws
        InvalidFurnitureClassException {
        final var annotation = furnitureClass.getAnnotation(BurrowFurniture.class);
        if (annotation == null) {
            throw new InvalidFurnitureClassException(furnitureClass.getName(),
                "The furniture class does not extend " + BurrowFurniture.class);
        }
    }
}
