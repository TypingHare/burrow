package burrow.furniture.entry;

import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import burrow.core.common.ColorUtility;
import burrow.core.common.Values;
import org.springframework.lang.NonNull;
import picocli.CommandLine;

@CommandLine.Command(
    name = "exist",
    description = "Check if an entry exists."
)
@CommandType(EntryFurniture.COMMAND_TYPE)
public class ExistCommand extends Command {
    @CommandLine.Parameters(index = "0", description = "The id of the entry.")
    private Integer id;

    public ExistCommand(@NonNull final CommandContext commandContext) {
        super(commandContext);
    }

    @Override
    public Integer call() {
        final var isExist = context.getHoard().exist(id);
        final var string = Values.Bool.stringify(isExist);

        buffer.append(ColorUtility.render(string, ColorUtility.Type.KEYWORD));

        return CommandLine.ExitCode.OK;
    }
}
