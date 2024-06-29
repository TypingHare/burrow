package burrow.furniture.entry;

import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import burrow.core.entry.Hoard;
import org.springframework.lang.NonNull;
import picocli.CommandLine;

import java.util.List;
import java.util.Objects;

@CommandLine.Command(name = "unset", description = "Unset a property for a specific entry.")
@CommandType(EntryFurniture.COMMAND_TYPE)
public class UnsetCommand extends Command {
    @CommandLine.Parameters(index = "0", description = "The ID of the entry to update.")
    private Integer id;

    @CommandLine.Parameters(index = "1", description = "The key to unset.")
    private String key;

    public UnsetCommand(@NonNull final CommandContext commandContext) {
        super(commandContext);
    }

    @Override
    public Integer call() {
        final var hoard = context.getHoard();
        final var entry = hoard.unsetProperties(id, List.of(key));

        if (Objects.equals(key, Hoard.KEY_ID)) {
            throw new RuntimeException("You cannot unset the ID property.");
        }

        buffer.append(EntryFurniture.entryToString(context, entry));

        return CommandLine.ExitCode.OK;
    }
}
