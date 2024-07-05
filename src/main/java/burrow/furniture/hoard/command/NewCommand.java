package burrow.furniture.hoard.command;

import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import burrow.furniture.hoard.HoardFurniture;
import org.springframework.lang.NonNull;
import picocli.CommandLine;

import java.util.HashMap;

@CommandLine.Command(name = "new", description = "Create a new entry.")
@CommandType(HoardFurniture.COMMAND_TYPE)
public class NewCommand extends Command {
    @CommandLine.Parameters(arity = "0..*")
    private String[] params;

    public NewCommand(@NonNull final CommandContext commandContext) {
        super(commandContext);
    }

    @Override
    public Integer call() {
        if (params != null && params.length % 2 == 1) {
            buffer.append("Invalid number of arguments: ").append(params.length);
            return CommandLine.ExitCode.USAGE;
        }

        final var properties = new HashMap<String, String>();
        if (params != null) {
            for (int i = 0; i < params.length / 2; ++i) {
                properties.put(params[i * 2], params[i * 2 + 1]);
            }
        }

        final var hoardFurniture = use(HoardFurniture.class);
        final var entry = hoardFurniture.getHoard().create(properties);
        buffer.append(hoardFurniture.entryToString(entry, commandContext));

        return CommandLine.ExitCode.OK;
    }
}