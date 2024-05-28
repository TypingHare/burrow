package me.jameschan.burrow.kernel.furniture.support;

import java.util.Collection;
import me.jameschan.burrow.kernel.config.Config;
import org.springframework.lang.Nullable;

public interface ConfigSupport {
  /**
   * Returns allowed config keys.
   *
   * @return the allowed config keys.
   */
  @Nullable
  Collection<String> configKeys();

  /**
   * Initializes config.
   *
   * @param config The config to initialize.
   */
  void initConfig(final Config config);
}
