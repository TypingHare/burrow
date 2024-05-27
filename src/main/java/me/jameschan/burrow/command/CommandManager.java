package me.jameschan.burrow.command;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import me.jameschan.burrow.chamber.Chamber;
import me.jameschan.burrow.chamber.ChamberBased;
import me.jameschan.burrow.command.builtin.*;
import me.jameschan.burrow.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CommandManager extends ChamberBased {
  private static final Logger logger = LoggerFactory.getLogger(CommandManager.class);
  private final Map<String, Class<? extends Command>> byName = new HashMap<>();

  public CommandManager(final Chamber chamber) {
    super(chamber);

    // Special commands
    byName.put(null, UnknownCommand.class);
    register(DefaultCommand.class);

    // Chamber commands
    register(RootCommand.class);
    register(CommandsCommand.class);
    register(HelpCommand.class);
    register(ConfigCommand.class);
    register(FurnitureCommand.class);

    // Entry commands
    register(NewCommand.class);
    register(EntryCommand.class);
    register(ExistCommand.class);
    register(DeleteCommand.class);
    register(EntriesCommand.class);
  }

  public int execute(
      final String commandName, final List<String> args, final RequestContext context) {
    final var commandClass = byName.getOrDefault(commandName, UnknownCommand.class);

    try {
      final var constructor = commandClass.getConstructor(RequestContext.class);
      final var command = constructor.newInstance(context);
      return new CommandLine(command)
          .setParameterExceptionHandler(command)
          .setExecutionExceptionHandler(command)
          .execute(args.toArray(new String[0]));
    } catch (final Exception ex) {
      logger.error("Fail to execute command.", ex);
      return 1;
    }
  }

  public void register(Class<? extends Command> clazz) {
    final var commandAnnotation = clazz.getDeclaredAnnotationsByType(CommandLine.Command.class);
    if (commandAnnotation.length == 0) {
      throw new RuntimeException(
          "Fail to register command, as it is not annotated by "
              + "picocli.CommandLine.Command: "
              + clazz.getName());
    }

    final var commandName = commandAnnotation[0].name();
    byName.put(commandName, clazz);
  }

  public void disable(Class<? extends Command> clazz) {
    final var commandAnnotation = clazz.getDeclaredAnnotation(CommandLine.Command.class);
    if (commandAnnotation == null) {
      throw new RuntimeException(
          "Fail to register command, as it is not annotated by "
              + "picocli.CommandLine.Command: "
              + clazz.getName());
    }

    final var commandName = commandAnnotation.name();
    byName.remove(commandName);
  }

  public Collection<Class<? extends Command>> getAllCommands() {
    final var specialCommand = List.of(UnknownCommand.class, DefaultCommand.class);

    return byName.values().stream().filter(Predicate.not(specialCommand::contains)).toList();
  }

  public Class<? extends Command> getCommand(final String commandName) {
    return byName.get(commandName);
  }
}
