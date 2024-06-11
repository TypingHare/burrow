package me.jameschan.burrow.kernel.furniture;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Predicate;
import me.jameschan.burrow.kernel.Chamber;
import me.jameschan.burrow.kernel.ChamberModule;
import me.jameschan.burrow.kernel.config.Config;
import me.jameschan.burrow.kernel.furniture.annotation.BurrowFurniture;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Renovator extends ChamberModule {
  public static final String FURNITURE_NAME_SEPARATOR = ":";

  private final Map<String, Furniture> furnitureStore = new LinkedHashMap<>();
  private final Map<String, List<String>> fullNameMap = new HashMap<>();

  public Renovator(@NonNull final Chamber chamber) {
    super(chamber);
  }

  /** Loads furniture. */
  public void loadFurniture() throws FurnitureNotFoundException, InvalidFurnitureClassException {
    final var furnitureListString = context.getConfig().get(Config.Key.FURNITURE_LIST);
    if (furnitureListString == null) {
      return;
    }

    final var furnitureNameList =
        Arrays.stream(furnitureListString.split(Renovator.FURNITURE_NAME_SEPARATOR))
            .map(String::trim)
            .filter(Predicate.not(String::isEmpty))
            .toList();

    resolveDependencies(List.of(), furnitureNameList);
  }

  public void resolveDependencies(
      @NonNull final List<String> dependencyPath, @NonNull final List<String> dependencyList)
      throws FurnitureNotFoundException, InvalidFurnitureClassException {
    for (final var dependency : dependencyList) {
      // If the dependency is found within the dependency path, a circular dependency is found
      if (dependencyPath.contains(dependency)) {
        throw new CircularDependencyException(dependencyPath);
      }

      if (furnitureStore.containsKey(dependency)) {
        continue;
      }

      // Load the furniture
      final var furniture = loadByName(dependency);
      final var furnitureClass = furniture.getClass();
      final var nextDependencyList =
          Arrays.stream(furnitureClass.getDeclaredAnnotation(BurrowFurniture.class).dependencies())
              .map(Class::getName)
              .toList();

      // Resolve the dependency
      final var nextDependencyPath = new ArrayList<>(dependencyPath);
      nextDependencyPath.add(dependency);
      resolveDependencies(nextDependencyPath, nextDependencyList);

      // Register the dependency
      if (!furnitureStore.containsKey(furniture.getClass().getName())) {
        register(furniture);
        loadConfigFrom(furniture);
        furniture.init();
      }
    }
  }

  private void register(@NonNull final Furniture furniture) {
    final var fullName = furniture.getClass().getName();
    final var simpleName = getSimpleName(fullName);
    furnitureStore.put(fullName, furniture);
    fullNameMap.computeIfAbsent(simpleName, k -> new ArrayList<>()).add(fullName);
  }

  private void loadConfigFrom(@NonNull final Furniture furniture) {
    final var config = context.getConfig();
    final var configKeys = furniture.configKeys();
    if (configKeys != null) {
      configKeys.forEach(config::addAllowedKey);
    }

    furniture.initConfig(config);
  }

  @NonNull
  public String getSimpleName(@NonNull final String fullName) {
    final var sp = fullName.split("\\.");
    return sp.length > 2 ? sp[sp.length - 2] : "";
  }

  @NonNull
  public Class<? extends Furniture> checkIfFurnitureExist(@NonNull final String name)
      throws FurnitureNotFoundException {
    try {
      @SuppressWarnings("unchecked")
      final Class<? extends Furniture> furnitureClass =
          (Class<? extends Furniture>) Class.forName(name);
      return furnitureClass;
    } catch (final ClassNotFoundException ex) {
      throw new FurnitureNotFoundException(name);
    }
  }

  public void testFurnitureClass(@NonNull final Class<? extends Furniture> furnitureClass)
      throws InvalidFurnitureClassException {
    // Check if the given class extends the Furniture class and is annotated correctly
    if (!Furniture.class.isAssignableFrom(furnitureClass)
        || furnitureClass.getDeclaredAnnotation(BurrowFurniture.class) == null) {
      throw new InvalidFurnitureClassException(furnitureClass.getName());
    }
  }

  @NonNull
  public Furniture loadByName(@NonNull final String name)
      throws FurnitureNotFoundException, InvalidFurnitureClassException {
    return loadByClass(checkIfFurnitureExist(name));
  }

  @NonNull
  public Furniture loadByClass(@NonNull final Class<? extends Furniture> clazz)
      throws InvalidFurnitureClassException {
    testFurnitureClass(clazz);

    final var burrowFurnitureAnnotation = clazz.getAnnotation(BurrowFurniture.class);
    if (burrowFurnitureAnnotation == null) {
      throw new RuntimeException(
          "Furniture class is not annotated by BurrowFurniture: " + clazz.getName());
    }

    Constructor<? extends Furniture> constructor;
    try {
      constructor = clazz.getDeclaredConstructor(Chamber.class);
    } catch (final NoSuchMethodException ex) {
      throw new RuntimeException(ex);
    }

    try {
      constructor.setAccessible(true);
      return constructor.newInstance(chamber);
    } catch (InstantiationException
        | IllegalArgumentException
        | InvocationTargetException
        | IllegalAccessException ex) {
      throw new RuntimeException("Failed to instantiate furniture: " + clazz.getName(), ex);
    }
  }

  @NonNull
  public Collection<Furniture> getAllFurniture() {
    return furnitureStore.values();
  }

  @NonNull
  public <T extends Furniture> T getFurniture(final Class<T> furnitureClass) {
    @SuppressWarnings("unchecked")
    final var furniture = (T) furnitureStore.get(furnitureClass.getName());

    if (furniture == null) {
      throw new RuntimeException("Furniture not found: " + furnitureClass.getName());
    }

    return furniture;
  }

  @NonNull
  public Furniture getFurnitureByFullName(@NonNull final String fullName)
      throws FurnitureNotFoundException {
    return Optional.ofNullable(furnitureStore.get(fullName))
        .orElseThrow(() -> new FurnitureNotFoundException(fullName));
  }

  @NonNull
  public Furniture getFurnitureBySimpleName(@NonNull final String simpleName)
      throws FurnitureNotFoundException, AmbiguousSimpleNameException {
    final var fullNameList = fullNameMap.get(simpleName);
    if (fullNameList == null) {
      throw new FurnitureNotFoundException(simpleName);
    }

    if (fullNameList.size() > 1) {
      throw new AmbiguousSimpleNameException(simpleName);
    }

    return getFurnitureByFullName(fullNameList.getFirst());
  }

  @NonNull
  public Furniture getFurnitureByName(@NonNull final String name)
      throws FurnitureNotFoundException, AmbiguousSimpleNameException {
    return name.contains(".") ? getFurnitureByFullName(name) : getFurnitureBySimpleName(name);
  }

  @NonNull
  public Map<String, List<String>> getFullNameMap() {
    return fullNameMap;
  }

  @NonNull
  public List<String> getAllFullNames() {
    return furnitureStore.values().stream()
        .map(furniture -> furniture.getClass().getName())
        .toList();
  }

  public void terminateAllFurniture() {
    furnitureStore.values().forEach(Furniture::onTerminate);
  }
}
