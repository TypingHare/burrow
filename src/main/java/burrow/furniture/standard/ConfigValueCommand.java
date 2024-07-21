package burrow.furniture.standard;

import burrow.core.chamber.ChamberInitializationException;
import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import burrow.core.common.ColorUtility;
import burrow.core.common.Values;
import picocli.CommandLine;

@CommandLine.Command(
    name = "c.value",
    description = "Retrieve the value of a configuration entry.")
@CommandType(StandardFurniture.COMMAND_TYPE)
public class ConfigValueCommand extends Command {
    @CommandLine.Parameters(index = "0", description = "The key of the entry to retrieve.")
    private String key;

    public ConfigValueCommand(final CommandContext commandContext) {
        super(commandContext);
    }

    @Override
    public Integer call() throws ChamberInitializationException {
        final var standardFurniture = use(StandardFurniture.class);
        final var value = standardFurniture.retrieveConfigItem(key);
        if (value == null) {
            buffer.append(ColorUtility.render(Values.NULL, ColorUtility.Type.NULL));
        } else {
            buffer.append(ColorUtility.render(value, ColorUtility.Type.VALUE));
        }

        return CommandLine.ExitCode.OK;
    }
}