package me.jameschan.burrow;

import java.time.Instant;
import java.util.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BurrowCli {
  private static final Logger logger = LoggerFactory.getLogger(BurrowCli.class);

  public static void main(final String[] args) {
    final var PROMPT = "> ";
    final var EXIT_COMMAND = "exit";
    final var USE_COMMAND = "use";

    final var properties = BurrowClient.loadApplicationProperties();
    final var uri = "http://localhost:" + properties.getProperty("server.port");
    final var scanner = new Scanner(System.in);

    var isRunning = true;
    var chamber = ".";
    while (isRunning) {
      System.out.print(chamber + PROMPT);
      final var command = scanner.nextLine();
      if (command.equals(EXIT_COMMAND)) {
        isRunning = false;
      } else if (command.startsWith(USE_COMMAND)) {
        chamber = command.substring(USE_COMMAND.length()).trim();
      } else {
        try {
          Instant start = Instant.now();
          final var data = BurrowClient.sendRequestToServer(uri, chamber + " " + command);
          Instant end = Instant.now();
          long duration = java.time.Duration.between(start, end).toMillis();
          System.out.println("Request duration: " + duration + " ms");

          final var output = data.get("output");
          final var code = Integer.parseInt(data.get("code"));
          System.out.println(output);
          System.out.println("code: " + code);
        } catch (final Throwable throwable) {
          System.out.println("Internal Error: " + throwable.getMessage());
          System.out.println(chamber + " " + command);
        }
      }
    }
  }
}
