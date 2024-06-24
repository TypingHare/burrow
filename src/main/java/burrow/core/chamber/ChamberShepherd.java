package burrow.core.chamber;

import burrow.core.Burrow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

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
    public ChamberShepherd(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Initiates the root chamber.
     */
    public void init() throws ChamberInitializationException {
        initiate(ROOT_CHAMBER_NAME);
    }

    @NonNull
    public Chamber initiate(@NonNull final String chamberName) throws ChamberInitializationException {
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

    public void terminate(final String chamberName) {
        final var chamber = chamberStore.get(chamberName);
        if (chamber == null) {
            logger.warn("Chamber not found: {}", chamberName);
        } else {
            chamber.terminate();
            chamberStore.remove(chamberName);
            logger.info("Chamber terminated: {}", chamberName);
        }
    }

    public Chamber getChamber(final String name) throws ChamberInitializationException {
        return chamberStore.containsKey(name) ? chamberStore.get(name) : initiate(name);
    }

//    public BurrowResponse processRequest(final BurrowRequest request) {
//        final var args = CommandUtility.splitArguments(request.getCommand());
//        final var hasChamber = !args.isEmpty() && !args.getFirst().startsWith("-");
//        final var chamberName = hasChamber ? args.getFirst() : ROOT_CHAMBER_NAME;
//        final var realArgs = hasChamber ? args.subList(1, args.size()) : args;
//
//        final BurrowResponse response = new BurrowResponse();
//        try {
//            final var chamber = getChamber(chamberName);
//            final var requestContext = new RequestContext();
//            requestContext.set(RequestContext.Key.WORKING_DIRECTORY, request);
//
//            // Trigger before execution listeners
//            beforeExecutionListeners.forEach(listener -> listener.accept(chamber, requestContext));
//            chamber.execute(requestContext, realArgs);
//            response.setMessage(requestContext.getBuffer().toString());
//            response.setCode(requestContext.getExitCode());
//            response.setImmediateCommand(requestContext.getImmediateCommand());
//
//            // Trigger after execution listeners
//            afterExecutionListeners.forEach(listener -> listener.accept(chamber, requestContext));
//        } catch (final Throwable ex) {
//            response.setMessage(String.join("\n", ErrorUtility.getStackTrace(ex)));
//            response.setCode(ExitCode.ERROR);
//        }
//
//        return response;
//    }

    public void terminateAll() {
        chamberStore.keySet().forEach(this::terminate);
    }
}