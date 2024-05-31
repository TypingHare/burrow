package me.jameschan.burrow.kernel;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
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
  public static final Path CHAMBER_ROOT_DIR = Burrow.ROOT_DIR.resolve("chamber");
  public static final String ROOT_CHAMBER_NAME = ".";

  private static final Logger logger = LoggerFactory.getLogger(ChamberShepherd.class);

  private final ApplicationContext applicationContext;
  private final Map<String, Chamber> chamberStore = new ConcurrentHashMap<>();
  private final List<BiConsumer<Chamber, RequestContext>> beforeExecutionListeners =
      new ArrayList<>();
  private final List<BiConsumer<Chamber, RequestContext>> afterExecutionListeners =
      new ArrayList<>();

  @Autowired
  public ChamberShepherd(final ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  /** Initiates the root chamber. */
  public void init() throws ChamberInitializationException {
    initiate(ROOT_CHAMBER_NAME);
  }

  public Chamber initiate(final String chamberName) throws ChamberInitializationException {
    final var chamber = applicationContext.getBean(Chamber.class);
    logger.info("Chamber initiating: {}", chamberName);

    try {
      chamber.initiate(chamberName);
    } catch (final ChamberInitializationException ex) {
      logger.error(ex.getMessage());
      logger.error("Fail to initiate chamber: {}", chamberName);
      throw ex;
    }

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
    final var chamberName = hasChamber ? args.getFirst() : ROOT_CHAMBER_NAME;
    final var realArgs = hasChamber ? args.subList(1, args.size()) : args;

    final BurrowResponse response = new BurrowResponse();
    try {
      final var chamber = getChamber(chamberName);
      final var requestContext = new RequestContext();
      requestContext.set(RequestContext.Key.WORKING_DIRECTORY, request);

      // Trigger before execution listeners
      beforeExecutionListeners.forEach(listener -> listener.accept(chamber, requestContext));
      chamber.execute(requestContext, realArgs);
      response.setMessage(requestContext.getBuffer().toString());
      response.setCode(requestContext.getExitCode());
      response.setImmediateCommand(requestContext.getImmediateCommand());

      // Trigger after execution listeners
      afterExecutionListeners.forEach(listener -> listener.accept(chamber, requestContext));
    } catch (final ChamberInitializationException ex) {
      response.setMessage("Fail to initiate: " + chamberName + "\n" + ex.getMessage());
      response.setCode(ExitCode.ERROR);
    } catch (final Throwable ex) {
      response.setMessage("Internal error: " + ex.getMessage());
      response.setCode(ExitCode.ERROR);
    }

    return response;
  }

  public void terminateAll() {
    chamberStore.keySet().forEach(this::terminate);
  }

  public void beforeExecution(final BiConsumer<Chamber, RequestContext> listener) {
    beforeExecutionListeners.add(listener);
  }

  public void afterExecution(final BiConsumer<Chamber, RequestContext> listener) {
    afterExecutionListeners.add(listener);
  }
}
