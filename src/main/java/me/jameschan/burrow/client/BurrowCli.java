package me.jameschan.burrow.client;

import com.google.common.base.Strings;
import java.util.LinkedHashMap;
import java.util.concurrent.Callable;
import me.jameschan.burrow.kernel.utility.ColorUtility;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import picocli.CommandLine;

@CommandLine.Command(
    name = "Burrow CLI",
    version = "1.0.0",
    mixinStandardHelpOptions = true,
    description =
        "Interactive command-line tool for managing and interacting with Burrow chambers.")
public class BurrowCli implements Callable<Integer> {
  public static final String DEFAULT_CHAMBER_NAME = ".";

  public static void main(final String[] args) {
    final var exitCode = new CommandLine(new BurrowCli()).execute(args);
    System.exit(exitCode);
  }

  @Override
  public Integer call() throws Exception {
    final var client = getBurrowClient();
    client.setCurrentChamberName(DEFAULT_CHAMBER_NAME);

    final var terminal = TerminalBuilder.terminal();
    final LineReader reader =
        LineReaderBuilder.builder().terminal(terminal).history(new DefaultHistory()).build();

    //noinspection InfiniteLoopStatement
    while (true) {
      try {
        final var prompt = getPromptString(client);
        final var command = reader.readLine(prompt).trim();

        if (command.isEmpty()) continue;
        if (command.equals(CliCommand.EXIT)) {
          exit(terminal);
        } else if (command.startsWith("/")) {
          resolveCliCommand(client, command);
        } else {
          final var response = client.sendRequestTiming(client.prepareRequest(command));
          client.printResponse(response);
        }
      } catch (final UserInterruptException ex) {
        exit(terminal);
      }
    }
  }

  private void exit(final Terminal terminal) {
    try {
      terminal.close();
    } catch (final Throwable ignored) {
    }

    System.out.println("またね");
    System.exit(0);
  }

  private void resolveCliCommand(final BurrowClient client, final String command) {
    if (command.startsWith(CliCommand.USE)) {
      final var chamberName = command.substring(CliCommand.USE.length()).trim();
      client.setCurrentChamberName(chamberName);
    } else if (command.startsWith(CliCommand.COMMANDS)) {
      final var commandDescription = new LinkedHashMap<String, String>();
      commandDescription.put(CliCommand.COMMANDS, "Display all the CLI commands.");
      commandDescription.put(CliCommand.EXIT, "Exit Burrow CLI.");
      commandDescription.put(CliCommand.USE, "Use a specific chamber.");

      for (final var key : commandDescription.keySet()) {
        final var coloredName =
            Strings.padEnd(ColorUtility.render(key, ColorUtility.Type.COMMAND_NAME), 12, ' ');
        final var coloredDescription =
            ColorUtility.render(commandDescription.get(key), ColorUtility.Type.DESCRIPTION);
        System.out.println(coloredName + coloredDescription);
      }
    } else {
      System.out.println("Unknown CLI command: " + command);
    }
  }

  private BurrowClient getBurrowClient() throws BurrowClientInitializationException {
    return new HttpBurrowClient();
  }

  private String getPromptString(final BurrowClient client) {
    final var chamberName = client.getCurrentChamberName();
    return ColorUtility.render(chamberName + "> ", ColorUtility.Type.COMMAND_NAME);
  }

  public static final class CliCommand {
    public static final String COMMANDS = "/commands";
    public static final String EXIT = "/exit";
    public static final String USE = "/use";
  }
}
