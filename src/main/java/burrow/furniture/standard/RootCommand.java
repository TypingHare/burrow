package burrow.furniture.standard;

import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import picocli.CommandLine;

@CommandLine.Command(name = "root", description = "Display the root directory of the chamber.")
@CommandType(StandardFurniture.COMMAND_TYPE)
public class RootCommand extends Command {
    public RootCommand(final CommandContext commandContext) {
        super(commandContext);
    }

    @Override
    public Integer call() {
        buffer.append(StandardFurniture.getRootDirectoryAbsolutePath(context));
        return CommandLine.ExitCode.OK;
    }
}