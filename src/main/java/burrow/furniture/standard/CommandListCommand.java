package burrow.furniture.standard;

import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import burrow.core.common.ColorUtility;
import com.google.common.base.Strings;
import org.springframework.lang.NonNull;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@CommandLine.Command(
    name = "mlist",
    description = "Display a list of available commands along with their descriptions."
)
@CommandType(StandardFurniture.COMMAND_TYPE)
public class CommandListCommand extends Command {
    @CommandLine.Option(
        names = {"-f", "--furniture"},
        paramLabel = "<furniture-name>",
        description = "Filter configuration items by furniture name.",
        defaultValue = CommandLine.Option.NULL_VALUE
    )
    private String furnitureName;

    @CommandLine.Option(
        names = {"-a", "--all"},
        description = "Show all available commands.",
        defaultValue = "false"
    )
    private Boolean all;

    public CommandListCommand(@NonNull final CommandContext commandContext) {
        super(commandContext);
    }

    @Override
    public Integer call() {
        final var standardFurniture = use(StandardFurniture.class);
        final var furnitureNameList = new ArrayList<String>();
        if (furnitureName != null) {
            furnitureNameList.add(furnitureName);
        } else if (!all) {
            furnitureNameList.addAll(standardFurniture.getConfigFurnitureNameList());
            furnitureNameList.remove(StandardFurniture.class.getName());
        }

        final var commandClassList = all ?
            standardFurniture.getCommandClassList() :
            standardFurniture.getCommandClassList(furnitureNameList);
        final var commandClassListMap = StandardFurniture.classifyCommandClasses(commandClassList);
        commandClassListMap.replaceAll((k, v) -> StandardFurniture.sortCommandClassList(v));
        display(commandClassListMap);

        return CommandLine.ExitCode.OK;
    }

    private void display(Map<String, Collection<Class<? extends Command>>> commandClassListMap) {
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
    }
}
