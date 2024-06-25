package burrow.furniture.standard;

import burrow.core.chamber.ChamberContext;
import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import org.springframework.lang.NonNull;
import picocli.CommandLine;

@CommandLine.Command(name = "root", description = "Display the root directory of the chamber.")
@CommandType(StandardFurniture.COMMAND_TYPE)
public class RootCommand extends Command {
    public RootCommand(final CommandContext commandContext) {
        super(commandContext);
    }

    @Override
    public Integer call() {
        buffer.append(getRootDirectoryAbsolutePath(context));
        return CommandLine.ExitCode.OK;
    }

    @NonNull
    public static String getRootDirectoryAbsolutePath(
        @NonNull final ChamberContext context
    ) {
        return context.getRootDir().toString();
    }
}