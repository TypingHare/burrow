package burrow.furniture.keyvalue;

import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import org.springframework.lang.NonNull;
import picocli.CommandLine;

@CommandLine.Command(
    name = "value-name",
    description = "Update the name of the value property."
)
@CommandType(KeyValueFurniture.COMMAND_TYPE)
public class ValueNameCommand extends Command {
    @CommandLine.Parameters(
        index = "0",
        paramLabel = "<value-name>",
        description = "The name of the value property.",
        defaultValue = CommandLine.Option.NULL_VALUE
    )
    private String valueName;

    public ValueNameCommand(@NonNull final CommandContext commandContext) {
        super(commandContext);
    }

    @Override
    public Integer call() {
        final var originalValueName = getFurniture(KeyValueFurniture.class).getValueName();

        if (valueName == null) {
            buffer.append(originalValueName);
            return CommandLine.ExitCode.OK;
        }

        KeyValueFurniture.changePropertyKey(context, originalValueName, valueName);

        return CommandLine.ExitCode.OK;
    }
}
