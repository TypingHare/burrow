package me.jameschan.burrow.client;

import java.util.Scanner;
import java.util.concurrent.Callable;
import me.jameschan.burrow.common.BurrowRequest;
import picocli.CommandLine;

@CommandLine.Command(name = "Burrow CLI", version = "1.0.0", mixinStandardHelpOptions = true)
public class BurrowCli implements Callable<Integer> {
  public static void main(final String[] args) {
    final var exitCode = new CommandLine(new BurrowCli()).execute(args);
    System.exit(exitCode);
  }

  public static final String DEFAULT_CHAMBER_NAME = ".";

  @Override
  public Integer call() throws Exception {
    final var client = getBurrowClient();
    client.setCurrentChamberName(DEFAULT_CHAMBER_NAME);

    final var scanner = new Scanner(System.in);
    var isRunning = true;
    while (isRunning) {
      client.printPrompt();
      final var command = scanner.nextLine().trim();
      if (command.equals(CliCommand.$EXIT)) {
        isRunning = false;
      } else if (command.startsWith("$")) {
        resolveCliCommand(client, command);
      } else {
        final var request = new BurrowRequest();
        request.setCommand(command);
        final var response = client.sendRequestTiming(request);
        client.printResponse(response);
      }
    }

    return 0;
  }

  private void resolveCliCommand(final BurrowClient client, final String command) {
    if (command.startsWith(CliCommand.$USE)) {
      client.setCurrentChamberName(command.substring(CliCommand.$USE.length()).trim());
    } else {
      System.out.println("Unknown CLI command: " + command);
    }
  }

  private BurrowClient getBurrowClient() throws BurrowClientInitializationException {
    return new HttpBurrowClient();
  }

  public static final class CliCommand {
    public static final String $EXIT = "$exit";
    public static final String $USE = "$use";
  }
}
