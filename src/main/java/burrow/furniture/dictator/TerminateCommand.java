package burrow.furniture.dictator;

import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import burrow.furniture.standard.StandardFurniture;
import org.springframework.lang.NonNull;
import picocli.CommandLine;

@CommandLine.Command(
    name = "terminate",
    description = "Terminate a chamber."
)
@CommandType(StandardFurniture.COMMAND_TYPE)
public class TerminateCommand extends Command {
    @CommandLine.Parameters(index = "0", description = "The name of the chamber to terminate.")
    private String name;

    public TerminateCommand(@NonNull final CommandContext commandContext) {
        super(commandContext);
    }

    @Override
    public Integer call() {
        DictatorFurniture.terminate(context, name);
        buffer.append("Terminated: ").append(name);

        return CommandLine.ExitCode.OK;
    }
}
