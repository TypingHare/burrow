package burrow.client;

import burrow.core.common.CommandUtility;

import java.util.concurrent.Callable;

public final class BurrowCommand implements Callable<Integer> {
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
        client.close();

        final var message = response.getMessage();
        if (message != null && !message.isEmpty()) {
            System.out.println(response.getMessage());
        }

        final var postCommand = response.getPostCommand();
        if (postCommand != null) {
            System.out.println("\nPOST_COMMAND=" + postCommand);
        }

        return response.getExitCode();
    }
}
