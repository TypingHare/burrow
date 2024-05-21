package me.jameschan.burrow.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.util.Properties;
import picocli.CommandLine;

public abstract class BurrowClient {
  public static final String APPLICATION_PROPERTIES_FILE = "application.properties";
  public static final String DEFAULT_CHAMBER_NAME = ".";

  protected final Properties properties = new Properties();

  protected final URI uri;

  protected String currentChamberName = DEFAULT_CHAMBER_NAME;

  protected String lastCommand = "";

  protected Duration lastRequestDuration = Duration.ZERO;

  public BurrowClient() throws BurrowClientInitializationException {
    try (final InputStream inputStream =
        getClass().getClassLoader().getResourceAsStream(APPLICATION_PROPERTIES_FILE)) {
      properties.load(inputStream);
    } catch (final IOException ex) {
      throw new BurrowClientInitializationException(
          "Fail to read file: " + APPLICATION_PROPERTIES_FILE, ex);
    }

    try {
      final var uriString = properties.getProperty("burrow.client.uri");
      this.uri = new URI(uriString);
    } catch (final URISyntaxException ex) {
      throw new BurrowClientInitializationException("Invalid URI: " + ex.getInput(), ex);
    }
  }

  public BurrowResponse sendRequestTiming(final BurrowRequest request) {
    final Instant start = Instant.now();
    final BurrowResponse response = sendRequest(request);
    lastCommand = request.getCommand();
    lastRequestDuration = Duration.between(start, Instant.now());

    // Print the command
    System.out.print(lastCommand);

    return response;
  }

  protected abstract BurrowResponse sendRequest(final BurrowRequest request);

  public String getPrompt() {
    return currentChamberName + "> ";
  }

  public void printResponse(final BurrowResponse response) {
    // Print the duration and status code on the right side
    final var durationString = lastRequestDuration.toMillis() + "ms";
    final var codeString = String.valueOf(response.getCode());
    final var rightSideString = durationString + ", " + codeString;
    final var leftSideStringLength = getPrompt().length() + lastCommand.length();
    final var rightSideStringLength = rightSideString.length();
    final var consoleWidth = getConsoleWidth();
    System.out.println(
        " ".repeat(consoleWidth - leftSideStringLength - rightSideStringLength) + rightSideString);

    // Print the message
    System.out.println(response.message);
  }

  public static int getConsoleWidth() {
    final var commandSpec = CommandLine.Model.CommandSpec.create();
    final var message = commandSpec.usageMessage().autoWidth(true);
    return message.width();
  }
}
