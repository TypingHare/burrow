package burrow.client;

import burrow.core.common.Environment;
import burrow.server.BurrowResponse;
import org.springframework.lang.NonNull;
import picocli.CommandLine;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Properties;

public abstract class BurrowClient {
    public static final String APPLICATION_PROPERTIES_FILE = "application.properties";

    protected final Properties properties = new Properties();
    protected final URI uri;
    protected final Environment environment = new Environment();
    protected Duration lastRequestDuration;

    public BurrowClient() throws BurrowClientInitializationException {
        // Load application properties
        try (final InputStream inputStream = getClass().getClassLoader()
            .getResourceAsStream(APPLICATION_PROPERTIES_FILE)) {
            properties.load(inputStream);
        } catch (final IOException ex) {
            throw new BurrowClientInitializationException(
                "Fail to read file: " + APPLICATION_PROPERTIES_FILE, ex);
        }

        // Get the URL of the server to send requests
        final var uriString = properties.getProperty("burrow.client.url");
        this.uri = URI.create(uriString);

        // Environment
        environment.setWorkingDirectory(System.getProperty("user.dir"));
        environment.setConsoleWidth(getConsoleWidth());
    }

    public static int getConsoleWidth() {
        final var commandSpec = CommandLine.Model.CommandSpec.create();
        final var message = commandSpec.usageMessage().autoWidth(true);
        return message.width();
    }

    public Duration getLastRequestDuration() {
        return lastRequestDuration;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public BurrowResponse sendRequestTiming(@NonNull final String command) {
        final var start = Instant.now();
        final var response = sendRequest(command);
        lastRequestDuration = Duration.between(start, Instant.now());

        return response;
    }

    protected abstract BurrowResponse sendRequest(@NonNull final String command);
}
