package burrow.client;

import burrow.core.Burrow;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jetbrains.annotations.NotNull;

public final class LocalBurrowClient extends BurrowClient {
    private final Burrow burrow;

    public LocalBurrowClient() throws BurrowClientInitializationException {
        redirectLogs();
        burrow = new Burrow();
    }

    @Override
    protected BurrowResponse sendRequest(@NotNull final String command) {
        final var commandContext = burrow.getChamberShepherd().process(command, getEnvironment());
        final var burrowResponse = new BurrowResponse();
        burrowResponse.setMessage(commandContext.getBuffer().toString());
        burrowResponse.setExitCode(commandContext.getExitCode());

        return burrowResponse;
    }

    @Override
    public void close() {
        burrow.shutdown();
    }

    private void redirectLogs() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        // Create a pattern layout encoder
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern("%d{yyyy-MM-dd HH:mm:ss} %-5level %logger{36} - %msg%n");
        encoder.start();

        // Create a file appender
        FileAppender<ILoggingEvent> fileAppender = new FileAppender<>();
        fileAppender.setContext(context);
        fileAppender.setName("fileAppender");
        fileAppender.setFile(Burrow.LOGS_ROOT_DIR.toString());
        fileAppender.setEncoder(encoder);
        fileAppender.start();

        // Get the root logger and add the appender
        final var rootLogger =
            (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.detachAppender("console");
        rootLogger.addAppender(fileAppender);
    }
}
