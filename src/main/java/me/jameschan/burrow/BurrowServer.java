package me.jameschan.burrow;

import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.jameschan.burrow.chamber.ChamberManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
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

  public static List<String> splitArguments(String input) {
    final List<String> arguments = new ArrayList<>();
    final Matcher matcher = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(input);
    while (matcher.find()) {
      arguments.add(matcher.group(1).replace("\"", ""));
    }

    return arguments;
  }

  @PostMapping("/")
  public ResponseEntity<Map<String, String>> receiveRequest(
      @RequestBody final Map<String, String> requestObject) {
    final var args = splitArguments(requestObject.get("command"));
    final var hasChamber = !args.isEmpty() && !args.getFirst().startsWith("-");
    final var chamberName = hasChamber ? args.getFirst() : Constants.DEFAULT_CHAMBER;
    final var chamber = chamberManager.getChamber(chamberName, true);
    final var responseObject = new HashMap<String, String>();

    if (chamber == null) {
      responseObject.put("output", "Chamber not found: " + chamberName);
      responseObject.put("code", "1");
    } else {
      final var context = chamber.execute(hasChamber ? args.subList(1, args.size()) : args);
      responseObject.put("code", String.valueOf(context.getStatusCode()));
      responseObject.put("output", context.getBuffer().toString());
    }

    return ResponseEntity.ok().body(responseObject);
  }

  @PreDestroy
  public void onShutdown() {
    logger.info("Shutting down Burrow ...");

    chamberManager.destructAllChambers();
  }
}
