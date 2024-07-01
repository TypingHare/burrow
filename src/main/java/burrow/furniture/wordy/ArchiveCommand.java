package burrow.furniture.wordy;

import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import org.springframework.lang.NonNull;
import picocli.CommandLine;

@CommandLine.Command(
    name = "archive",
    description = "Archive a word."
)
@CommandType(WordyFurniture.COMMAND_TYPE)
public class ArchiveCommand extends Command {
    @CommandLine.Parameters(index = "0", description = "The id of the word to delete.")
    private Integer id;

    public ArchiveCommand(@NonNull final CommandContext commandContext) {
        super(commandContext);
    }

    @Override
    public Integer call() {
        WordyFurniture.archive(context, context.getHoard().getById(id));

        return CommandLine.ExitCode.OK;
    }
}
