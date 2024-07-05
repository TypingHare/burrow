package burrow.furniture.dictator;

import burrow.core.chamber.ChamberInitializationException;
import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import org.springframework.lang.NonNull;
import picocli.CommandLine;

@CommandLine.Command(name = "start", description = "Start a chamber.")
@CommandType(DictatorFurniture.COMMAND_TYPE)
public class StartCommand extends Command {
    @CommandLine.Parameters(index = "0", description = "The name of the chamber to start.")
    private String name;

    public StartCommand(@NonNull final CommandContext commandContext) {
        super(commandContext);
    }

    @Override
    public Integer call() throws ChamberInitializationException {
        use(DictatorFurniture.class).start(name);

        return CommandLine.ExitCode.OK;
    }
}
