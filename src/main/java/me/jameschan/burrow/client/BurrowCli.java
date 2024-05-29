package me.jameschan.burrow.client;

import java.util.concurrent.Callable;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.TerminalBuilder;
import picocli.CommandLine;

@CommandLine.Command(name = "Burrow CLI", version = "1.0.0", mixinStandardHelpOptions = true)
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

    var isRunning = true;
    while (isRunning) {
      final var prompt = getPromptString(client);
      final var command = reader.readLine(prompt).trim();
      if (command.equals(CliCommand.$EXIT)) {
        isRunning = false;
        terminal.close();
      } else if (command.startsWith("$")) {
        resolveCliCommand(client, command);
      } else {
        final var response = client.sendRequestTiming(client.prepareRequest(command));
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

  private String getPromptString(final BurrowClient client) {
    final var chamberName = client.getCurrentChamberName();
    return CommandLine.Help.Ansi.AUTO.string("@|blue " + chamberName + "> |@");
  }

  public static final class CliCommand {
    public static final String $EXIT = "$exit";
    public static final String $USE = "$use";
  }
}
