package burrow.furniture.entry;

import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import org.springframework.lang.NonNull;
import picocli.CommandLine;

import java.util.Map;

@CommandLine.Command(name = "set", description = "Set a property for a specific entry.")
@CommandType(EntryFurniture.COMMAND_TYPE)
public class SetCommand extends Command {
    @CommandLine.Parameters(index = "0", description = "The ID of the entry to update.")
    private Integer id;

    @CommandLine.Parameters(index = "1", description = "The key to set.")
    private String key;

    @CommandLine.Parameters(index = "2", description = "The value to set.")
    private String value;

    public SetCommand(@NonNull final CommandContext commandContext) {
        super(commandContext);
    }

    @Override
    public Integer call() {
        final var hoard = context.getHoard();
        final var entry = hoard.setProperties(id, Map.of(key, value));
        buffer.append(EntryFurniture.entryToString(context, entry));

        return CommandLine.ExitCode.OK;
    }
}
