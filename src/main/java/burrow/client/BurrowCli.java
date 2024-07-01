package burrow.client;

import burrow.core.chamber.ChamberShepherd;
import burrow.core.common.ColorUtility;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.concurrent.Callable;

@CommandLine.Command(
    name = "Burrow CLI",
    version = "1.0.0",
    mixinStandardHelpOptions = true,
    description = "Interactive command-line tool for managing and interacting with Burrow chambers."
)
public final class BurrowCli implements Callable<Integer> {
    @CommandLine.Option(names = {"-v", "--version"}, defaultValue = "false")
    private Boolean version;
    private String chamberName = ChamberShepherd.ROOT_CHAMBER_NAME;

    public static void main(final String[] args) {
        System.exit(new CommandLine(new BurrowCli()).execute(args));
    }

    @Override
    public Integer call() throws IOException {
        if (version) {
            final var annotation = getClass().getAnnotation(CommandLine.Command.class);
            final var name = annotation.name();
            final var version = annotation.version();
            System.out.println(name + " v" + version[0]);
            return CommandLine.ExitCode.OK;
        }

        final var terminalBuilder = TerminalBuilder.builder();
        terminalBuilder.encoding(StandardCharsets.UTF_8);
        final var terminal = terminalBuilder.build();
        final var reader = LineReaderBuilder.builder()
            .terminal(terminal)
            .history(new DefaultHistory())
            .option(LineReader.Option.DISABLE_EVENT_EXPANSION, true)
            .build();

        //noinspection InfiniteLoopStatement
        while (true) {
            try {
                final var prompt = getPromptString();
                final var command = reader.readLine(prompt).trim();
                if (command.isEmpty()) continue;
                processNextCommand(command, terminal);
            } catch (final UserInterruptException | BurrowClientInitializationException ex) {
                exit(terminal);
            }
        }
    }

    private void processNextCommand(
        final String command,
        final Terminal terminal
    ) throws BurrowClientInitializationException {
        if (command.equals(CliCommand.EXIT)) {
            exit(terminal);
        } else if (command.startsWith("/")) {
            resolveCliCommand(command);
        } else {
            final var burrowClient = new HttpBurrowClient();
            final var response =
                burrowClient.sendRequestTiming(chamberName + " " + command);
            final var lastRequestDuration = burrowClient.getLastRequestDuration();
            final var environment = burrowClient.getEnvironment();

            final var exitCode = response.getExitCode();
            final var message = response.getMessage();

            // Print the duration and status code on the right side
            final var durationString = "(" + lastRequestDuration.toMillis() + "ms)";
            final var rightSideString = exitCode + " " + durationString;
            final var rightSideStringLength = rightSideString.length();
            final var consoleWidth = environment.getConsoleWidth();
            final var coloredRightSideString = getColoredCodeString(exitCode) + " " +
                getColoredDurationString(durationString);

            // Move the cursor to the proper column in the previous line, then output the
            // right side string and wrap the line
            System.out.print("\033[1A");
            System.out.print("\033[" + (consoleWidth - rightSideStringLength) + "C");
            System.out.println(coloredRightSideString);

            // Print the message and wrap the line
            if (!message.isEmpty()) {
                System.out.println(response.getMessage());
            }
        }
    }

    private void exit(final Terminal terminal) {
        try {
            terminal.close();
        } catch (final Throwable ignored) {
        }

        System.out.println("See you | 再见 | 得閒飲茶 | またね");
        System.exit(CommandLine.ExitCode.OK);
    }

    private String getPromptString() {
        return ColorUtility.render(chamberName + "> ", "fg(134)");
    }

    private String getColoredCodeString(int code) {
        return code == 0
            ? CommandLine.Help.Ansi.AUTO.string("@|green 0|@")
            : CommandLine.Help.Ansi.AUTO.string("@|red " + code + "|@");
    }

    private String getColoredDurationString(final String durationString) {
        return CommandLine.Help.Ansi.AUTO.string("@|yellow " + durationString + "|@");
    }

    private void resolveCliCommand(final String command) {
        if (command.startsWith(CliCommand.USE)) {
            chamberName = command.substring(CliCommand.USE.length()).trim();
        } else if (command.startsWith(CliCommand.COMMANDS)) {
            final var commandDescription = new LinkedHashMap<String, String>();
            commandDescription.put(CliCommand.COMMANDS, "Display all the CLI commands.");
            commandDescription.put(CliCommand.EXIT, "Exit Burrow CLI.");
            commandDescription.put(CliCommand.USE, "Use a specific chamber.");

            for (final var key : commandDescription.keySet()) {
                final var paddingLength = 12 - key.length();
                final var coloredName = ColorUtility.render(key, ColorUtility.Type.NAME_COMMAND) +
                    " ".repeat(paddingLength);
                final var coloredDescription =
                    ColorUtility.render(commandDescription.get(key), ColorUtility.Type.DESCRIPTION);
                System.out.println(coloredName + coloredDescription);
            }
        } else {
            System.out.println("Unknown CLI command: " + command);
        }
    }

    private static final class CliCommand {
        public static final String COMMANDS = "/commands";
        public static final String EXIT = "/exit";
        public static final String USE = "/use";
    }
}
