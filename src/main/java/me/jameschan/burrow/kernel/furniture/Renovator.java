package me.jameschan.burrow.kernel.furniture;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Predicate;
import me.jameschan.burrow.kernel.Chamber;
import me.jameschan.burrow.kernel.ChamberModule;
import me.jameschan.burrow.kernel.common.Constants;
import me.jameschan.burrow.kernel.config.Config;
import me.jameschan.burrow.kernel.furniture.annotation.BurrowFurniture;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Renovator extends ChamberModule {
  private final Map<String, Furniture> furnitureStore = new LinkedHashMap<>();

  public Renovator(final Chamber chamber) {
    super(chamber);
  }

  /** Loads furniture. */
  public void loadFurniture() throws FurnitureNotFoundException, InvalidFurnitureClassException {
    final var furnitureListString = context.getConfig().get(Config.Key.FURNITURE_LIST);
    final var furnitureNameList =
        Arrays.stream(furnitureListString.split(Constants.FURNITURE_NAME_SEPARATOR))
            .map(String::trim)
            .filter(Predicate.not(String::isEmpty))
            .toList();

    resolveDependencies(List.of(), furnitureNameList);
  }

  public void resolveDependencies(
      final List<String> dependencyPath, final List<String> dependencyList)
      throws FurnitureNotFoundException, InvalidFurnitureClassException {
    for (final var dependency : dependencyList) {
      // If the dependency is found within the dependency path, a circular dependency is found
      if (dependencyPath.contains(dependency)) {
        throw new CircularDependencyException(dependencyPath);
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

      // Add the dependency to the store
      furnitureStore.put(furnitureClass.getName(), furniture);
    }
  }

  public Class<? extends Furniture> checkIfFurnitureExist(final String name)
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

  public void testFurnitureClass(final Class<? extends Furniture> furnitureClass)
      throws InvalidFurnitureClassException {
    // Check if the given class extends the Furniture class and is annotated correctly
    if (!Furniture.class.isAssignableFrom(furnitureClass)
        || furnitureClass.getAnnotation(CommandLine.Command.class) == null) {
      throw new InvalidFurnitureClassException(furnitureClass.getName());
    }
  }

  public Furniture loadByName(final String name)
      throws FurnitureNotFoundException, InvalidFurnitureClassException {
    return loadByClass(checkIfFurnitureExist(name));
  }

  public Furniture loadByClass(final Class<? extends Furniture> clazz)
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
}
