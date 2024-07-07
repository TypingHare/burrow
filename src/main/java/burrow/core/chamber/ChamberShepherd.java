package burrow.core.chamber;

import burrow.chain.Chain;
import burrow.core.Burrow;
import burrow.core.command.CommandContext;
import burrow.core.common.CommandUtility;
import burrow.core.common.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import picocli.CommandLine;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class ChamberShepherd {
    public static final String ROOT_CHAMBER_NAME = ".";
    private static final Logger logger = LoggerFactory.getLogger(ChamberShepherd.class);
    private final Burrow burrow;
    private final Map<String, Chamber> chamberStore = new HashMap<>();
    private final Map<String, ChamberLifeCycleContext> chamberLifeCycleContextStore =
        new HashMap<>();

    private final CreateChamberChain createChamberChain = new CreateChamberChain();
    private final TerminateChamberChain terminateChamberChain = new TerminateChamberChain();
    private final ProcessCommandChain processCommandChain = new ProcessCommandChain();

    public ChamberShepherd(@NonNull final Burrow burrow) {
        this.burrow = burrow;

        processCommandChain.use(this::prepareCommand);
        processCommandChain.use(this::executeCommand);
    }

    @NonNull
    public Burrow getBurrow() {
        return burrow;
    }

    @NonNull
    public Map<String, Chamber> getChamberStore() {
        return chamberStore;
    }

    @NonNull
    public CreateChamberChain getCreateChamberChain() {
        return createChamberChain;
    }

    @NonNull
    public TerminateChamberChain getTerminateChamberChain() {
        return terminateChamberChain;
    }

    @NonNull
    public ProcessCommandChain getProcessCommandChain() {
        return processCommandChain;
    }

    public void initializeRootChamber() throws ChamberInitializationException {
        initialize(ROOT_CHAMBER_NAME);
    }

    @NonNull
    public void initialize(@NonNull final String chamberName) throws
        ChamberInitializationException {
        final var start = Instant.now();
        final var chamber = new Chamber(this, chamberName);

        try {
            chamber.initialize();
            final var context = createChamberChain.apply(chamber, start);
            chamberStore.put(chamberName, chamber);
            chamberLifeCycleContextStore.put(chamberName, context);
        } catch (final Throwable ex) {
            logger.error("Fail to start chamber <{}>", chamberName, ex);
            throw new ChamberInitializationException(ex);
        }

        final var duration = Duration.between(start, Instant.now());
        logger.info("Started chamber <{}> in {} ms", chamberName, duration.toMillis());
    }

    public boolean isChamberRunning(@NonNull final String chamberName) {
        return chamberStore.containsKey(chamberName);
    }

    @NonNull
    public Chamber getOrStartChamber(@NonNull final String chamberName) throws
        ChamberInitializationException {
        if (!isChamberRunning(chamberName)) {
            initialize(chamberName);
        }

        return Objects.requireNonNull(chamberStore.get(chamberName));
    }

    public void terminate(@NonNull final String chamberName) {
        final var chamber = chamberStore.get(chamberName);
        final var context = chamberLifeCycleContextStore.get(chamberName);

        if (chamber == null || context == null) {
            logger.warn("Stop terminating chamber <{}>, as it is not running", chamberName);
        } else {
            final var start = Instant.now();
            terminateChamberChain.apply(context);
            final var duration = Duration.between(start, Instant.now());
            logger.info("Terminated chamber <{}> in {} ms", chamberName, duration.toMillis());
        }

        chamberStore.remove(chamberName);
        chamberLifeCycleContextStore.remove(chamberName);
    }

    public void terminateAll() {
        chamberStore.values().forEach(Chamber::terminate);
    }

    @NonNull
    public CommandContext process(
        @NonNull final String command,
        @NonNull final Environment environment
    ) {
        final var args = CommandUtility.splitArguments(command);
        final var hasChamber = !args.isEmpty() && !args.getFirst().startsWith("-");
        final var chamberName = hasChamber ? args.getFirst() : ROOT_CHAMBER_NAME;
        final var realArgs = hasChamber ? args.subList(1, args.size()) : args;

        final Chamber chamber;
        try {
            chamber = getOrStartChamber(chamberName);
        } catch (final ChamberInitializationException ex) {
            final var commandContext = new CommandContext();
            final var buffer = new StringBuilder();
            buffer.append("Fail to initialize chamber: ").append(chamberName);

            commandContext.setBuffer(buffer);
            commandContext.setExitCode(CommandLine.ExitCode.SOFTWARE);

            return commandContext;
        }

        return processCommandChain.apply(chamber.getChamberContext(), realArgs, environment);
    }

    public void prepareCommand(
        @NonNull final CommandContext context,
        @Nullable final Runnable next
    ) {
        final var args = context.getCommandArgs();
        final var hasCommand = !args.isEmpty() && !args.getFirst().startsWith("-");
        final var commandName = hasCommand ? args.getFirst() : "";
        final var realArgs = hasCommand ? args.subList(1, args.size()) : args;

        context.setCommandName(commandName);
        context.setCommandArgs(realArgs);
        context.setExitCode(CommandLine.ExitCode.SOFTWARE);
        context.setBuffer(new StringBuilder());

        Chain.runIfNotNull(next);
    }

    public void executeCommand(
        @NonNull final CommandContext context,
        @Nullable final Runnable next
    ) {
        Chain.runIfNotNull(next);

        context.getChamberContext().getChamber().getExecuteCommandChain().apply(context);
    }
}
