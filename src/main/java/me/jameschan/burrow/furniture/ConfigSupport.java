package me.jameschan.burrow.furniture;

import me.jameschan.burrow.config.Config;

import java.util.Collection;

public interface ConfigSupport {
    Collection<String> configKeys();

    void initConfig(final Config config);
}
