package burrow.furniture.standard;

import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import burrow.core.common.ColorUtility;
import burrow.core.furniture.exception.AmbiguousSimpleNameException;
import burrow.core.furniture.exception.FurnitureNotFoundException;
import org.springframework.lang.NonNull;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@CommandLine.Command(name = "clist", description = "Display a list of all configuration items.")
@CommandType(StandardFurniture.COMMAND_TYPE)
public class ConfigListCommand extends Command {
    public static final String KEY_VALUE_SEPARATOR = " -> ";

    // The --furniture option allows specifying a furniture name to filter configuration items.
    // If not provided, all configuration items will be displayed.
    @CommandLine.Option(
        names = {"-f", "--furniture"},
        description = "Filter configuration items by furniture name.",
        defaultValue = CommandLine.Option.NULL_VALUE)
    private String furnitureName;

    public ConfigListCommand(final CommandContext commandContext) {
        super(commandContext);
    }

    @Override
    public Integer call() throws FurnitureNotFoundException, AmbiguousSimpleNameException {
        final var standardFurniture = use(StandardFurniture.class);
        final var configStore = getConfig().getStore();
        final var configKeys = furnitureName == null ? configStore.keySet() :
            standardFurniture.getConfigKeys(furnitureName);
        final List<Map.Entry<String, String>> configToPrint =
            configStore.entrySet().stream()
                .filter(entry -> configKeys.contains(entry.getKey()))
                .sorted(Map.Entry.comparingByKey())
                .toList();

        final var lines = new ArrayList<String>();
        final var entries = configToPrint.stream().sorted(Map.Entry.comparingByKey()).toList();
        for (final var entry : entries) {
            lines.add(getColoredLine(entry.getKey(), entry.getValue()));
        }

        bufferAppendLines(lines);

        return CommandLine.ExitCode.OK;
    }

    @NonNull
    private String getColoredLine(@NonNull final String key, @NonNull final String value) {
        return ColorUtility.render(key, ColorUtility.Type.KEY)
            + ColorUtility.render(KEY_VALUE_SEPARATOR, ColorUtility.Type.SYMBOL)
            + ColorUtility.render("\"" + value + "\"", ColorUtility.Type.VALUE);
    }
}
