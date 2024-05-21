package me.jameschan.burrow.client;

import me.jameschan.burrow.common.BurrowRequest;
import me.jameschan.burrow.utility.CommandUtility;

public class BurrowCommand {
  public static void main(final String[] args) throws BurrowClientInitializationException {
    final var command = CommandUtility.getOriginalCommand(args);
    final var client = new HttpBurrowClient();
    final var request = new BurrowRequest();
    request.setCommand(command);

    final var response = client.sendRequest(request);
    System.out.print(response.getMessage());
    System.exit(response.getCode());
  }
}
