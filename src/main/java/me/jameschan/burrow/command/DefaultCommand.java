package me.jameschan.burrow.command;

import me.jameschan.burrow.config.Config;
import me.jameschan.burrow.context.RequestContext;
import picocli.CommandLine;

@CommandLine.Command(name = "", description = "Prints the help information of the application.")
public class DefaultCommand extends Command {
    public DefaultCommand(final RequestContext context) {
        super(context);
    }

    @CommandLine.Option(
        names = {"-v", "--version"},
        description = "Print the name and version of the app being used."
    )
    private boolean version = false;

    @CommandLine.Option(
        names = {"-h", "--help"},
        description = "Print help information."
    )
    private boolean help = false;

    @Override
    public Integer call() {
        final var config = context.getConfig();
        final var buffer = context.getBuffer();

        if (version) {
            final var appName = config.get(Config.Key.APP_NAME);
            final var appVersion = config.get(Config.Key.APP_VERSION);
            buffer.append(String.format("%s v%s%n", appName, appVersion));
        }

        if (help) {
            buffer.append("There is nothing I can help you with.\n");
        }

        return 0;
    }
}