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
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;

@BurrowFurniture(
    simpleName = "time",
    description = "Add creation time and modification time of each entry.",
    dependencies = {
        HoardFurniture.class
    }
)
public final class TimeFurniture extends Furniture {
    public static final String DEFAULT_TIME_FORMAT = "MM/dd, yyyy";

    public TimeFurniture(@NonNull final Chamber chamber) {
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
    public void initializeConfig(@NonNull final Config config) {
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
        @NonNull final CreateEntryContext context,
        @Nullable final Runnable next
    ) {
        final var entry = CreateEntryContext.Hook.entry.getNonNull(context);
        setCreateTime(entry);
        setUpdateTime(entry);
    }

    public void toFormattedObject(
        @NonNull final ToFormattedObjectContext context,
        @Nullable final Runnable next
    ) {
        final var entry = ToFormattedObjectContext.Hook.entry.getNonNull(context);
        final var formattedObject =
            ToFormattedObjectContext.Hook.formattedObject.getNonNull(context);
        if (isCreatedAtEnabled()) {
            final var dateString = getDateString(entry.getNonNull(EntryKey.CREATED_AT));
            formattedObject.put(EntryKey.CREATED_AT, dateString);
        }

        if (isUpdatedAtEnabled()) {
            final var dateString = getDateString(entry.getNonNull(EntryKey.UPDATED_AT));
            formattedObject.put(EntryKey.UPDATED_AT, dateString);
        }
    }

    public void setEntry(@NonNull final SetEntryContext context, @Nullable final Runnable next) {
        final var entry = SetEntryContext.Hook.entry.getNonNull(context);
        setUpdateTime(entry);
    }

    public void unsetEntry(
        @NonNull final UnsetEntryContext context,
        @Nullable final Runnable next
    ) {
        final var entry = UnsetEntryContext.Hook.entry.getNonNull(context);
        setUpdateTime(entry);
    }

    public boolean isUpdatedAtEnabled() {
        return Values.Bool.isTrue(getConfig().get(ConfigKey.TIME_CREATED_AT_ENABLED));
    }

    public boolean isCreatedAtEnabled() {
        return Values.Bool.isTrue(getConfig().get(ConfigKey.TIME_CREATED_AT_ENABLED));
    }

    public void setCreateTime(@NonNull final Entry entry) {
        if (isCreatedAtEnabled()) {
            entry.set(EntryKey.CREATED_AT, System.currentTimeMillis());
        }
    }

    public void setUpdateTime(@NonNull final Entry entry) {
        if (isUpdatedAtEnabled()) {
            entry.set(EntryKey.UPDATED_AT, System.currentTimeMillis());
        }
    }

    @NonNull
    public String dateToString(final long timestampMs) {
        final var format = getConfig().getNonNull(ConfigKey.TIME_CREATED_AT_FORMAT);
        final var formatter = DateTimeFormatter.ofPattern(format);
        final var dateTime
            = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestampMs), ZoneId.systemDefault());

        return dateTime.format(formatter);
    }

    public String getDateString(@NonNull final String value) {
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
