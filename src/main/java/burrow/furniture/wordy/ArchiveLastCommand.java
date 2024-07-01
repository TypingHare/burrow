package burrow.furniture.wordy;

import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import org.springframework.lang.NonNull;
import picocli.CommandLine;

@CommandLine.Command(name = "archive-last", description = "Display a random word.")
@CommandType(WordyFurniture.COMMAND_TYPE)
public class ArchiveLastCommand extends Command {
    public ArchiveLastCommand(@NonNull final CommandContext commandContext) {
        super(commandContext);
    }

    @Override
    public Integer call() {
        WordyFurniture.archiveLastWord(context);
        return CommandLine.ExitCode.OK;
    }
}
