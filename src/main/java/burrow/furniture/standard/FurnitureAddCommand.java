package burrow.furniture.standard;

import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import burrow.core.config.Config;
import burrow.core.furniture.Renovator;
import picocli.CommandLine;

import java.util.ArrayList;

@CommandLine.Command(name = "f.add", description = "Add a furniture for the current chamber.")
@CommandType(StandardFurniture.COMMAND_TYPE)
public class FurnitureAddCommand extends Command {
    @CommandLine.Parameters(
        index = "0",
        paramLabel = "<name>",
        description = "The full name of the furniture to add.")
    private String name;

    public FurnitureAddCommand(final CommandContext commandContext) {
        super(commandContext);
    }

    @Override
    public Integer call() {
        // Checks if the furniture already exist
        final var standardFurniture = use(StandardFurniture.class);
        final var configNameList = standardFurniture.getConfigFurnitureNameList();
        if (configNameList.contains(name)) {
            buffer.append("Furniture already exists: ").append(name);
            return CommandLine.ExitCode.SOFTWARE;
        }

        // Add the furniture to the list and update the config file
        final var config = getConfig();
        final var newNameList = new ArrayList<>(configNameList);
        newNameList.add(name);
        final var furnitureListString = config.getNonNull(Config.Key.CHAMBER_FURNITURE_LIST);
        final var newFurnitureListString =
            String.join(Renovator.FURNITURE_NAME_SEPARATOR, newNameList);
        config.set(Config.Key.CHAMBER_FURNITURE_LIST, newFurnitureListString);
        config.saveToFile();

        // Restart the chamber
        try {
            getChamber().restart();
        } catch (final Throwable ex) {
            buffer.append("Fail to add the furniture due to a failure of restarting.\n");
            bufferAppendThrowable(ex);

            // Restore to the former furniture list and update the config file
            config.set(Config.Key.CHAMBER_FURNITURE_LIST, furnitureListString);
            config.saveToFile();

            return CommandLine.ExitCode.SOFTWARE;
        }

        return CommandLine.ExitCode.OK;
    }
}