package me.jameschan.burrow.furniture;

import java.util.Collection;
import me.jameschan.burrow.config.Config;

public interface ConfigSupport {
  /**
   * Returns allowed config keys.
   *
   * @return the allowed config keys.
   */
  Collection<String> configKeys();

  /**
   * Initializes config.
   *
   * @param config The config to initialize.
   */
  void initConfig(final Config config);
}
