package me.jameschan.burrow.furnitures.config;

import me.jameschan.burrow.chamber.Chamber;
import me.jameschan.burrow.furniture.Furniture;
import me.jameschan.burrow.furnitures.config.command.ConfigCommand;

public class ConfigFurniture extends Furniture {
    public ConfigFurniture(final Chamber chamber) {
        super(chamber);

        registerCommand(ConfigCommand.class);
    }
}
