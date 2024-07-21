package burrow.furniture.time;

import burrow.core.chamber.Chamber;
import burrow.core.common.Values;
import burrow.core.config.Config;
import burrow.core.furniture.BurrowFurniture;
import burrow.core.furniture.Furniture;
import burrow.furniture.hoard.Entry;
import burrow.furniture.hoard.HoardFurniture;
import burrow.furniture.hoard.chain.CreateEntryContext;
import burrow.furniture.hoard.chain.SetEntryContext;
import burrow.furniture.hoard.chain.ToFormattedObjectContext;
import burrow.furniture.hoard.chain.UnsetEntryContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;

@BurrowFurniture(
    simpleName = "time",
    description = "Add creation time and modification time of each entry.",
    type = BurrowFurniture.Type.COMPONENT,
    dependencies = {
        HoardFurniture.class
    }
)
public final class TimeFurniture extends Furniture {
    public static final String DEFAULT_TIME_FORMAT = "MM/dd, yyyy";

    public TimeFurniture(@NotNull final Chamber chamber) {
        super(chamber);
    }

    @Override
    public Collection<String> configKeys() {
        return List.of(
            ConfigKey.TIME_CREATED_AT_ENABLED,
            ConfigKey.TIME_UPDATED_AT_ENABLED,
            ConfigKey.TIME_CREATED_AT_FORMAT,
            ConfigKey.TIME_UPDATED_AT_FORMAT
        );
    }

    @Override
    public void initializeConfig(@NotNull final Config config) {
        config.setIfAbsent(ConfigKey.TIME_CREATED_AT_ENABLED, Values.Bool.TRUE);
        config.setIfAbsent(ConfigKey.TIME_UPDATED_AT_ENABLED, Values.Bool.TRUE);
        config.setIfAbsent(ConfigKey.TIME_CREATED_AT_FORMAT, DEFAULT_TIME_FORMAT);
        config.setIfAbsent(ConfigKey.TIME_UPDATED_AT_FORMAT, DEFAULT_TIME_FORMAT);
    }

    @Override
    public void initialize() {
        final var hoard = use(HoardFurniture.class).getHoard();
        hoard.getCreateEntryChain().use(this::createEntry);
        hoard.getSetEntryChain().use(this::setEntry);
        hoard.getUnsetEntryChain().use(this::unsetEntry);
        hoard.getToFormattedObjectChain().use(this::toFormattedObject);
    }

    public void createEntry(
        @NotNull final CreateEntryContext context,
        @Nullable final Runnable next
    ) {
        final var entry = context.getEntry();
        setCreateTime(entry);
        setUpdateTime(entry);
    }

    public void toFormattedObject(
        @NotNull final ToFormattedObjectContext context,
        @Nullable final Runnable next
    ) {
        final var entry = context.getEntry();
        final var formattedObject = context.getFormattedObject();
        if (isCreatedAtEnabled()) {
            final var dateString = getDateString(entry.getNotNull(EntryKey.CREATED_AT));
            formattedObject.put(EntryKey.CREATED_AT, dateString);
        }

        if (isUpdatedAtEnabled()) {
            final var dateString = getDateString(entry.getNotNull(EntryKey.UPDATED_AT));
            formattedObject.put(EntryKey.UPDATED_AT, dateString);
        }
    }

    public void setEntry(@NotNull final SetEntryContext context, @Nullable final Runnable next) {
        setUpdateTime(context.getEntry());
    }

    public void unsetEntry(
        @NotNull final UnsetEntryContext context,
        @Nullable final Runnable next
    ) {
        setUpdateTime(context.getEntry());
    }

    public boolean isUpdatedAtEnabled() {
        return Values.Bool.isTrue(getConfig().get(ConfigKey.TIME_CREATED_AT_ENABLED));
    }

    public boolean isCreatedAtEnabled() {
        return Values.Bool.isTrue(getConfig().get(ConfigKey.TIME_CREATED_AT_ENABLED));
    }

    public void setCreateTime(@NotNull final Entry entry) {
        if (isCreatedAtEnabled()) {
            entry.set(EntryKey.CREATED_AT, System.currentTimeMillis());
        }
    }

    public void setUpdateTime(@NotNull final Entry entry) {
        if (isUpdatedAtEnabled()) {
            entry.set(EntryKey.UPDATED_AT, System.currentTimeMillis());
        }
    }

    @NotNull
    public String dateToString(final long timestampMs) {
        final var format = getConfig().getNotNull(ConfigKey.TIME_CREATED_AT_FORMAT);
        final var formatter = DateTimeFormatter.ofPattern(format);
        final var dateTime
            = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestampMs), ZoneId.systemDefault());

        return dateTime.format(formatter);
    }

    public String getDateString(@NotNull final String value) {
        final var timestampMs = Long.parseLong(value);
        return dateToString(timestampMs);
    }

    public static final class EntryKey {
        public static final String CREATED_AT = "created_at";
        public static final String UPDATED_AT = "updated_at";
    }

    public static final class ConfigKey {
        public static final String TIME_CREATED_AT_ENABLED = "time.created_at.enabled";
        public static final String TIME_UPDATED_AT_ENABLED = "time.updated_at.enabled";
        public static final String TIME_CREATED_AT_FORMAT = "time.created_at.format";
        public static final String TIME_UPDATED_AT_FORMAT = "time.updated_at.format";
    }
}
