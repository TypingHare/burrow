package me.jameschan.burrow.furniture;

import java.util.List;

public class CircularDependencyException extends RuntimeException {
  public CircularDependencyException(final List<String> dependencyPath) {
    super("Circular dependency found: \n" + String.join(" -> \n", dependencyPath));
  }
}
