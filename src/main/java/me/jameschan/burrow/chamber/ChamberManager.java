package me.jameschan.burrow.chamber;

import java.util.Map;
import java.util.concurrent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ChamberManager {
  private static final Logger logger = LoggerFactory.getLogger(ChamberManager.class.getName());

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

  public Chamber getChamber(final String name, final boolean autoDelete) {
    if (!byName.containsKey(name)) {
      final Chamber chamber = applicationContext.getBean(Chamber.class);
      try {
        chamber.construct(name);
        byName.put(name, chamber);
        if (autoDelete) {
          scheduler.schedule(() -> removeChamber(name), 300, TimeUnit.SECONDS);
        }
      } catch (final ChamberNotFoundException ex) {
        logger.error(ex.getMessage(), ex);
      }
    }

    return byName.get(name);
  }

  public void destructAllChambers() {
    scheduler.shutdownNow();
    byName.values().forEach(Chamber::destruct);
  }
}
