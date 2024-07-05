package burrow.furniture.standard;

import burrow.core.chamber.ChamberInitializationException;
import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import burrow.core.common.ColorUtility;
import burrow.core.common.Values;
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
        final var standardFurniture = use(StandardFurniture.class);
        if (value == null) {
            final var value = standardFurniture.retrieveConfigItem(key);
            if (value == null) {
                buffer.append(ColorUtility.render(Values.NULL, ColorUtility.Type.NULL));
            } else {
                buffer.append(ColorUtility.render(value, ColorUtility.Type.VALUE));
            }

            getChamber().restart();
        } else {
            standardFurniture.updateConfigItem(key, value);
        }

        return CommandLine.ExitCode.OK;
    }
}