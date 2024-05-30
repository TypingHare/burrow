package me.jameschan.burrow.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Properties;
import me.jameschan.burrow.kernel.common.BurrowRequest;
import me.jameschan.burrow.kernel.common.BurrowResponse;
import picocli.CommandLine;

public abstract class BurrowClient {
  public static final String APPLICATION_PROPERTIES_FILE = "application.properties";

  protected final Properties properties = new Properties();
  protected final URI uri;
  protected String workingDirectory;
  protected String currentChamberName;
  protected String lastCommand = "";
  protected Duration lastRequestDuration = Duration.ZERO;

  public BurrowClient() throws BurrowClientInitializationException {
    // Load application properties
    try (final InputStream inputStream =
        getClass().getClassLoader().getResourceAsStream(APPLICATION_PROPERTIES_FILE)) {
      properties.load(inputStream);
    } catch (final IOException ex) {
      throw new BurrowClientInitializationException(
          "Fail to read file: " + APPLICATION_PROPERTIES_FILE, ex);
    }

    // Get the URL of the server to send requests
    final var uriString = properties.getProperty("burrow.client.url");
    this.uri = URI.create(uriString);

    // Environment
    workingDirectory = System.getProperty("user.dir");
  }

  public static int getConsoleWidth() {
    final var commandSpec = CommandLine.Model.CommandSpec.create();
    final var message = commandSpec.usageMessage().autoWidth(true);
    return message.width();
  }

  public BurrowRequest prepareRequest(final String command) {
    final var request = new BurrowRequest();
    request.setCommand(command);
    request.setWorkingDirectory(workingDirectory);
    request.setConsoleWidth(getConsoleWidth());

    return request;
  }

  public BurrowResponse sendRequestTiming(final BurrowRequest request) {
    final Instant start = Instant.now();
    lastCommand = request.getCommand();
    final BurrowResponse response = sendRequest(request);
    lastRequestDuration = Duration.between(start, Instant.now());

    return response;
  }

  protected abstract BurrowResponse sendRequest(final BurrowRequest request);

  public String getCurrentChamberName() {
    return currentChamberName;
  }

  public void setCurrentChamberName(final String currentChamberName) {
    this.currentChamberName = currentChamberName;
  }

  public void printResponse(final BurrowResponse response) {
    // Print the duration and status code on the right side
    final var durationString = "(" + lastRequestDuration.toMillis() + "ms)";
    final var rightSideString = response.getCode() + " " + durationString;
    final var rightSideStringLength = rightSideString.length();
    final var consoleWidth = getConsoleWidth();
    final var coloredRightSideString =
        getColoredCodeString(response.getCode()) + " " + getColoredDurationString(durationString);

    // Move the cursor to the proper column in the previous line, then output the right side string
    // and wrap the line
    System.out.print("\033[1A");
    System.out.print("\033[" + (consoleWidth - rightSideStringLength) + "C");
    System.out.println(coloredRightSideString);

    // Print the message and wrap the line
    System.out.println(response.getMessage());
  }

  protected String getColoredCodeString(int code) {
    return code == 0
        ? CommandLine.Help.Ansi.AUTO.string("@|green 0|@")
        : CommandLine.Help.Ansi.AUTO.string("@|red " + code + "|@");
  }

  protected String getColoredDurationString(final String durationString) {
    return CommandLine.Help.Ansi.AUTO.string("@|yellow " + durationString + "|@");
  }
}
