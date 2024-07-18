package burrow.furniture.standard;

import burrow.core.chamber.ChamberInitializationException;
import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.config.IllegalKeyException;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

@CommandLine.Command(
    name = "c.set",
    description = "Update the value of a configuration entry.")
public class ConfigSetCommand extends Command {
    @CommandLine.Parameters(index = "0", description = "The key of the item to set.")
    private String key;

    @CommandLine.Parameters(
        index = "1",
        description = "The new value."
    )
    private String value;

    public ConfigSetCommand(@NotNull final CommandContext commandContext) {
        super(commandContext);
    }

    @Override
    public Integer call() throws IllegalKeyException, ChamberInitializationException {
        use(StandardFurniture.class).updateConfigItem(key, value);
        getChamber().restart();

        return CommandLine.ExitCode.OK;
    }
}
