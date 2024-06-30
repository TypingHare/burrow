package burrow.furniture.time;

import burrow.chain.Context;
import burrow.core.chain.ToEntryObjectChain;
import burrow.core.chain.UpdateEntryChain;
import burrow.core.chamber.Chamber;
import burrow.core.chamber.ChamberContext;
import burrow.core.common.Values;
import burrow.core.config.Config;
import burrow.core.furniture.BurrowFurniture;
import burrow.core.furniture.Furniture;
import burrow.furniture.entry.EntryFurniture;
import org.springframework.lang.NonNull;

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
        EntryFurniture.class
    }
)
public final class TimeFurniture extends Furniture {
    public static final String DEFAULT_TIME_FORMAT = "MM/dd, yyyy";

    public TimeFurniture(@NonNull final Chamber chamber) {
        super(chamber);
    }

    @Override
    public void init() {
        context.getOverseer().getCreateEntryChain().pre.use(this::createEntry);
        context.getOverseer().getSetEntryChain().pre.use(this::setEntry);
        context.getOverseer().getToFormattedObjectChain().pre.use(this::toFormattedObject);
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
    public void initConfig(@NonNull final Config config) {
        config.setIfAbsent(ConfigKey.TIME_CREATED_AT_ENABLED, Values.Bool.TRUE);
        config.setIfAbsent(ConfigKey.TIME_UPDATED_AT_ENABLED, Values.Bool.TRUE);
        config.setIfAbsent(ConfigKey.TIME_CREATED_AT_FORMAT, DEFAULT_TIME_FORMAT);
        config.setIfAbsent(ConfigKey.TIME_UPDATED_AT_FORMAT, DEFAULT_TIME_FORMAT);
    }

    public void createEntry(@NonNull final Context ctx) {
        final var entry = UpdateEntryChain.entryHook.get(ctx);
        final var currentTimeMs = System.currentTimeMillis();

        if (isCreatedAtEnabled(context)) {
            entry.set(EntryKey.CREATED_AT, currentTimeMs);
        }

        if (isUpdatedAtEnabled(context)) {
            entry.set(EntryKey.UPDATED_AT, currentTimeMs);
        }
    }

    public void toFormattedObject(@NonNull final Context ctx) {
        final var entry = ToEntryObjectChain.entryHook.get(ctx);
        final var entryObject = ToEntryObjectChain.entryObjectHook.get(ctx);

        if (isCreatedAtEnabled(context)) {
            final var dateString =
                getDateString(context, entry.getRequireNonNull(EntryKey.CREATED_AT));
            entryObject.put(EntryKey.CREATED_AT, dateString);
        }

        if (isUpdatedAtEnabled(context)) {
            final var dateString =
                getDateString(context, entry.getRequireNonNull(EntryKey.UPDATED_AT));
            entryObject.put(EntryKey.UPDATED_AT, dateString);
        }
    }

    public void setEntry(@NonNull final Context ctx) {
        final var entry = UpdateEntryChain.entryHook.get(ctx);
        final var currentTimeMs = System.currentTimeMillis();

        if (isUpdatedAtEnabled(context)) {
            entry.set(EntryKey.UPDATED_AT, currentTimeMs);
        }
    }

    public static boolean isUpdatedAtEnabled(@NonNull final ChamberContext chamberContext) {
        return Values.Bool.isTrue(chamberContext.getConfig()
            .get(ConfigKey.TIME_CREATED_AT_ENABLED));
    }

    public static boolean isCreatedAtEnabled(@NonNull final ChamberContext chamberContext) {
        return Values.Bool.isTrue(chamberContext.getConfig()
            .get(ConfigKey.TIME_CREATED_AT_ENABLED));
    }

    @NonNull
    public static String dateToString(
        @NonNull final ChamberContext chamberContext,
        final long timestampMs
    ) {
        final var format =
            chamberContext.getConfig().getRequireNotNull(ConfigKey.TIME_CREATED_AT_FORMAT);
        final var formatter = DateTimeFormatter.ofPattern(format);
        final var dateTime
            = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestampMs), ZoneId.systemDefault());

        return dateTime.format(formatter);
    }

    public static String getDateString(
        @NonNull final ChamberContext chamberContext,
        @NonNull final String value
    ) {
        final var timestampMs = Long.parseLong(value);
        return dateToString(chamberContext, timestampMs);
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
