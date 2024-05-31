package me.jameschan.burrow.furniture.scheduler;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import me.jameschan.burrow.furniture.aspectcore.AspectCoreFurniture;
import me.jameschan.burrow.kernel.Chamber;
import me.jameschan.burrow.kernel.ChamberShepherd;
import me.jameschan.burrow.kernel.config.Config;
import me.jameschan.burrow.kernel.furniture.Furniture;
import me.jameschan.burrow.kernel.furniture.annotation.BurrowFurniture;
import org.springframework.lang.NonNull;

@BurrowFurniture(
    simpleName = "scheduler",
    description = "Schedule auto terminations for unused chambers.",
    dependencies = {AspectCoreFurniture.class})
public class SchedulerFurniture extends Furniture {
  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
  private final Map<String, Integer> lastAccessedTime = new HashMap<>();

  @Override
  public Collection<String> configKeys() {
    return List.of(ConfigKey.SCHEDULE_INTERVAL_SECONDS, ConfigKey.SCHEDULE_THRESHOLD_SECONDS);
  }

  @Override
  public void initConfig(@NonNull final Config config) {
    config.setIfAbsent(ConfigKey.SCHEDULE_INTERVAL_SECONDS, "5");
    config.setIfAbsent(ConfigKey.SCHEDULE_THRESHOLD_SECONDS, "600");
  }

  public SchedulerFurniture(final Chamber chamber) {
    super(chamber);
  }

  @Override
  public void init() {
    final var aspectFurniture = use(AspectCoreFurniture.class);
    aspectFurniture.beforeExecution(
        (chamberToAccess, requestContext) -> {
          final var chamberName = chamberToAccess.getName();
          if (!chamberName.equals(ChamberShepherd.ROOT_CHAMBER_NAME)) {
            lastAccessedTime.put(chamberName, 0);
          }
        });

    final var intervalInSeconds =
        Integer.parseInt(getContext().getConfig().get(ConfigKey.SCHEDULE_INTERVAL_SECONDS));
    final var thresholdSeconds =
        Integer.parseInt(getContext().getConfig().get(ConfigKey.SCHEDULE_THRESHOLD_SECONDS));
    final Runnable task =
        () -> {
          lastAccessedTime.replaceAll((k, v) -> lastAccessedTime.get(k) + intervalInSeconds);
          for (final var entry : lastAccessedTime.entrySet()) {
            if (entry.getValue() < thresholdSeconds) return;
            final var chamberName = entry.getKey();
            aspectFurniture.getChamberShepherd().terminate(chamberName);
            lastAccessedTime.remove(chamberName);
          }
        };
    scheduler.schedule(task, intervalInSeconds, TimeUnit.SECONDS);
  }

  @Override
  public void onTerminate() {
    scheduler.shutdownNow();
  }

  public static final class ConfigKey {
    public static final String SCHEDULE_INTERVAL_SECONDS = "scheduler.interval_seconds";
    public static final String SCHEDULE_THRESHOLD_SECONDS = "scheduler.threshold_seconds";
  }
}
