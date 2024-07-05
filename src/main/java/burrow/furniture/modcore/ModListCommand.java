package burrow.furniture.modcore;

import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import org.springframework.lang.NonNull;
import picocli.CommandLine;

import java.util.ArrayList;

@CommandLine.Command(name = "modlist")
@CommandType(ModCoreFurniture.COMMAND_TYPE)
public class ModListCommand extends Command {
    public ModListCommand(@NonNull final CommandContext commandContext) {
        super(commandContext);
    }

    @Override
    public Integer call() {
        final var jarFilePathList =
            use(ModCoreFurniture.class).getPathClassLoaderMap().keySet().stream().toList();

        final var lines = new ArrayList<String>();
        for (int i = 0; i < jarFilePathList.size(); i++) {
            lines.add(String.format("[%s] %s", i, jarFilePathList.get(i)));
        }

        bufferAppendLines(lines);

        return CommandLine.ExitCode.OK;
    }
}
