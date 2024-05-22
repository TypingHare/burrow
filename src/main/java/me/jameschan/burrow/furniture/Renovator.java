package me.jameschan.burrow.furniture;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import me.jameschan.burrow.chamber.Chamber;
import me.jameschan.burrow.chamber.ChamberBased;
import me.jameschan.burrow.furniture.annotation.BurrowFurniture;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Renovator extends ChamberBased {
  private final Map<String, Furniture> byName = new LinkedHashMap<>();

  public Renovator(final Chamber chamber) {
    super(chamber);
  }

  public void resolveDependencies(
      final List<String> dependencyPath, final List<String> dependencies) {
    if (dependencies.isEmpty()) {
      return;
    }

    for (final var dependency : dependencies) {
      // If the dependency is found within the dependency path, a circular dependency is found
      if (dependencyPath.contains(dependency)) {
        throw new CircularDependencyException(dependencyPath);
      }

      // Load the furniture
      final var furniture = loadByName(dependency);
      final var furnitureClass = furniture.getClass();
      final var nextDependencies =
          Arrays.stream(furnitureClass.getDeclaredAnnotation(BurrowFurniture.class).dependencies())
              .map(Class::getName)
              .toList();

      // Resolve the dependency
      final var nextDependencyPath = new ArrayList<>(dependencyPath);
      nextDependencyPath.add(dependency);
      resolveDependencies(nextDependencyPath, nextDependencies);

      // Add the dependency to the map
      byName.put(furnitureClass.getName(), furniture);
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
      return constructor.newInstance(getChamber());
    } catch (InstantiationException
        | IllegalArgumentException
        | InvocationTargetException
        | IllegalAccessException ex) {
      throw new RuntimeException("Failed to instantiate furniture: " + clazz.getName(), ex);
    }
  }

  public Collection<Furniture> getAllFurniture() {
    return byName.values();
  }

  public <T extends Furniture> T getFurniture(final Class<T> clazz) {
    @SuppressWarnings("unchecked")
    final var furniture = (T) byName.get(clazz.getName());
    return furniture;
  }
}
