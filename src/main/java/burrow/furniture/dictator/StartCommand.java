package burrow.furniture.dictator;

import burrow.core.chamber.ChamberInitializationException;
import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(name = "start", description = "Start a chamber.")
@CommandType(DictatorFurniture.COMMAND_TYPE)
public class StartCommand extends Command {
    @CommandLine.Parameters(index = "0", description = "The name of the chamber to start.")
    private String name;

    public StartCommand(@NotNull final CommandContext commandContext) {
        super(commandContext);
    }

    @Override
    public Integer call() throws ChamberInitializationException, IOException {
        final var dictatorFurniture = use(DictatorFurniture.class);
        final var chamberNameList = dictatorFurniture.getAvailableChamberList();
        if (!chamberNameList.contains(name)) {
            buffer.append("Chamber directory not found: ").append(name);
            return CommandLine.ExitCode.USAGE;
        }

        dictatorFurniture.start(name);

        return CommandLine.ExitCode.OK;
    }
}
