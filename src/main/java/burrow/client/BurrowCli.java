package burrow.client;

import burrow.core.chamber.ChamberShepherd;
import burrow.core.common.ColorUtility;
import burrow.furniture.standard.HelpCommand;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@CommandLine.Command(
    name = "Burrow CLI",
    version = "1.0.0",
    mixinStandardHelpOptions = true,
    description = "Interactive command-line tool for interacting with Burrow chambers."
)
public final class BurrowCli implements Callable<Integer> {
    @CommandLine.Parameters(
        index = "0",
        paramLabel = "<initial-chamber>",
        description = "The initial chamber.",
        defaultValue = ChamberShepherd.ROOT_CHAMBER_NAME
    )
    private String initialChamber;

    @CommandLine.Option(
        names = {"-v", "--version"},
        description = "Display the version of Burrow CLI.",
        defaultValue = "false"
    )
    private Boolean version;

    @CommandLine.Option(
        names = {"-h", "--help"},
        description = "Display the usage and help info of Burrow CLI.",
        defaultValue = "false"
    )
    private Boolean help;

    @CommandLine.Option(
        names = {"-s", "--split"},
        paramLabel = "<use-split-mode>",
        description = "Apply split mode by default.",
        defaultValue = "false"
    )
    private Boolean useSplitMode;

    @CommandLine.Option(
        names = {"-r", "--server"},
        paramLabel = "<use-server>",
        description = "Use server.",
        defaultValue = "false"
    )
    private Boolean useServer;

    private String chamberName = ChamberShepherd.ROOT_CHAMBER_NAME;
    private Terminal terminal;
    private LineReader reader;
    private BurrowClient burrowClient;

    public static void main(final String[] args) throws IOException {
        System.exit(new CommandLine(new BurrowCli()).execute(args));
    }

    private static String wrapDoubleQuotes(@NotNull final String input) {
        return '"' + input.replace("\"", "\\\"") + '"';
    }

    @Override
    public Integer call() throws BurrowClientInitializationException, IOException {
        if (version) {
            final var annotation = getClass().getAnnotation(CommandLine.Command.class);
            final var name = annotation.name();
            final var version = annotation.version();
            System.out.println(name + " v" + version[0]);
            return CommandLine.ExitCode.OK;
        } else if (help) {
            System.out.println(new CommandLine(this.getClass()).getUsageMessage());
            return CommandLine.ExitCode.OK;
        }

        // Initialize burrow client
        if (useServer) {
            burrowClient = new HttpBurrowClient();
        } else {
            burrowClient = new LocalBurrowClient();
        }

        // Initialize JLine
        final var terminalBuilder = TerminalBuilder.builder();
        terminalBuilder.encoding(StandardCharsets.UTF_8);
        terminal = terminalBuilder.build();
        reader = LineReaderBuilder.builder()
            .terminal(terminal)
            .history(new DefaultHistory())
            .option(LineReader.Option.DISABLE_EVENT_EXPANSION, true)
            .build();

        if (!Objects.equals(initialChamber, ChamberShepherd.ROOT_CHAMBER_NAME)) {
            useChamber(initialChamber);
        }

        //noinspection InfiniteLoopStatement
        while (true) {
            try {
                final var prompt = getPromptString();
                final var command = reader.readLine(prompt).trim();
                if (command.isEmpty()) continue;
                processCommand(command);
            } catch (final UserInterruptException | BurrowClientInitializationException ex) {
                exit();
            }
        }
    }

    private void processCommand(
        @NotNull final String command) throws BurrowClientInitializationException {
        if (command.equals(CliCommand.EXIT)) {
            exit();
        } else if (command.startsWith("/")) {
            resolveCliCommand(command);
        } else {
            if (useSplitMode) {
                executeSplit(command);
            } else {
                execute(command);
            }
        }
    }

    private void execute(@NotNull final String command) {
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

    private void executeSplit(
        @NotNull final String command
    ) {
        final var commandName = command.split(" ")[0];
        final var response = burrowClient.sendRequest(chamberName + " help --json " + commandName);
        final var json = response.getMessage();

        final HelpCommand.CommandInfo commandInfo;
        try {
            commandInfo =
                new Gson().fromJson(json, new TypeToken<HelpCommand.CommandInfo>() {
                }.getType());
        } catch (final Exception ex) {
            System.out.println("Fail to get metadata of command. Check if the chamber exists and uses.");
            return;
        }


        if (commandInfo.getError() != null) {
            System.out.println(commandInfo.getError());
            return;
        }

        final var parameters = commandInfo.getParameters();
        final var argList = new ArrayList<String>();
        for (final var parameter : parameters) {
            final var isOptional = parameter.getOptional();
            final var label = parameter.getLabel();
            final var prompt = label + (isOptional ? "(optional)" : "") + ": ";
            final var coloredPrompt = ColorUtility.render(prompt, ColorUtility.Type.KEY);
            final var arg = reader.readLine(coloredPrompt);
            argList.add(arg);
        }

        final var joinedArgs =
            argList.stream()
                .map(String::trim)
                .map(BurrowCli::wrapDoubleQuotes)
                .collect(Collectors.joining(" "));

        final var realCommand = command + " " + joinedArgs;
        execute(realCommand);
    }

    private void exit() {
        try {
            burrowClient.close();
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
            final var chamberName = command.substring(CliCommand.USE.length()).trim();
            useChamber(chamberName);
        } else if (command.startsWith(CliCommand.COMMANDS)) {
            final var commandDescription = new LinkedHashMap<String, String>();
            commandDescription.put(CliCommand.COMMANDS, "Display all the CLI commands.");
            commandDescription.put(CliCommand.EXIT, "Exit Burrow CLI.");
            commandDescription.put(CliCommand.USE, "Use a specific chamber.");
            commandDescription.put(CliCommand.SPLIT, "Split arguments for input.");

            for (final var key : commandDescription.keySet()) {
                final var paddingLength = 12 - key.length();
                final var coloredName = ColorUtility.render(key, ColorUtility.Type.NAME_COMMAND) +
                    " ".repeat(paddingLength);
                final var coloredDescription =
                    ColorUtility.render(commandDescription.get(key), ColorUtility.Type.DESCRIPTION);
                System.out.println(coloredName + coloredDescription);
            }
        } else if (command.startsWith(CliCommand.SPLIT)) {
            if (useSplitMode) {
                useSplitMode = false;
                System.out.println("Split Mode - OFF");
            } else {
                useSplitMode = true;
                System.out.println("Split Mode - ON");
            }
        } else if (command.equals(CliCommand.CLEAR)) {
            terminal.writer().print("\033[H\033[2J");
            terminal.flush();
        } else {
            System.out.println("Unknown CLI command: " + command);
        }
    }

    private void useChamber(@NotNull final String chamberName) {
        if (!checkChamberExist(chamberName)) {
            System.out.format("Chamber <%s> does not exist.\n", chamberName);
            final var tempChamberName = this.chamberName;
            this.chamberName = ".";
            execute("rlist");
            this.chamberName = tempChamberName;
        } else {
            this.chamberName = chamberName;
        }
    }

    private boolean checkChamberExist(@NotNull final String chamberName) {
        try {
            final var response = burrowClient.sendRequest(chamberName + " root");
            return response.getExitCode() == CommandLine.ExitCode.OK;
        } catch (final Exception ex) {
            return false;
        }
    }

    private @interface CliCommand {
        String COMMANDS = "/commands";
        String EXIT = "/exit";
        String USE = "/use";
        String SPLIT = "/split";
        String CLEAR = "/clear";
    }
}
