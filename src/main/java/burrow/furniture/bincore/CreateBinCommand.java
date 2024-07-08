package burrow.furniture.bincore;

import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import org.springframework.lang.NonNull;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(name = "create-bin", description = "Create a bin file.")
@CommandType(BinCoreFurniture.COMMAND_TYPE)
public class CreateBinCommand extends Command {
    public CreateBinCommand(@NonNull final CommandContext commandContext) {
        super(commandContext);
    }

    @Override
    public Integer call() throws IOException {
        use(BinCoreFurniture.class).createBinFile();

        return CommandLine.ExitCode.OK;
    }
}
