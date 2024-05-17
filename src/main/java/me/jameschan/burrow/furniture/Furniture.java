package me.jameschan.burrow.furniture;

import me.jameschan.burrow.chamber.Chamber;
import me.jameschan.burrow.chamber.ChamberBased;
import me.jameschan.burrow.command.Command;
import me.jameschan.burrow.config.Config;

import java.util.Collection;

public abstract class Furniture extends ChamberBased implements ConfigSupport {
  public Furniture(final Chamber chamber) {
    super(chamber);
  }

  public void registerCommand(final Class<? extends Command> command) {
    getContext().getCommandManager().register(command);
  }

  public void disableCommand(final Class<? extends Command> command) {
    getContext().getCommandManager().disable(command);
  }

  @Override
  public Collection<String> configKeys() {
    return null;
  }

  @Override
  public void initConfig(final Config config) {}
}
