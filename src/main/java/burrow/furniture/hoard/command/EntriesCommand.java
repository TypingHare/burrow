package burrow.furniture.hoard.command;

import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import burrow.furniture.hoard.HoardFurniture;
import burrow.furniture.standard.StandardFurniture;
import org.springframework.lang.NonNull;
import picocli.CommandLine;

import java.util.Arrays;
import java.util.List;

@CommandLine.Command(name = "entries", description = "Retrieve a list of entries.")
@CommandType(HoardFurniture.COMMAND_TYPE)
public class EntriesCommand extends Command {
    @CommandLine.Parameters(index = "0", description = "A list of IDs to retrieve.")
    private String idList;

    public EntriesCommand(@NonNull final CommandContext commandContext) {
        super(commandContext);
    }

    @Override
    public Integer call() {
        final var idListString = idList.trim();
        if (idListString.length() < 2) {
            buffer.append("Invalid id list string: ").append(idListString);
            return CommandLine.ExitCode.USAGE;
        }

        final var environment = CommandContext.Hook.environment.getNonNull(commandContext);
        final var hoard = use(HoardFurniture.class).getHoard();
        final var idList = idListString.substring(1, idListString.length() - 1).split(",");
        final List<String> entryStringList =
            Arrays.stream(idList)
                .map(Integer::parseInt)
                .map(hoard::get)
                .map(entry -> hoard.entryToString(entry, environment))
                .toList();

        buffer.append(StandardFurniture.stringListToString(commandContext, entryStringList));

        return CommandLine.ExitCode.OK;
    }
}
