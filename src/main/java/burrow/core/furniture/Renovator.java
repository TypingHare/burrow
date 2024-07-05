package burrow.core.furniture;

import burrow.core.chamber.Chamber;
import burrow.core.chamber.ChamberModule;
import burrow.core.config.Config;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.*;
import java.util.function.Predicate;

public final class Renovator extends ChamberModule {
    public static final String FURNITURE_NAME_SEPARATOR = ":";

    private final Map<String, Furniture> furnitureStore = new LinkedHashMap<>();
    private final DependencyTree dependencyTree = new DependencyTree();

    public Renovator(@NonNull final Chamber chamber) {
        super(chamber);
    }

    @NonNull
    public Map<String, Furniture> getFurnitureStore() {
        return furnitureStore;
    }

    @NonNull
    public FurnitureRegistrar getRegistrar() {
        return chamber.getChamberShepherd().getBurrow().getFurnitureRegistrar();
    }

    public void loadFurniture() throws FurnitureNotFoundException, InvalidFurnitureClassException {
        final var furnitureListString = getConfig().getNonNull(Config.Key.CHAMBER_FURNITURE_LIST);
        final var furnitureNameList =
            Arrays.stream(furnitureListString.split(FURNITURE_NAME_SEPARATOR))
                .map(String::trim)
                .filter(Predicate.not(String::isEmpty))
                .toList();

        resolveDependencies(List.of(), furnitureNameList, dependencyTree.getRoot());

        // Load config, after which put the furniture to the store
        dependencyTree.resolve(furniture -> {
            final var config = getConfig();
            final var configKeys = furniture.configKeys();
            if (configKeys != null) {
                configKeys.forEach(config::addAllowedKey);
            }
            furniture.initializeConfig(config);

            furnitureStore.put(furniture.getClass().getName(), furniture);
        });

        // Before initialization
        dependencyTree.resolve(Furniture::beforeInitialization);

        // Initialize
        dependencyTree.resolve(furniture -> {
            furniture.setInitialized(true);
            furniture.initialize();
        });

        // After initialization
        dependencyTree.resolve(Furniture::afterInitialization);
    }

    private void resolveDependencies(
        @NonNull final List<String> dependencyPath,
        @NonNull final List<String> dependencyList,
        @NonNull final DependencyTree.Node dependencyNode
    ) throws InvalidFurnitureClassException {
        final var registrar = getRegistrar();

        for (final var dependency : dependencyList) {
            if (dependency == null || furnitureStore.containsKey(dependency)) {
                continue;
            }

            if (dependencyPath.contains(dependency)) {
                throw new CircularDependencyException(dependencyPath);
            }

            // Check if the furniture is in the registrar
            final var furnitureClass = registrar.getByName(dependency);
            if (furnitureClass == null) {
                throw new FurnitureNotFoundException(dependency);
            }

            final var furniture = instantiateFurniture(furnitureClass);

            // Resolve the dependencies of the furniture class
            final var nextDependencyList =
                Arrays.stream(furnitureClass.getDeclaredAnnotation(BurrowFurniture.class)
                    .dependencies()).map(Class::getName).toList();
            final var nextDependencyPath = new ArrayList<>(dependencyPath);
            final var nextNode = new DependencyTree.Node(furniture);
            nextDependencyPath.add(dependency);
            resolveDependencies(nextDependencyPath, nextDependencyList, nextNode);
            dependencyNode.add(nextNode);
        }
    }

    @NonNull
    private Furniture instantiateFurniture(
        @NonNull final Class<? extends Furniture> furnitureClass) throws
        InvalidFurnitureClassException {
        try {
            final var constructor = furnitureClass.getConstructor(Chamber.class);
            return constructor.newInstance(chamber);
        } catch (final Exception ex) {
            throw new InvalidFurnitureClassException(furnitureClass.getName(),
                "Unable to instantiate " + furnitureClass.getName());
        }
    }

    @Nullable
    public <T extends Furniture> T getFurniture(@NonNull final String furnitureName) {
        @SuppressWarnings("unchecked") final T furniture =
            (T) furnitureStore.get(furnitureName);
        return furniture;
    }

    public void terminateAllFurniture() {
        for (final var furniture : furnitureStore.values()) {
            furniture.terminate();
        }
    }
}
