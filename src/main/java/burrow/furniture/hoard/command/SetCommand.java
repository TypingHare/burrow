package burrow.furniture.hoard.command;

import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import burrow.furniture.hoard.HoardFurniture;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.util.Map;

@CommandLine.Command(name = "set", description = "Set a property for a specific entry.")
@CommandType(HoardFurniture.COMMAND_TYPE)
public class SetCommand extends Command {
    @CommandLine.Parameters(index = "0", description = "The ID of the entry to update.")
    private Integer id;

    @CommandLine.Parameters(index = "1", description = "The key to set.")
    private String key;

    @CommandLine.Parameters(index = "2", description = "The value to set.")
    private String value;

    public SetCommand(@NotNull final CommandContext commandContext) {
        super(commandContext);
    }

    @Override
    public Integer call() {
        final var hoardFurniture = use(HoardFurniture.class);
        final var entry = hoardFurniture.getHoard().setProperties(id, Map.of(key, value));
        buffer.append(hoardFurniture.entryToString(entry, commandContext));

        return CommandLine.ExitCode.OK;
    }
}
