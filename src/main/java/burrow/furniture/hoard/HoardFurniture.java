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
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.ArrayList;

@BurrowFurniture(
    simpleName = "Hoard",
    description = "Entries."
)
public class HoardFurniture extends Furniture {
    public static final String COMMAND_TYPE = "Hoard";

    private final Hoard hoard = new Hoard(chamber);

    public HoardFurniture(@NonNull final Chamber chamber) {
        super(chamber);
    }

    public static void toFormattedObject(
        @NonNull final ToFormattedObjectContext context,
        @Nullable final Runnable next
    ) {
        final var entry = ToFormattedObjectContext.Hook.entry.getNonNull(context);
        final var formattedObject =
            ToFormattedObjectContext.Hook.formattedObject.getNonNull(context);
        formattedObject.putAll(entry.getProperties());

        Chain.runIfNotNull(next);
    }

    public static void formattedObjectToString(
        @NonNull final FormattedObjectToStringContext context,
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
                FormattedObjectToStringContext.Hook.environment.getNonNull(context);
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

        hoard.getToFormattedObjectChain().use(HoardFurniture::toFormattedObject);
        hoard.getFormattedObjectToStringChain().use(HoardFurniture::formattedObjectToString);
    }

    @NonNull
    public Hoard getHoard() {
        return hoard;
    }

    @Override
    public void afterInitialization() {
        hoard.loadFromFile(hoard.getHoardFilePath());
    }

    @Override
    public void terminate() {
        hoard.saveToFile(hoard.getHoardFilePath());
    }

    public void changePropertyName(
        @NonNull final String originalPropertyName,
        @NonNull final String newPropertyName
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

    @NonNull
    public String entryToString(
        @NonNull final Entry entry,
        @NonNull final CommandContext commandContext
    ) {
        final var environment = CommandContext.Hook.environment.getNonNull(commandContext);
        return getHoard().entryToString(entry, environment);
    }
}
