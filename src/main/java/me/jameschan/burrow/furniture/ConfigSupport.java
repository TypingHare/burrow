package me.jameschan.burrow.furniture;

import java.util.Collection;
import me.jameschan.burrow.config.Config;

public interface ConfigSupport {
  Collection<String> configKeys();

  void initConfig(final Config config);
}
