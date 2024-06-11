package me.jameschan.burrow.kernel.command;

import com.google.common.collect.ImmutableList;
import java.util.*;
import me.jameschan.burrow.kernel.Chamber;
import me.jameschan.burrow.kernel.ChamberModule;
import me.jameschan.burrow.kernel.command.builtin.*;
import me.jameschan.burrow.kernel.command.entry.*;
import me.jameschan.burrow.kernel.command.special.*;
import me.jameschan.burrow.kernel.common.ExitCode;
import me.jameschan.burrow.kernel.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Processor extends ChamberModule {
  public static final String DEFAULT_COMMAND_NAME = "__default__";
  public static final String UNKNOWN_COMMAND_NAME = "__unknown__";
  public static final List<Class<? extends Command>> BUILTIN_COMMAND_LIST =
      ImmutableList.of(
          // Special commands
          UnknownCommand.class,
          DefaultCommand.class,
          // Builtin commands
          RootCommand.class,
          CommandListCommand.class,
          HelpCommand.class,
          ConfigItemCommand.class,
          ConfigListCommand.class,
          FurnitureListCommand.class,
          FurnitureAddCommand.class,
          FurnitureRemoveCommand.class,
          DescriptionCommand.class,
          HoardCommand.class,
          // Entry commands
          NewCommand.class,
          EntryCommand.class,
          ExistCommand.class,
          DeleteCommand.class,
          CountCommand.class,
          EntriesCommand.class,
          PropCommand.class,
          SetCommand.class,
          UnsetCommand.class);

  private static final Logger logger = LoggerFactory.getLogger(Processor.class);

  private final Map<String, Class<? extends Command>> commandClassStore = new HashMap<>();
  private final Map<String, String> aliasMap = new HashMap<>();

  public Processor(final Chamber chamber) {
    super(chamber);

    BUILTIN_COMMAND_LIST.forEach(this::register);
  }

  public int execute(
      final String commandName, final List<String> args, final RequestContext requestContext) {
    var commandClass = commandClassStore.get(commandName);
    if (commandClass == null) {
      final var aliasCommandName = aliasMap.get(commandName);
      if (aliasCommandName != null) {
        commandClass = commandClassStore.get(aliasCommandName);
      }
    }
    if (commandClass == null) commandClass = UnknownCommand.class;

    try {
      final var constructor = commandClass.getConstructor(RequestContext.class);
      final var command = constructor.newInstance(requestContext);
      return new CommandLine(command)
          .setParameterExceptionHandler(command)
          .setExecutionExceptionHandler(command)
          .execute(args.toArray(new String[0]));
    } catch (final Throwable ex) {
      logger.error("Fail to execute command: {}", commandName, ex);

      return ExitCode.ERROR;
    }
  }

  public void register(final Class<? extends Command> commandClass) {
    final var commandAnnotation = commandClass.getAnnotation(CommandLine.Command.class);
    if (commandAnnotation == null) {
      throw new RuntimeException(
          "Fail to register command, as it is not annotated by "
              + "picocli.CommandLine.Command: "
              + commandClass.getName());
    }

    final var commandName = commandAnnotation.name();
    commandClassStore.put(commandName, commandClass);
  }

  public void disable(final Class<? extends Command> commandClass) {
    commandClassStore.entrySet().removeIf(entry -> entry.getValue() == commandClass);
  }

  public Collection<Class<? extends Command>> getAllCommands() {
    return commandClassStore.values();
  }

  public void setAlias(@NonNull final String commandName, @NonNull final String alias) {
    if (getCommand(commandName) != null) {
      aliasMap.put(alias, commandName);
    }
  }

  @Nullable
  public String getCommandNameByAlias(@NonNull final String alias) {
    return aliasMap.get(alias);
  }

  @Nullable
  public Class<? extends Command> getCommand(@NonNull final String commandName) {
    return commandClassStore.get(commandName);
  }

  @NonNull
  public static String getCommandName(@NonNull final Class<? extends Command> commandClass) {
    return commandClass.getAnnotation(CommandLine.Command.class).name();
  }
}
