package burrow.furniture.standard;

import burrow.core.chamber.ChamberInitializationException;
import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import burrow.core.config.Config;
import burrow.core.furniture.FurnitureNotFoundException;
import burrow.core.furniture.InvalidFurnitureClassException;
import burrow.core.furniture.Renovator;
import picocli.CommandLine;

import java.util.ArrayList;

@CommandLine.Command(name = "fadd", description = "Add a furniture for the current chamber.")
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
        final var configNameList = StandardFurniture.getConfigFurnitureNameList(context);
        if (configNameList.contains(name)) {
            buffer.append("Furniture already exists: ").append(name);
            return CommandLine.ExitCode.SOFTWARE;
        }

        // Checks if the furniture exists and is valid
        try {
            final var furnitureClass = Renovator.checkIfFurnitureExist(name);
            Renovator.testFurnitureClass(furnitureClass);
        } catch (final FurnitureNotFoundException | InvalidFurnitureClassException ex) {
            buffer.append(ex.getMessage());
            return CommandLine.ExitCode.SOFTWARE;
        }

        // Add the furniture to the list and update the config file
        final var newNameList = new ArrayList<>(configNameList);
        newNameList.add(name);
        final var furnitureListString = context.getConfig().get(Config.Key.FURNITURE_LIST);
        final var newFurnitureListString =
            String.join(Renovator.FURNITURE_NAME_SEPARATOR, newNameList);
        context.getConfig().set(Config.Key.FURNITURE_LIST, newFurnitureListString);
        context.getConfig().saveToFile();

        // Restart the chamber
        try {
            context.getChamber().restart();
        } catch (final ChamberInitializationException ex) {
            // Restore to the former furniture list and update the config file
            getContext().set(Config.Key.FURNITURE_LIST, furnitureListString);
            context.getConfig().saveToFile();
            buffer.append("Fail to add the furniture due to a failure of restarting.\n");
            bufferAppendThrowable(ex);

            return CommandLine.ExitCode.SOFTWARE;
        }

        return CommandLine.ExitCode.OK;
    }
}