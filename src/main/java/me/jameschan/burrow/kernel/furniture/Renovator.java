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

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Renovator extends ChamberModule {
  private final Map<String, Furniture> furnitureStore = new LinkedHashMap<>();

  public Renovator(final Chamber chamber) {
    super(chamber);
  }

  /** Loads furniture. */
  public void loadFurniture() {
    final var furnitureListString = context.getConfig().get(Config.Key.FURNITURE_LIST);
    final var furnitureNameList =
        Arrays.stream(furnitureListString.split(Constants.FURNITURE_NAME_SEPARATOR))
            .map(String::trim)
            .filter(Predicate.not(String::isEmpty))
            .toList();

    resolveDependencies(List.of(), furnitureNameList);
  }

  public void resolveDependencies(
      final List<String> dependencyPath, final List<String> dependencyList) {
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

  public Class<? extends Furniture> checkIfFurnitureExist(final String name) {
    try {
      @SuppressWarnings("unchecked")
      final var clazz = (Class<? extends Furniture>) Class.forName(name);
      return clazz;
    } catch (final ClassNotFoundException ex) {
      throw new RuntimeException(ex);
    }
  }

  public void testFurnitureClass(final Class<? extends Furniture> clazz) {
    final String name = clazz.getName();
    if (!Furniture.class.isAssignableFrom(clazz)) {
      throw new ClassCastException("Class does not extend Furniture: " + name);
    }
  }

  public Furniture loadByName(final String name) {
    return loadByClass(checkIfFurnitureExist(name));
  }

  public Furniture loadByClass(final Class<? extends Furniture> clazz) {
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
