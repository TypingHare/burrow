package burrow.furniture.hoard.command;

import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import burrow.furniture.hoard.HoardFurniture;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

@CommandLine.Command(name = "del", description = "Delete an entry.")
@CommandType(HoardFurniture.COMMAND_TYPE)
public class DeleteCommand extends Command {
    @CommandLine.Parameters(index = "0", description = "The ID of the entry to delete.")
    private Integer id;

    public DeleteCommand(@NotNull final CommandContext commandContext) {
        super(commandContext);
    }

    @Override
    public Integer call() {
        final var hoardFurniture = use(HoardFurniture.class);
        final var entry = hoardFurniture.getHoard().delete(id);
        buffer.append(hoardFurniture.entryToString(entry, commandContext));

        return CommandLine.ExitCode.OK;
    }
}
