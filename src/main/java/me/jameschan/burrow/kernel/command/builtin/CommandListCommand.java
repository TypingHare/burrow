package me.jameschan.burrow.kernel.command.builtin;

import com.google.common.base.Strings;
import java.util.*;
import me.jameschan.burrow.kernel.command.Command;
import me.jameschan.burrow.kernel.common.ExitCode;
import me.jameschan.burrow.kernel.context.ChamberContext;
import me.jameschan.burrow.kernel.context.RequestContext;
import me.jameschan.burrow.kernel.furniture.AmbiguousSimpleNameException;
import me.jameschan.burrow.kernel.furniture.FurnitureNotFoundException;
import me.jameschan.burrow.kernel.furniture.annotation.CommandType;
import me.jameschan.burrow.kernel.utility.ColorUtility;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import picocli.CommandLine;

@CommandLine.Command(
    name = "mlist",
    description = "Display a list of all available commands along with their descriptions.")
@CommandType(CommandType.BUILTIN)
public class CommandListCommand extends Command {
  @CommandLine.Parameters(
      index = "0",
      paramLabel = "<furniture-name>",
      description = "Filter configuration items by furniture name.",
      defaultValue = CommandLine.Option.NULL_VALUE)
  private String furnitureName;

  public CommandListCommand(final RequestContext requestContext) {
    super(requestContext);
  }

  @Override
  public Integer call() throws FurnitureNotFoundException, AmbiguousSimpleNameException {
    final var commandClassList = getCommandClassList(context, furnitureName);
    final var commandClassListMap = classifyCommandClasses(commandClassList);
    commandClassListMap.replaceAll((k, v) -> sortCommandClassList(v));

    final var lines = new ArrayList<String>();
    for (final var commandType : commandClassListMap.keySet()) {
      final var sortedCommandClassList = commandClassListMap.get(commandType);
      lines.add(ColorUtility.render(" " + commandType + " ", ColorUtility.Type.COMMAND_TYPE));
      for (final var commandClass : sortedCommandClassList) {
        final var name = Command.getName(commandClass);
        final var descriptionArray = Command.getDescription(commandClass);
        final var description = descriptionArray.length > 0 ? descriptionArray[0] : "";
        lines.add(
            ColorUtility.render(Strings.padEnd(name, 16, ' '), ColorUtility.Type.COMMAND_NAME)
                + ColorUtility.render(description, ColorUtility.Type.DESCRIPTION));
      }
      lines.add(" ");
    }
    bufferAppendLines(lines);

    return ExitCode.SUCCESS;
  }

  @NonNull
  public static Collection<Class<? extends Command>> getCommandClassList(
      @NonNull final ChamberContext context, @Nullable final String furnitureName)
      throws FurnitureNotFoundException, AmbiguousSimpleNameException {
    return furnitureName == null
        ? context.getProcessor().getAllCommands()
        : context.getRenovator().getFurnitureByName(furnitureName).getAllCommands();
  }

  @NonNull
  public static Map<String, Collection<Class<? extends Command>>> classifyCommandClasses(
      @NonNull final Collection<Class<? extends Command>> commandClassList) {
    final var map = new HashMap<String, Collection<Class<? extends Command>>>();
    for (final var commandClass : commandClassList) {
      final var commandType = Command.getType(commandClass);
      map.computeIfAbsent(commandType, k -> new ArrayList<>()).add(commandClass);
    }

    return map;
  }

  @NonNull
  public static Collection<Class<? extends Command>> sortCommandClassList(
      @NonNull Collection<Class<? extends Command>> commandClassList) {
    return commandClassList.stream().sorted(Comparator.comparing(Command::getName)).toList();
  }
}
