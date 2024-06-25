package burrow.furniture.standard;

import burrow.core.chamber.ChamberInitializationException;
import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import burrow.core.config.Config;
import burrow.core.furniture.Renovator;
import picocli.CommandLine;

import java.util.Arrays;

@CommandLine.Command(name = "frm", description = "Remove a furniture from the current chamber.")
@CommandType(StandardFurniture.COMMAND_TYPE)
public class FurnitureRemoveCommand extends Command {
    @CommandLine.Parameters(
        index = "0",
        paramLabel = "<name>",
        description = "The full name of the furniture to removes.")
    private String name;

    public FurnitureRemoveCommand(final CommandContext commandContext) {
        super(commandContext);
    }

    @Override
    public Integer call() throws ChamberInitializationException {
        final var config = context.getConfig();
        final var furnitureListString = config.getRequireNotNull(Config.Key.FURNITURE_LIST);
        final var newFurnitureNameList =
            Arrays.stream(furnitureListString.split(Renovator.FURNITURE_NAME_SEPARATOR))
                .map(String::trim)
                .filter(fn -> fn.equals(name))
                .toList();
        final var newFurnitureListString =
            String.join(Renovator.FURNITURE_NAME_SEPARATOR, newFurnitureNameList);
        config.set(Config.Key.FURNITURE_LIST, newFurnitureListString);
        context.getConfig().saveToFile();

        // Restart the chamber
        context.getChamber().restart();

        return CommandLine.ExitCode.OK;
    }
}
