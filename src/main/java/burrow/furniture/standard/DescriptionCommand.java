package burrow.furniture.standard;

import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import burrow.core.common.ColorUtility;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

@CommandLine.Command(
    name = "desc",
    description = "Retrieve or update the current chamber's description."
)
@CommandType(StandardFurniture.COMMAND_TYPE)
public class DescriptionCommand extends Command {
    @CommandLine.Parameters(
        index = "0",
        paramLabel = "<new-description>",
        description = "The new description to update.",
        defaultValue = CommandLine.Option.NULL_VALUE
    )
    private String newDescription;

    public DescriptionCommand(@NotNull final CommandContext commandContext) {
        super(commandContext);
    }

    @Override
    public Integer call() {
        final var standardFurniture = use(StandardFurniture.class);
        if (newDescription == null) {
            final var description = standardFurniture.getChamberDescription();
            buffer.append(ColorUtility.render(description, ColorUtility.Type.DESCRIPTION));
        } else {
            standardFurniture.updateChamberDescription(newDescription);
        }

        return CommandLine.ExitCode.OK;
    }
}
