package burrow.furniture.modcore;

import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import com.google.common.io.Files;
import org.springframework.lang.NonNull;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.ArrayList;

@CommandLine.Command(
    name = "modlist",
    description = "Display the mod list."
)
@CommandType(ModCoreFurniture.COMMAND_TYPE)
public final class ModListCommand extends Command {
    public ModListCommand(@NonNull final CommandContext commandContext) {
        super(commandContext);
    }

    @Override
    public Integer call() {
        final var modPathList = use(ModCoreFurniture.class).getModPathList();

        final var lines = new ArrayList<String>();
        for (int i = 0; i < modPathList.size(); i++) {
            lines.add(String.format("[%s] %s", i, getModPathLine(modPathList.get(i))));
        }

        bufferAppendLines(lines);

        return CommandLine.ExitCode.OK;
    }

    @NonNull
    public static String getModPathLine(@NonNull final Path modPath) {
        final var modName = Files.getNameWithoutExtension(modPath.getFileName().toString());
        return String.format("%s (%s)", modName, modPath);
    }
}
