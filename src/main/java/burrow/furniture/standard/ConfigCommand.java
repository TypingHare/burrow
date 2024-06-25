package burrow.furniture.standard;

import burrow.core.chamber.ChamberInitializationException;
import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import burrow.core.common.ColorUtility;
import picocli.CommandLine;

@CommandLine.Command(
    name = "c",
    description = "Retrieve or update the value of a configuration item.")
@CommandType(StandardFurniture.COMMAND_TYPE)
public class ConfigCommand extends Command {
    @CommandLine.Parameters(index = "0", description = "The key of the item to retrieve or update.")
    private String key;

    @CommandLine.Parameters(
        index = "1",
        description = "The value to be updated.",
        defaultValue = CommandLine.Option.NULL_VALUE)
    private String value;

    public ConfigCommand(final CommandContext commandContext) {
        super(commandContext);
    }

    @Override
    public Integer call() throws ChamberInitializationException {
        if (value == null) {
            final var value = StandardFurniture.retrieveConfigItem(context, key);
            if (value == null) {
                buffer.append(ColorUtility.render("null", ColorUtility.Type.NULL));
            } else {
                buffer.append(ColorUtility.render(value, ColorUtility.Type.VALUE));
            }

            context.getChamber().restart();
        } else {
            StandardFurniture.updateConfigItem(context, key, value);
        }

        return CommandLine.ExitCode.OK;
    }
}