package me.jameschan.burrow.chamber;

import java.util.Map;
import java.util.concurrent.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ChamberManager {
  private final Map<String, Chamber> byName = new ConcurrentHashMap<>();
  private final ApplicationContext applicationContext;
  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

  @Autowired
  public ChamberManager(final ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  public void removeChamber(final String name) {
    final var chamber = byName.get(name);
    if (chamber == null) {
      return;
    }

    byName.remove(name);
    chamber.destruct();
  }

  public Chamber getChamber(final String name) {
    return byName.computeIfAbsent(
        name,
        key -> {
          final Chamber chamber = applicationContext.getBean(Chamber.class);
          chamber.construct(name);
          scheduler.schedule(() -> removeChamber(name), 300, TimeUnit.SECONDS);
          return chamber;
        });
  }

  public void destructAllChambers() {
    scheduler.shutdownNow();
    byName.values().forEach(Chamber::destruct);
  }
}
