package burrow.furniture.dictator;

import burrow.core.Burrow;
import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import burrow.core.common.ColorUtility;
import burrow.core.config.Config;
import com.google.common.base.Strings;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.io.IOException;
import java.util.ArrayList;

@CommandLine.Command(
    name = "rlist",
    description = "Display all chambers' names and descriptions.")
@CommandType(DictatorFurniture.COMMAND_TYPE)
public class ChamberListCommand extends Command {
    public ChamberListCommand(@NotNull final CommandContext commandContext) {
        super(commandContext);
    }

    @Override
    public Integer call() throws IOException {
        final var chamberNameList = use(DictatorFurniture.class).getAvailableChamberList();
        final var lines = new ArrayList<String>();
        for (final var chamberName : chamberNameList) {
            final var configFile = Burrow.CHAMBERS_ROOT_DIR
                .resolve(chamberName)
                .resolve(Config.CONFIG_FILE_NAME);
            final var configMap = Config.loadFromConfigFile(configFile);
            final var description = configMap.get(Config.Key.CHAMBER_DESCRIPTION);
            lines.add(
                ColorUtility.render(Strings.padEnd(chamberName, 16, ' '), ColorUtility.Type.NAME_CHAMBER)
                    + ColorUtility.render(description, ColorUtility.Type.DESCRIPTION));
        }

        bufferAppendLines(lines);

        return CommandLine.ExitCode.OK;
    }
}