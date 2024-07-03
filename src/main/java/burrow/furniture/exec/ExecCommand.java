package burrow.furniture.exec;

import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import org.springframework.lang.NonNull;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(
    name = "exec",
    description = "Take the first argument as a command to execute."
)
@CommandType(ExecFurniture.COMMAND_TYPE)
public class ExecCommand extends Command {
    @CommandLine.Parameters(index = "0", description = "The command to execute.")
    private String command;

    public ExecCommand(@NonNull final CommandContext commandContext) {
        super(commandContext);
    }

    @Override
    public Integer call() throws IOException, InterruptedException {
        useFurniture(ExecFurniture.class).execute(commandContext, command);

        return CommandLine.ExitCode.OK;
    }
}
