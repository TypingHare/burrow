package burrow.furniture.hoard.command;

import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import burrow.furniture.hoard.HoardFurniture;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

@CommandLine.Command(
    name = "update-prop",
    description = "Update the name of a property for all entries."
)
@CommandType(HoardFurniture.COMMAND_TYPE)
public class UpdatePropCommand extends Command {
    @CommandLine.Parameters(
        index = "0",
        description = "The property name to update."
    )
    private String prop;

    @CommandLine.Parameters(
        index = "1",
        paramLabel = "<new-prop>",
        description = "The new property name."
    )
    private String newProp;

    public UpdatePropCommand(@NotNull final CommandContext commandContext) {
        super(commandContext);
    }

    @Override
    public Integer call() {
        final var hoardFurniture = use(HoardFurniture.class);
        hoardFurniture.changePropertyName(prop, newProp);

        return CommandLine.ExitCode.OK;
    }
}
