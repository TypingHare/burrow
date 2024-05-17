package me.jameschan.burrow.furnitures.config.command;

import me.jameschan.burrow.command.Command;
import me.jameschan.burrow.context.RequestContext;
import picocli.CommandLine;

/**
 * Retrieve or update configuration.
 */
@CommandLine.Command(name = "config", mixinStandardHelpOptions = true)
public class ConfigCommand extends Command {
    @CommandLine.Parameters(
        index = "0",
        description = "The configuration key",
        defaultValue = ""
    )
    private String key;

    @CommandLine.Parameters(
        index = "1",
        description = "The configuration value",
        defaultValue = ""
    )
    private String value;

    public ConfigCommand(final RequestContext context) {
        super(context);
    }

    @Override
    public Integer call() {
        final var config = context.getConfig();
        final var buffer = context.getBuffer();

        if (value.isEmpty()) {
            if (key.isEmpty()) {
                // Print the entire configuration
                final var configData = context.getConfig().getData();
                buffer.append(configData);
            } else {
                // Retrieve configuration
                buffer.append(String.format("%s -> \"%s\"%n", key, config.get(key)));
            }
        } else {
            // Update configuration
            config.set(key, value);
            buffer.append(String.format("%s -> \"%s\"%n", key, value));
            context.getChamber().saveConfig();
        }

        return 0;
    }
}
