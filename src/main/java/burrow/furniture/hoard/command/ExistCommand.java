package burrow.furniture.hoard.command;

import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import burrow.core.common.ColorUtility;
import burrow.core.common.Values;
import burrow.furniture.hoard.HoardFurniture;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

@CommandLine.Command(
    name = "exist",
    description = "Check if an entry exists."
)
@CommandType(HoardFurniture.COMMAND_TYPE)
public class ExistCommand extends Command {
    @CommandLine.Parameters(index = "0", description = "The id of the entry.")
    private Integer id;

    public ExistCommand(@NotNull final CommandContext commandContext) {
        super(commandContext);
    }

    @Override
    public Integer call() {
        final var hoardFurniture = use(HoardFurniture.class);
        final var isExist = hoardFurniture.getHoard().exist(id);
        final var string = Values.Bool.stringify(isExist);

        buffer.append(ColorUtility.render(string, ColorUtility.Type.KEYWORD));

        return CommandLine.ExitCode.OK;
    }
}
