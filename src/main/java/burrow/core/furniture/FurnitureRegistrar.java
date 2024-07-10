package burrow.core.furniture;

import burrow.core.furniture.exception.InvalidFurnitureClassException;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.beans.JavaBean;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.stream.Collectors;

public final class FurnitureRegistrar {
    private final Map<ClassLoader, Info> classLoaderInfoMap = new HashMap<>();
    private final Set<Class<? extends Furniture>> furnitureClassSet = new HashSet<>();

    public FurnitureRegistrar() {
    }

    public static void checkFurnitureClass(
        @NotNull final Class<? extends Furniture> furnitureClass) throws
        InvalidFurnitureClassException {
        final var annotation = furnitureClass.getAnnotation(BurrowFurniture.class);
        if (annotation == null) {
            throw new InvalidFurnitureClassException(furnitureClass.getName(),
                "The furniture class does not extend " + BurrowFurniture.class);
        }
    }

    @NotNull
    public Map<ClassLoader, Info> getClassLoaderInfoMap() {
        return classLoaderInfoMap;
    }

    @NotNull
    public Set<Class<? extends Furniture>> getFurnitureClassSet() {
        return furnitureClassSet;
    }

    @Nullable
    public Class<? extends Furniture> getByName(@NotNull final String name) {
        for (final var furnitureClass : furnitureClassSet) {
            if (furnitureClass.getName().equals(name)) {
                return furnitureClass;
            }
        }

        return null;
    }

    @NotNull
    public Info scanPackage(
        @NotNull ClassLoader classLoader,
        @NotNull final Collection<String> packageNames
    ) throws InvalidFurnitureClassException {
        final var filterBuilder = new FilterBuilder();
        for (final var packageName : packageNames) {
            filterBuilder.includePackage(packageName);
        }

        final Collection<URL> urlCollection = classLoader instanceof URLClassLoader ?
            List.of(((URLClassLoader) classLoader).getURLs()) :
            packageNames.stream()
                .flatMap(packageName -> ClasspathHelper.forPackage(packageName, classLoader)
                    .stream())
                .collect(Collectors.toSet());
        final var configuration = new ConfigurationBuilder()
            .filterInputsBy(filterBuilder)
            .setUrls(urlCollection)
            .addClassLoaders(classLoader);

        final var reflections = new Reflections(configuration);
        final var allFurnitureClassSet = reflections.getSubTypesOf(Furniture.class);
        for (final var furnitureClass : allFurnitureClassSet) {
            FurnitureRegistrar.checkFurnitureClass(furnitureClass);
        }

        final var info = classLoaderInfoMap.computeIfAbsent(classLoader, k -> new Info());
        info.furnitureClassList = allFurnitureClassSet.stream().toList();
        info.setPackageNames(packageNames);
        furnitureClassSet.addAll(allFurnitureClassSet);

        return info;
    }

    @JavaBean
    public static final class Info {
        private List<Class<? extends Furniture>> furnitureClassList;
        private Collection<String> packageNames;

        public List<Class<? extends Furniture>> getFurnitureClassList() {
            return furnitureClassList;
        }

        public void setFurnitureClassList(
            final List<Class<? extends Furniture>> furnitureClassList
        ) {
            this.furnitureClassList = furnitureClassList;
        }

        public Collection<String> getPackageNames() {
            return packageNames;
        }

        public void setPackageNames(final Collection<String> packageNames) {
            this.packageNames = packageNames;
        }
    }
}
