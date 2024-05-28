package me.jameschan.burrow.kernel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import me.jameschan.burrow.kernel.common.*;
import me.jameschan.burrow.kernel.context.RequestContext;
import me.jameschan.burrow.kernel.utility.CommandUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component()
public class ChamberShepherd {
  private static final Logger logger = LoggerFactory.getLogger(ChamberShepherd.class);

  private final ApplicationContext applicationContext;
  private final Map<String, Chamber> chamberStore = new ConcurrentHashMap<>();

  @Autowired
  public ChamberShepherd(final ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  public Chamber initiate(final String chamberName) throws ChamberInitializationException {
    final var chamber = applicationContext.getBean(Chamber.class);
    chamber.initiate(chamberName);
    chamberStore.put(chamberName, chamber);
    logger.info("Chamber initiated: {}", chamberName);

    return chamber;
  }

  public void terminate(final String chamberName) {
    final var chamber = chamberStore.get(chamberName);
    if (chamber == null) {
      logger.warn("Chamber not found: {}", chamberName);
    } else {
      chamber.terminate();
      chamberStore.remove(chamberName);
      logger.info("Chamber terminated: {}", chamberName);
    }
  }

  public Chamber getChamber(final String name) throws ChamberInitializationException {
    return chamberStore.containsKey(name) ? chamberStore.get(name) : initiate(name);
  }

  public BurrowResponse processRequest(final BurrowRequest request) {
    final var args = CommandUtility.splitArguments(request.getCommand());
    final var hasChamber = !args.isEmpty() && !args.getFirst().startsWith("-");
    final var chamberName = hasChamber ? args.getFirst() : Constants.DEFAULT_CHAMBER;
    final var realArgs = hasChamber ? args.subList(1, args.size()) : args;

    final BurrowResponse response = new BurrowResponse();
    try {
      final var chamber = getChamber(chamberName);
      final var requestContext = new RequestContext();
      requestContext.set(RequestContext.Key.WORKING_DIRECTORY, request);
      chamber.execute(requestContext, realArgs);

      response.setMessage(requestContext.getBuffer().toString());
      response.setCode(requestContext.getExitCode());
    } catch (final ChamberInitializationException ex) {
      response.setMessage(ex.getMessage());
      response.setCode(ExitCode.ERROR);
    } catch (final Throwable ex) {
      response.setMessage("Internal error: " + ex.getMessage());
      response.setCode(ExitCode.ERROR);
    }

    return response;
  }

  public void terminateAll() {
    chamberStore.values().forEach(Chamber::terminate);
  }
}
