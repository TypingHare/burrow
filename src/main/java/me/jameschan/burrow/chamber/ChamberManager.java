package me.jameschan.burrow.chamber;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Consumer;
import me.jameschan.burrow.context.RequestContext;
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
  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
  private final List<Consumer<Chamber>> beforeExecuteCommandListeners = new ArrayList<>();

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
    logger.info("Removed chamber: {}", name);
  }

  public Chamber getChamber(final String name, final boolean autoDelete)
      throws ChamberNotFoundException {
    if (!byName.containsKey(name)) {
      if (autoDelete) {
        scheduler.schedule(() -> removeChamber(name), 300, TimeUnit.SECONDS);
      }
      return initChamber(name);
    }

    return byName.get(name);
  }

  public RequestContext executeCommand(final String name, final List<String> args)
      throws ChamberNotFoundException {
    final var chamber = getChamber(name, true);
    beforeExecuteCommandListeners.forEach(listener -> listener.accept(chamber));

    return chamber.execute(args);
  }

  public Chamber initChamber(final String name) throws ChamberNotFoundException {
    final Chamber chamber = applicationContext.getBean(Chamber.class);
    chamber.construct(name);
    byName.put(name, chamber);

    return chamber;
  }

  public void destructAllChambers() {
    scheduler.shutdownNow();
    byName.values().forEach(Chamber::destruct);
  }

  public void beforeExecuteCommand(final Consumer<Chamber> listener) {
    beforeExecuteCommandListeners.add(listener);
  }
}
