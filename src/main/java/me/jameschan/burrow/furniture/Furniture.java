package me.jameschan.burrow.furniture;

import me.jameschan.burrow.chamber.Chamber;
import me.jameschan.burrow.chamber.ChamberBased;
import me.jameschan.burrow.config.Config;

import java.util.Collection;

public abstract class Furniture extends ChamberBased implements ConfigSupport {
    public Furniture(final Chamber chamber) {
        super(chamber);
    }

    @Override
    public Collection<String> configKeys() {
        return null;
    }

    @Override
    public void initConfig(final Config config) {
    }
}
