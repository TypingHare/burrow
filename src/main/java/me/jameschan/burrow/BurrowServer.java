package me.jameschan.burrow;

import jakarta.annotation.PreDestroy;
import me.jameschan.burrow.chamber.ChamberManager;
import me.jameschan.burrow.chamber.ChamberNotFoundException;
import me.jameschan.burrow.common.BurrowRequest;
import me.jameschan.burrow.common.BurrowResponse;
import me.jameschan.burrow.utility.CommandUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class BurrowServer {
  private static final Logger logger = LoggerFactory.getLogger(BurrowServer.class);
  private final ChamberManager chamberManager;

  @Autowired
  public BurrowServer(final ChamberManager chamberManager) {
    this.chamberManager = chamberManager;
  }

  public static void main(final String[] args) {
    SpringApplication.run(BurrowServer.class, args);
  }

  @EventListener(ContextRefreshedEvent.class)
  public void onStart() {
    try {
      chamberManager.initiate(Constants.DEFAULT_CHAMBER);
    } catch (final ChamberNotFoundException ex) {
      logger.error("Fail to initialize the default chamber.", ex);
      System.exit(1);
    }
  }

  @PostMapping("/")
  public BurrowResponse receiveRequest(@RequestBody final BurrowRequest request) {
    final var args = CommandUtility.splitArguments(request.getCommand());
    final var hasChamber = !args.isEmpty() && !args.getFirst().startsWith("-");
    final var chamberName = hasChamber ? args.getFirst() : Constants.DEFAULT_CHAMBER;
    final var realArgs = hasChamber ? args.subList(1, args.size()) : args;

    var response = new BurrowResponse();
    try {
      final var requestContext = chamberManager.executeCommand(chamberName, realArgs);
      response.setMessage(requestContext.getBuffer().toString());
      response.setCode(requestContext.getStatusCode());
    } catch (final ChamberNotFoundException ex) {
      response.setMessage("Chamber not found: " + chamberName);
      response.setCode(1);
    } catch (final Throwable ex) {
      response.setMessage("Internal error: " + ex.getMessage());
      response.setCode(1);
    }

    return response;
  }

  @PreDestroy
  public void onShutdown() {
    logger.info("Shutting down Burrow ...");

    chamberManager.destructAllChambers();
  }
}
