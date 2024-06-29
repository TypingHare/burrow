package burrow.furniture.entry;

import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import org.springframework.lang.NonNull;
import picocli.CommandLine;

@CommandLine.Command(name = "del", description = "Delete an entry.")
@CommandType(EntryFurniture.COMMAND_TYPE)
public class DeleteCommand extends Command {
    @CommandLine.Parameters(index = "0", description = "The ID of the entry to delete.")
    private Integer id;

    public DeleteCommand(@NonNull final CommandContext commandContext) {
        super(commandContext);
    }

    @Override
    public Integer call() {
        final var entry = context.getHoard().delete(id);
        buffer.append(EntryFurniture.entryToString(context, entry));

        return CommandLine.ExitCode.OK;
    }
}
