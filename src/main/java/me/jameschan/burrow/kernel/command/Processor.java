package me.jameschan.burrow.kernel.command;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import me.jameschan.burrow.kernel.Chamber;
import me.jameschan.burrow.kernel.ChamberModule;
import me.jameschan.burrow.kernel.command.builtin.CommandsCommand;
import me.jameschan.burrow.kernel.command.builtin.DefaultCommand;
import me.jameschan.burrow.kernel.command.builtin.RootCommand;
import me.jameschan.burrow.kernel.command.builtin.UnknownCommand;
import me.jameschan.burrow.kernel.common.ExitCode;
import me.jameschan.burrow.kernel.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Processor extends ChamberModule {
  public static final String DEFAULT_COMMAND_NAME = "";
  public static final String UNKNOWN_COMMAND_NAME = "unknown";
  public static final ImmutableList<Class<? extends Command>> SPECIAL_COMMAND_LIST =
      ImmutableList.of(UnknownCommand.class, DefaultCommand.class);

  private static final Logger logger = LoggerFactory.getLogger(Processor.class);

  private final Map<String, Class<? extends Command>> commandClassStore = new HashMap<>();

  public Processor(final Chamber chamber) {
    super(chamber);

    // Special commands
    register(DefaultCommand.class);
    register(UnknownCommand.class);

    // Chamber scope commands
    register(RootCommand.class);
    register(CommandsCommand.class);
  }

  public int execute(
      final String commandName, final List<String> args, final RequestContext context) {
    final var commandClass = commandClassStore.getOrDefault(commandName, UnknownCommand.class);

    try {
      final var constructor = commandClass.getConstructor(RequestContext.class);
      final var command = constructor.newInstance(context);
      return new CommandLine(command)
          .setParameterExceptionHandler(command)
          .setExecutionExceptionHandler(command)
          .execute(args.toArray(new String[0]));
    } catch (final Exception ex) {
      logger.error("Fail to execute command.", ex);

      return ExitCode.ERROR;
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
    commandClassStore.put(commandName, clazz);
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
    commandClassStore.remove(commandName);
  }

  public Collection<Class<? extends Command>> getAllCommands() {
    return commandClassStore.values().stream()
        .filter(Predicate.not(SPECIAL_COMMAND_LIST::contains))
        .toList();
  }

  public Class<? extends Command> getCommand(final String commandName) {
    return commandClassStore.get(commandName);
  }
}
