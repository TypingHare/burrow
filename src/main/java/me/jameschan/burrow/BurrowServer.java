package me.jameschan.burrow;

import jakarta.annotation.PreDestroy;
import me.jameschan.burrow.chamber.ChamberManager;
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
    final var chamber = chamberManager.getChamber(Constants.DEFAULT_CHAMBER, false);
    if (chamber == null) {
      System.out.println("Fail to initialize the default chamber.");
      System.exit(1);
    }
  }

  @PostMapping("/")
  public BurrowResponse receiveRequest(@RequestBody final BurrowRequest request) {
    final var args = CommandUtility.splitArguments(request.getCommand());
    final var hasChamber = !args.isEmpty() && !args.getFirst().startsWith("-");
    final var chamberName = hasChamber ? args.getFirst() : Constants.DEFAULT_CHAMBER;
    final var chamber = chamberManager.getChamber(chamberName, true);
    final var response = new BurrowResponse();

    if (chamber == null) {
      response.setMessage("Chamber not found: " + chamberName);
      response.setCode(1);
    } else {
      final var context = chamber.execute(hasChamber ? args.subList(1, args.size()) : args);
      response.setMessage(context.getBuffer().toString());
      response.setCode(context.getStatusCode());
    }

    return response;
  }

  @PreDestroy
  public void onShutdown() {
    logger.info("Shutting down Burrow ...");

    chamberManager.destructAllChambers();
  }
}
