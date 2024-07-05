package burrow.furniture.hoard.command;

import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import burrow.core.common.ColorUtility;
import burrow.core.common.Values;
import burrow.furniture.hoard.HoardFurniture;
import org.springframework.lang.NonNull;
import picocli.CommandLine;

@CommandLine.Command(
    name = "prop",
    description = "Retrieve the value of an entry property."
)
@CommandType(HoardFurniture.COMMAND_TYPE)
public class PropCommand extends Command {
    @CommandLine.Parameters(index = "0", description = "The ID of the entry to retrieve.")
    private Integer id;

    @CommandLine.Parameters(index = "1", description = "The key to retrieve.")
    private String key;

    public PropCommand(@NonNull final CommandContext commandContext) {
        super(commandContext);
    }

    @Override
    public Integer call() {
        final var hoardFurniture = use(HoardFurniture.class);
        final var entry = hoardFurniture.getHoard().get(id);
        final var value = entry.getOrDefault(key, null);

        if (value == null) {
            buffer.append(ColorUtility.render(Values.NULL, ColorUtility.Type.NULL));
        } else {
            buffer.append(ColorUtility.render(value, ColorUtility.Type.VALUE));
        }

        return CommandLine.ExitCode.OK;
    }
}