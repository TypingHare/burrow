package burrow.core.chamber;

import burrow.core.Burrow;
import burrow.core.command.CommandContext;
import burrow.core.common.CommandUtility;
import burrow.core.common.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChamberShepherd {
    public static final Path CHAMBER_ROOT_DIR = Burrow.ROOT_DIR.resolve("chamber");
    public static final String ROOT_CHAMBER_NAME = ".";

    private static final Logger logger = LoggerFactory.getLogger(ChamberShepherd.class);

    private final ApplicationContext applicationContext;
    private final Map<String, Chamber> chamberStore = new ConcurrentHashMap<>();

    @Autowired
    public ChamberShepherd(@NonNull final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Initiates the root chamber.
     */
    public void initRootChamber() throws ChamberInitializationException {
        initiate(ROOT_CHAMBER_NAME);
    }

    @NonNull
    public Chamber initiate(
        @NonNull final String chamberName) throws ChamberInitializationException {
        final var chamber = applicationContext.getBean(Chamber.class);
        logger.info("Chamber initiating: {}", chamberName);
        final var start = Instant.now();

        try {
            chamber.initiate(chamberName);
        } catch (final ChamberInitializationException ex) {
            logger.error(ex.getMessage());
            logger.error("Fail to initiate chamber: {}", chamberName);
            throw ex;
        }

        chamberStore.put(chamberName, chamber);
        final var duration = Duration.between(start, Instant.now());
        logger.info("Chamber initiated: {} ({} ms)", chamberName, duration.toMillis());

        return chamber;
    }

    public void terminate(@NonNull final String chamberName) {
        final var chamber = chamberStore.get(chamberName);
        if (chamber == null) {
            logger.warn("Chamber not found: {}", chamberName);
        } else {
            chamber.terminate();
            chamberStore.remove(chamberName);
            logger.info("Chamber terminated: {}", chamberName);
        }
    }

    @NonNull
    public Chamber getChamber(@NonNull final String name) throws ChamberInitializationException {
        return chamberStore.containsKey(name) ? chamberStore.get(name) : initiate(name);
    }

    @NonNull
    public CommandContext processCommand(
        @NonNull final String command,
        @NonNull final Environment environment
    ) {
        final var args = CommandUtility.splitArguments(command);
        final var hasChamber = !args.isEmpty() && !args.getFirst().startsWith("-");
        final var chamberName = hasChamber ? args.getFirst() : ROOT_CHAMBER_NAME;
        final var realArgs = hasChamber ? args.subList(1, args.size()) : args;

        try {
            final var chamber = getChamber(chamberName);
            return chamber.execute(realArgs, environment);
        } catch (final ChamberInitializationException ex) {
            final var commandContext = new CommandContext();
            final var buffer = new StringBuilder();
            buffer.append("Fail to initialize chamber: ").append(chamberName);
            commandContext.set(CommandContext.Key.EXIT_CODE, CommandLine.ExitCode.SOFTWARE);
            commandContext.set(CommandContext.Key.BUFFER, buffer);

            return commandContext;
        }
    }

    public void terminateAll() {
        chamberStore.keySet().forEach(this::terminate);
    }
}
