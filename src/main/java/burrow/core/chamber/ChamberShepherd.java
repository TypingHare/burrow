package burrow.core.chamber;

import burrow.chain.Context;
import burrow.chain.Hook;
import burrow.chain.IdentityChain;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ChamberShepherd {
    public static final Path CHAMBER_ROOT_DIR = Burrow.ROOT_DIR.resolve("chamber");
    public static final String ROOT_CHAMBER_NAME = ".";

    private static final Logger logger = LoggerFactory.getLogger(ChamberShepherd.class);

    private final ApplicationContext applicationContext;
    private final Map<String, Chamber> chamberStore = new HashMap<>();
    private final ProcessChain processChain = new ProcessChain();

    @Autowired
    public ChamberShepherd(@NonNull final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;

        processChain.pre.use((ctx) -> {
            final var chamber = ctx.getChamber();
            final var environment = ctx.getEnvironment();
            final var realArgs = ctx.getRealArgs();
            final var commandContext = chamber.execute(realArgs, environment);

            ProcessChain.commandContextHook.set(ctx, commandContext);
        });
    }

    /**
     * Initiates the root chamber.
     */
    public void initRootChamber() throws ChamberInitializationException {
        initiate(ROOT_CHAMBER_NAME);
    }

    @NonNull
    public Chamber initiate(
        @NonNull final String chamberName
    ) throws ChamberInitializationException {
        final var chamber = applicationContext.getBean(Chamber.class);
        logger.info("Initiating chamber <{}>...", chamberName);
        final var start = Instant.now();

        try {
            chamber.initiate(chamberName);
        } catch (final ChamberInitializationException ex) {
            logger.error(ex.getMessage());
            logger.error("Fail to initiate chamber <{}>", chamberName);
            throw ex;
        }

        chamberStore.put(chamberName, chamber);
        final var duration = Duration.between(start, Instant.now());
        logger.info("Completed chamber <{}> initialization in {}ms", chamberName, duration.toMillis());

        return chamber;
    }

    public void terminate(@NonNull final String chamberName) {
        final var chamber = chamberStore.get(chamberName);
        if (chamber == null) {
            logger.warn("Stop terminating chamber <{}> because it is not running", chamberName);
        } else {
            chamber.terminate();
            chamberStore.remove(chamberName);
            logger.info("Terminated chamber <{}>", chamberName);
        }
    }

    @NonNull
    public Chamber getChamber(@NonNull final String name) throws ChamberInitializationException {
        return isChamberRunning(name) ? chamberStore.get(name) : initiate(name);
    }

    public boolean isChamberRunning(@NonNull final String name) {
        return chamberStore.containsKey(name);
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

        final Chamber chamber;
        try {
            chamber = getChamber(chamberName);
        } catch (final ChamberInitializationException ex) {
            final var commandContext = new CommandContext();
            final var buffer = new StringBuilder();
            buffer.append("Fail to initialize chamber: ").append(chamberName);
            commandContext.set(CommandContext.Key.EXIT_CODE, CommandLine.ExitCode.SOFTWARE);
            commandContext.set(CommandContext.Key.BUFFER, buffer);

            return commandContext;
        }

        final var processContext = processChain.createContext(chamber, environment, realArgs);
        processChain.apply(processContext);

        return ProcessChain.commandContextHook.get(processContext);
    }

    public void terminateAll() {
        chamberStore.keySet().forEach(this::terminate);
    }

    @NonNull
    public Map<String, Chamber> getChamberStore() {
        return chamberStore;
    }

    @NonNull
    public ProcessChain getProcessChain() {
        return processChain;
    }

    public static final class ProcessContext extends Context {
        @NonNull
        public Chamber getChamber() {
            return ProcessChain.chamberHook.get(this);
        }

        @NonNull
        public Environment getEnvironment() {
            return ProcessChain.environmentHook.get(this);
        }

        @NonNull
        public List<String> getRealArgs() {
            return ProcessChain.realArgsHook.get(this);
        }

        @NonNull
        public CommandContext getCommandContext() {
            return ProcessChain.commandContextHook.get(this);
        }
    }

    public static final class ProcessChain extends IdentityChain<ProcessContext> {
        public static final Hook<Chamber> chamberHook = Hook.of(Key.CHAMBER, Chamber.class);
        public static final Hook<Environment> environmentHook =
            Hook.of(Key.ENVIRONMENT, Environment.class);
        public static final Hook<List<String>> realArgsHook = Hook.of(Key.REAL_ARGS);
        public static final Hook<CommandContext> commandContextHook =
            Hook.of(Key.COMMAND_CONTEXT, CommandContext.class);

        @NonNull
        public ProcessContext createContext(
            @NonNull Chamber chamber,
            @NonNull Environment environment,
            @NonNull List<String> realArgs
        ) {
            final var context = new ProcessContext();
            chamberHook.set(context, chamber);
            environmentHook.set(context, environment);
            realArgsHook.set(context, realArgs);
            commandContextHook.set(context, null);

            return context;
        }

        public static final class Key {
            public static final String CHAMBER = "CHAMBER";
            public static final String ENVIRONMENT = "ENVIRONMENT";
            public static final String REAL_ARGS = "REAL_ARGS";
            public static final String COMMAND_CONTEXT = "COMMAND_CONTEXT";
        }
    }
}
