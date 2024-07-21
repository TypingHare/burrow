package burrow.furniture.hoard.command;

import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import burrow.furniture.hoard.HoardFurniture;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.regex.Pattern;

@CommandLine.Command(
    name = "back.list",
    description = "Display the list of backup hoards."
)
@CommandType(HoardFurniture.COMMAND_TYPE)
public class BackListCommand extends Command {
    public BackListCommand(@NotNull final CommandContext commandContext) {
        super(commandContext);
    }

    @Override
    public Integer call() throws IOException {
        final var rootPath = getChamberContext().getRootPath();

        final var backupList = new ArrayList<String>();
        try (final var stream = Files.newDirectoryStream(rootPath)) {
            for (final var path : stream) {
                final var fileName = path.getFileName().toString();
                final var pattern = Pattern.compile("^hoard\\.(\\w*)\\.(\\w*).backup\\.json$");
                final var matcher = pattern.matcher(fileName);
                if (!matcher.find()) {
                    continue;
                }

                final var name = matcher.group(1);
                final var dateString = matcher.group(2);
                backupList.add(String.format("%s %s (%s)", dateString, name, fileName));
            }
        }

        final var sortedBackupList = backupList.stream().sorted().toList();
        final var lines = new ArrayList<String>();
        var i = 0;
        for (final var backup : sortedBackupList) {
            lines.add(String.format("[%d] %s", i++, backup));
        }

        bufferAppendLines(lines);

        return CommandLine.ExitCode.OK;
    }
}
