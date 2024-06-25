package burrow.client;

import burrow.core.common.CommandUtility;

import java.util.concurrent.Callable;

public class BurrowCommand implements Callable<Integer> {
    private final String command;

    public BurrowCommand(final String command) {
        this.command = command;
    }

    public static void main(final String[] args) throws BurrowClientInitializationException {
        final var exitCode = new BurrowCommand(CommandUtility.getOriginalCommand(args)).call();
        System.exit(exitCode);
    }

    public Integer call() throws BurrowClientInitializationException {
        final var client = new HttpBurrowClient();
        final var response = client.sendRequest(command);
        final var message = response.getMessage();
        if (message != null && !message.isEmpty()) {
            System.out.println(response.getMessage());
        }

//        final var immediateCommand = response.getImmediateCommand();
//        if (!immediateCommand.isEmpty()) {
//            // TODO: EXECUTE THE COMMAND
//            final List<String> commandTokens = Arrays.asList("sh", "-c", command);
//            final var processBuilder = new ProcessBuilder(commandTokens);
//            processBuilder.start();
//        }

        return response.getExitCode();
    }
}
