package burrow.furniture.scheduler;

import burrow.core.chamber.Chamber;
import burrow.core.chamber.ChamberShepherd;
import burrow.core.config.Config;
import burrow.core.furniture.BurrowFurniture;
import burrow.core.furniture.Furniture;
import burrow.furniture.aspectcore.AspectCoreFurniture;
import burrow.furniture.dictator.DictatorFurniture;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@BurrowFurniture(
    simpleName = "Scheduler",
    description = "Scheduler terminates unused chambers automatically.",
    type = BurrowFurniture.Type.ROOT
)
public class SchedulerFurniture extends Furniture {
    public static final long DEFAULT_INTERVAL_MS = 3000;
    public static final long DEFAULT_THRESHOLD_MS = 600000;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    public Long thresholdMs = DEFAULT_THRESHOLD_MS;

    public SchedulerFurniture(@NotNull final Chamber chamber) {
        super(chamber);
    }

    @Override
    public Collection<String> configKeys() {
        return List.of(ConfigKey.SCHEDULE_INTERVAL_MS, ConfigKey.SCHEDULE_THRESHOLD_MS);
    }

    @Override
    public void initializeConfig(@NotNull final Config config) {
        config.setIfAbsent(ConfigKey.SCHEDULE_INTERVAL_MS, DEFAULT_INTERVAL_MS);
        config.setIfAbsent(ConfigKey.SCHEDULE_THRESHOLD_MS, DEFAULT_THRESHOLD_MS);
    }

    @Override
    public void afterInitialization() {
        thresholdMs = getThresholdMs();
        final var intervalMs = getIntervalMs();
        scheduler.scheduleAtFixedRate(this::callback, intervalMs, intervalMs, TimeUnit.MILLISECONDS);
    }

    private void callback() {
        final var aspectCoreFurniture = use(AspectCoreFurniture.class);
        final var chamberNameList = aspectCoreFurniture.getRunningChamberNameList();
        final var dictatorFurniture = use(DictatorFurniture.class);

        chamberNameList.remove(ChamberShepherd.ROOT_CHAMBER_NAME);
        for (final var chamberName : chamberNameList) {
            final var chamberInfo = dictatorFurniture.getChamberInfoMap().get(chamberName);
            final var lastRequestTimestampMs = chamberInfo.getLastRequestTimestampMs();
            final var diff = System.currentTimeMillis() - lastRequestTimestampMs;
            if (diff < thresholdMs) return;

            // Terminate the chamber
            dictatorFurniture.terminate(chamberName);
        }
    }

    public long getIntervalMs() {
        return Long.parseLong(getConfig().getNotNull(ConfigKey.SCHEDULE_INTERVAL_MS));
    }

    public long getThresholdMs() {
        return Long.parseLong(getConfig().getNotNull(ConfigKey.SCHEDULE_THRESHOLD_MS));
    }

    @Override
    public void terminate() {
        scheduler.shutdownNow();
    }

    public @interface ConfigKey {
        String SCHEDULE_INTERVAL_MS = "scheduler.interval_ms";
        String SCHEDULE_THRESHOLD_MS = "scheduler.threshold_ms";
    }

}
