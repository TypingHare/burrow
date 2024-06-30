package burrow.furniture.dictator;

import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import org.springframework.lang.NonNull;
import picocli.CommandLine;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@CommandLine.Command(
    name = "rinfo",
    description = "Display chamber info of a specific chamber."
)
public class ChamberInfoCommand extends Command {
    @CommandLine.Parameters(
        index = "0",
        description = "The name of the chamber.",
        defaultValue = CommandLine.Option.NULL_VALUE
    )
    private String name;

    public ChamberInfoCommand(@NonNull final CommandContext commandContext) {
        super(commandContext);
    }

    @Override
    public Integer call() {
        final var chamberInfoMap = getFurniture(DictatorFurniture.class).getChamberInfoMap();
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

    @NonNull
    public static String getChamberInfoString(
        @NonNull final String chamberName,
        @NonNull final ChamberInfo chamberInfo
    ) {
        final var initiateTimestampMs = chamberInfo.getInitiateTimestampMs();
        final var lastRequestTimestampMs = chamberInfo.getLastRequestTimestampMs();

        return String.format("<%s>(initiated at %s, last requested at %s)",
            chamberName,
            timestempToString(initiateTimestampMs),
            timestempToString(lastRequestTimestampMs)
        );
    }

    public static String timestempToString(@NonNull final Long timestampMs) {
        final var formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        final var dateTime
            = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestampMs), ZoneId.systemDefault());
        return dateTime.format(formatter);
    }
}
