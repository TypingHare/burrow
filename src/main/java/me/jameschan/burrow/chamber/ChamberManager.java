package me.jameschan.burrow.chamber;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ChamberManager {
  private final Map<String, Chamber> byName = new HashMap<>();

  private final ApplicationContext applicationContext;

  @Autowired
  public ChamberManager(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  public Chamber getChamber(final String name) {
    if (!byName.containsKey(name)) {
      final var chamber = applicationContext.getBean(Chamber.class);
      chamber.construct(name);
      byName.put(name, chamber);

      final ExecutorService executor = Executors.newSingleThreadExecutor();
      final Callable<Void> callback =
          () -> {
            Thread.sleep(60000);
            chamber.destruct();
            executor.close();
            return null;
          };

      executor.submit(callback);
    }

    return byName.get(name);
  }

  public void destructAllChambers() {
    byName.values().forEach(Chamber::destruct);
  }
}
