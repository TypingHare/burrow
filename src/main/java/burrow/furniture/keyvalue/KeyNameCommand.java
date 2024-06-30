package burrow.furniture.keyvalue;

import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import org.springframework.lang.NonNull;
import picocli.CommandLine;

@CommandLine.Command(
    name = "key-name",
    description = "Update the name of the key property."
)
@CommandType(KeyValueFurniture.COMMAND_TYPE)
public class KeyNameCommand extends Command {
    @CommandLine.Parameters(
        index = "0",
        paramLabel = "<key-name>",
        description = "The name of the key property.",
        defaultValue = CommandLine.Option.NULL_VALUE
    )
    private String keyName;

    public KeyNameCommand(@NonNull final CommandContext commandContext) {
        super(commandContext);
    }

    @Override
    public Integer call() {
        final var originalKeyName = getFurniture(KeyValueFurniture.class).getKeyName();

        if (keyName == null) {
            buffer.append(originalKeyName);
            return CommandLine.ExitCode.OK;
        }

        KeyValueFurniture.changePropertyKey(context, originalKeyName, keyName);

        return CommandLine.ExitCode.OK;
    }
}
