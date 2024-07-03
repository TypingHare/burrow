package burrow.furniture.standard;

import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import burrow.core.common.ColorUtility;
import burrow.core.furniture.AmbiguousSimpleNameException;
import com.google.common.base.Strings;
import org.springframework.lang.NonNull;
import picocli.CommandLine;

import java.util.ArrayList;

@CommandLine.Command(
    name = "mls",
    description = "Display a list of all available commands along with their descriptions."
)
@CommandType(StandardFurniture.COMMAND_TYPE)
public class CommandListComand extends Command {
    @CommandLine.Parameters(
        index = "0",
        paramLabel = "<furniture-name>",
        description = "Filter configuration items by furniture name.",
        defaultValue = CommandLine.Option.NULL_VALUE
    )
    private String furnitureName;

    public CommandListComand(@NonNull final CommandContext commandContext) {
        super(commandContext);
    }

    @Override
    public Integer call() throws AmbiguousSimpleNameException {
        final var commandClassList = StandardFurniture.getCommandClassList(context, furnitureName);
        final var commandClassListMap = StandardFurniture.classifyCommandClasses(commandClassList);
        commandClassListMap.replaceAll((k, v) -> StandardFurniture.sortCommandClassList(v));

        final var lines = new ArrayList<String>();
        for (final var commandType : commandClassListMap.keySet()) {
            final var sortedCommandClassList = commandClassListMap.get(commandType);
            lines.add(ColorUtility.render(
                " " + commandType + " ", ColorUtility.Type.NAME_COMMAND_TYPE));
            for (final var commandClass : sortedCommandClassList) {
                final var name = Command.getName(commandClass);
                final var coloredName =
                    ColorUtility.render(Strings.padEnd(name, 16, ' '), ColorUtility.Type.NAME_COMMAND);
                final var descriptionArray = Command.getDescription(commandClass);
                final var description = descriptionArray.length > 0 ? descriptionArray[0] : "";
                final var coloredDescription =
                    ColorUtility.render(description, ColorUtility.Type.DESCRIPTION);
                lines.add(coloredName + coloredDescription);
            }
            lines.add(" ");
        }

        bufferAppendLines(lines);

        return CommandLine.ExitCode.OK;
    }
}
