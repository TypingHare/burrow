package burrow.client;

import burrow.core.common.Environment;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Properties;

public abstract class BurrowClient implements Closeable {
    public static final String APPLICATION_PROPERTIES_FILE = "application.properties";

    protected final Properties properties = new Properties();
    protected final URI uri;
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
    }

    public static int getConsoleWidth() {
        final var commandSpec = CommandLine.Model.CommandSpec.create();
        final var message = commandSpec.usageMessage().autoWidth(true);
        return message.width();
    }

    @NotNull
    public Duration getLastRequestDuration() {
        return lastRequestDuration;
    }

    @NotNull
    public Environment getEnvironment() {
        final var environment = new Environment();
        environment.setWorkingDirectory(System.getProperty("user.dir"));
        environment.setConsoleWidth(getConsoleWidth());

        return environment;
    }

    @NotNull
    public BurrowResponse sendRequestTiming(@NotNull final String command) {
        final var start = Instant.now();
        final var response = sendRequest(command);
        lastRequestDuration = Duration.between(start, Instant.now());

        return response;
    }

    protected abstract BurrowResponse sendRequest(@NotNull final String command);

    @Override
    public void close() {}
}
