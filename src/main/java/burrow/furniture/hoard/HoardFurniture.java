package burrow.furniture.hoard;

import burrow.chain.Chain;
import burrow.core.chamber.Chamber;
import burrow.core.command.CommandContext;
import burrow.core.common.ColorUtility;
import burrow.core.common.Values;
import burrow.core.furniture.BurrowFurniture;
import burrow.core.furniture.Furniture;
import burrow.furniture.hoard.chain.FormattedObjectToStringContext;
import burrow.furniture.hoard.chain.ToFormattedObjectContext;
import burrow.furniture.hoard.command.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;

@BurrowFurniture(
    simpleName = "Hoard",
    description = "Simple local storage support.",
    type = BurrowFurniture.Type.COMPONENT
)
public class HoardFurniture extends Furniture {
    public static final String COMMAND_TYPE = "Hoard";

    private final Hoard hoard = new Hoard(chamber);
    private boolean saveHoardWhenTerminate = true;

    public HoardFurniture(@NotNull final Chamber chamber) {
        super(chamber);
    }

    public void setSaveHoardWhenTerminate(final boolean saveHoardWhenTerminate) {
        this.saveHoardWhenTerminate = saveHoardWhenTerminate;
    }

    public void toFormattedObject(
        @NotNull final ToFormattedObjectContext context,
        @Nullable final Runnable next
    ) {
        final var entry = ToFormattedObjectContext.Hook.entry.getNotNull(context);
        final var formattedObject =
            ToFormattedObjectContext.Hook.formattedObject.getNotNull(context);
        formattedObject.putAll(entry.getProperties());

        Chain.runIfNotNull(next);
    }

    public void formattedObjectToString(
        @NotNull final FormattedObjectToStringContext context,
        @Nullable final Runnable next
    ) {
        final var id = FormattedObjectToStringContext.Hook.id.get(context);
        final var formattedObject =
            FormattedObjectToStringContext.Hook.formattedObject.get(context);
        final var resultHook = FormattedObjectToStringContext.Hook.result;

        if (formattedObject == null) {
            resultHook.set(context, ColorUtility.render(Values.NULL, ColorUtility.Type.NULL));
        } else {
            final var entrySet = formattedObject.entrySet();
            if (entrySet.isEmpty()) {
                resultHook.set(context, "[" + id + "] {}");
                Chain.runIfNotNull(next);
                return;
            }

            final var environment =
                FormattedObjectToStringContext.Hook.environment.getNotNull(context);
            final var getConsoleWidth = environment.getConsoleWidth();

            final var itemList = new ArrayList<String>();
            final var coloredItemList = new ArrayList<String>();
            for (final var entry : entrySet) {
                final var key = entry.getKey();
                final var value = entry.getValue();
                final var coloredKey = ColorUtility.render(entry.getKey(), ColorUtility.Type.KEY);
                final var coloredValue =
                    ColorUtility.render(entry.getValue(), ColorUtility.Type.VALUE);
                itemList.add(key + ": \"" + value + "\"");
                coloredItemList.add(coloredKey + ": \"" + coloredValue + "\"");
            }
            final var oneLine = "[" + id + "] { " + String.join(", ", itemList) + " }";
            if (oneLine.length() <= Math.floor(getConsoleWidth * 0.75)) {
                final var coloredOneLine =
                    "[" + id + "] { " + String.join(", ", coloredItemList) + " }";
                resultHook.set(context, coloredOneLine);
            } else {
                final var coloredMultipleLine =
                    "[" + id + "] {\n  " + String.join(",\n  ", coloredItemList) + "\n}";
                resultHook.set(context, String.join("\n", coloredMultipleLine));
            }
        }

        Chain.runIfNotNull(next);
    }

    @Override
    public void beforeInitialization() {
        registerCommand(EntryCommand.class);
        registerCommand(NewCommand.class);
        registerCommand(DeleteCommand.class);
        registerCommand(ExistCommand.class);
        registerCommand(CountCommand.class);
        registerCommand(EntriesCommand.class);
        registerCommand(PropCommand.class);
        registerCommand(SetCommand.class);
        registerCommand(UnsetCommand.class);
        registerCommand(UpdatePropCommand.class);
        registerCommand(BackCommand.class);
        registerCommand(BackListCommand.class);
        registerCommand(BackRestoreCommand.class);

        hoard.getToFormattedObjectChain().use(this::toFormattedObject);
        hoard.getFormattedObjectToStringChain().use(this::formattedObjectToString);
    }

    @NotNull
    public Hoard getHoard() {
        return hoard;
    }

    @Override
    public void afterInitialization() {
        hoard.loadFromFile(hoard.getHoardFilePath());
    }

    @Override
    public void terminate() {
        if (saveHoardWhenTerminate) {
            hoard.saveToFile(hoard.getHoardFilePath());
        }
    }

    public void changePropertyName(
        @NotNull final String originalPropertyName,
        @NotNull final String newPropertyName
    ) {
        hoard.getEntryList().forEach(entry -> {
            final var value = entry.getOrDefault(originalPropertyName, null);
            if (value == null) {
                return;
            }

            entry.set(newPropertyName, value);
            entry.unset(originalPropertyName);
        });
    }

    @NotNull
    public String entryToString(
        @NotNull final Entry entry,
        @NotNull final CommandContext commandContext
    ) {
        final var environment = CommandContext.Hook.environment.getNotNull(commandContext);
        return getHoard().entryToString(entry, environment);
    }

    public static @NotNull String getInstantDateString() {
        final var now = Instant.now();
        final var zonedDateTime = now.atZone(ZoneId.systemDefault());
        final var year = zonedDateTime.getYear();
        final var month = zonedDateTime.getMonthValue();
        final var day = zonedDateTime.getDayOfMonth();
        final var hour = zonedDateTime.getHour();
        final var minute = zonedDateTime.getMinute();
        final var second = zonedDateTime.getSecond();

        final var yearStr = String.valueOf(year);
        final var monthStr = month >= 10 ? String.valueOf(month) : "0" + month;
        final var dayStr = day >= 10 ? String.valueOf(day) : "0" + day;
        final var hourStr = hour >= 10 ? String.valueOf(hour) : "0" + hour;
        final var minuteStr = minute >= 10 ? String.valueOf(minute) : "0" + minute;
        final var secondStr = second >= 10 ? String.valueOf(second) : "0" + second;

        return yearStr + monthStr + dayStr + hourStr + minuteStr + secondStr;
    }

    public @NotNull String backup(@NotNull final String name) {
        final var dateString = getInstantDateString();
        final var fileName = String.format("hoard.%s.%s.backup.json", name, dateString);
        final var filePath = getChamberContext().getRootPath().resolve(fileName);
        hoard.saveToFile(filePath);

        return fileName;
    }

    public boolean restore(@NotNull final Path filePath) throws IOException {
        if (!filePath.toFile().exists()) {
            return false;
        }

        final var hoardFilePath = hoard.getHoardFilePath();
        Files.delete(hoardFilePath);
        Files.copy(filePath, hoardFilePath);

        return true;
    }

    public boolean deleteBackup(@NotNull final Path filePath) throws IOException {
        if (!filePath.toFile().exists()) {
            return false;
        }
        Files.delete(filePath);

        return true;
    }
}
