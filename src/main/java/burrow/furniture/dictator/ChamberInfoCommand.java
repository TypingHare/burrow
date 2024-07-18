package burrow.furniture.dictator;

import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import burrow.core.common.ColorUtility;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@CommandLine.Command(
    name = "r.info",
    description = "Display chamber info of a specific chamber."
)
@CommandType(DictatorFurniture.COMMAND_TYPE)
public class ChamberInfoCommand extends Command {
    @CommandLine.Parameters(
        index = "0",
        description = "The name of the chamber.",
        defaultValue = CommandLine.Option.NULL_VALUE
    )
    private String name;

    public ChamberInfoCommand(@NotNull final CommandContext commandContext) {
        super(commandContext);
    }

    @NotNull
    public static String getChamberInfoString(
        @NotNull final String chamberName,
        @NotNull final ChamberInfo chamberInfo
    ) {
        final var startTimestampMs = chamberInfo.getStartTimestampMs();
        final var lastRequestTimestampMs = chamberInfo.getLastRequestTimestampMs();

        return String.format("<%s> (started at %s, last requested at %s)",
            ColorUtility.render(chamberName, ColorUtility.Type.NAME_CHAMBER),
            timestempToString(startTimestampMs),
            timestempToString(lastRequestTimestampMs)
        );
    }

    public static String timestempToString(@NotNull final Long timestampMs) {
        final var formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        final var dateTime
            = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestampMs), ZoneId.systemDefault());
        return dateTime.format(formatter);
    }

    @Override
    public Integer call() {
        final var chamberInfoMap = use(DictatorFurniture.class).getChamberInfoMap();
        final var lines = new ArrayList<String>();
        int i = 0;
        for (final var entry : chamberInfoMap.entrySet()) {
            final var chamberName = entry.getKey();
            final var chamberInfo = entry.getValue();
            lines.add(String.format("[%d] %s", i++, getChamberInfoString(chamberName, chamberInfo)));
        }

        bufferAppendLines(lines);

        return CommandLine.ExitCode.OK;
    }
}
