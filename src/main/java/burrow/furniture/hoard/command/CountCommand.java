package burrow.furniture.hoard.command;

import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import burrow.furniture.hoard.HoardFurniture;
import org.springframework.lang.NonNull;
import picocli.CommandLine;

@CommandLine.Command(
    name = "count",
    description = "Retrieve the count of entries."
)
@CommandType(HoardFurniture.COMMAND_TYPE)
public class CountCommand extends Command {
    public CountCommand(@NonNull final CommandContext commandContext) {
        super(commandContext);
    }

    @Override
    public Integer call() {
        final var count = use(HoardFurniture.class).getHoard().getSize();
        buffer.append(count);

        return CommandLine.ExitCode.OK;
    }
}
