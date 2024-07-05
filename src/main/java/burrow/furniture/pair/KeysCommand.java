package burrow.furniture.pair;

import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import burrow.furniture.standard.StandardFurniture;
import org.springframework.lang.NonNull;
import picocli.CommandLine;

@CommandLine.Command(name = "keys", description = "Display all keys.")
@CommandType(PairFurniture.COMMAND_TYPE)
public class KeysCommand extends Command {
    public KeysCommand(@NonNull final CommandContext commandContext) {
        super(commandContext);
    }

    @Override
    public Integer call() {
        final var idSetStore = use(PairFurniture.class).getIdSetStore();
        final var keyList = idSetStore.keySet().stream().sorted().toList();
        buffer.append(StandardFurniture.stringListToString(commandContext, keyList));

        return CommandLine.ExitCode.OK;
    }
}
