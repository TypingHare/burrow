package me.jameschan.burrow.client;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import me.jameschan.burrow.kernel.utility.CommandUtility;

public class Burrow {
  public static void main(final String[] args)
      throws BurrowClientInitializationException, IOException {
    final var command = CommandUtility.getOriginalCommand(args);
    final var client = new HttpBurrowClient();
    final var request = client.prepareRequest(command);
    final var response = client.sendRequest(request);

    final var message = response.getMessage();
    if (!message.isEmpty()) {
      System.out.println(response.getMessage());
    }

    final var immediateCommand = response.getImmediateCommand();
    if (!immediateCommand.isEmpty()) {
      // TODO: EXECUTE THE COMMAND
      final List<String> commandTokens = Arrays.asList("sh", "-c", command);
      final var processBuilder = new ProcessBuilder(commandTokens);
      processBuilder.start();
    }

    System.exit(response.getCode());
  }
}
