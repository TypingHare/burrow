package burrow.furniture.script;

import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import burrow.core.common.ColorUtility;
import burrow.furniture.exec.ExecFurniture;
import burrow.furniture.keyvalue.KeyValueFurniture;
import org.springframework.lang.NonNull;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(
    name = "exec",
    description = "Execute a command"
)
@CommandType(ExecFurniture.COMMAND_TYPE)
public class ExecCommand extends Command {
    @CommandLine.Parameters(
        index = "0",
        description = "The label associated with the command."
    )
    private String label;

    public ExecCommand(@NonNull final CommandContext commandContext) {
        super(commandContext);
    }

    @Override
    public Integer call() throws IOException, InterruptedException {
        final var valueList = useFurniture(KeyValueFurniture.class).getValueListByKey(label);
        if (valueList.isEmpty()) {
            final var errorMessage = "No command associated with such label: " + label;
            buffer.append(ColorUtility.render(errorMessage, ColorUtility.Type.MESSAGE_ERROR));
            return CommandLine.ExitCode.USAGE;
        }
        final var command = valueList.getFirst();
        useFurniture(ExecFurniture.class).execute(commandContext, command);

        return CommandLine.ExitCode.OK;
    }
}
