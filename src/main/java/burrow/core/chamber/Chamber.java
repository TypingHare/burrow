package burrow.core.chamber;

import burrow.core.command.CommandContext;
import burrow.core.command.Processor;
import burrow.core.common.Environment;
import burrow.core.config.Config;
import burrow.core.entry.Hoard;
import burrow.core.furniture.Renovator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.util.List;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Chamber {
    private final ApplicationContext applicationContext;
    private final ChamberContext context;

    @Autowired
    public Chamber(
        final ApplicationContext applicationContext,
        final ChamberContext chamberContext
    ) {
        this.applicationContext = applicationContext;
        this.context = chamberContext;
    }

    @NonNull
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @NonNull
    public ChamberContext getContext() {
        return context;
    }

    public void execute(
        @NonNull final Environment environment,
        @NonNull final List<String> args
    ) {
        final var hasCommand = !args.isEmpty() && !args.getFirst().startsWith("-");
        final var commandName = hasCommand ? args.getFirst() : "";
        final var realArgs = hasCommand ? args.subList(1, args.size()) : args;

        final var commandContext = new CommandContext();
        commandContext.set(CommandContext.Key.CHAMBER_CONTEXT, context);
        commandContext.set(CommandContext.Key.COMMAND_NAME, commandName);
        commandContext.set(CommandContext.Key.COMMAND_ARGS, realArgs);
        commandContext.set(CommandContext.Key.WORKING_DIRECTORY, environment.getWorkingDirectory());
        commandContext.set(CommandContext.Key.EXIT_CODE, CommandLine.ExitCode.SOFTWARE);
        commandContext.set(CommandContext.Key.BUFFER, new StringBuilder());

        final var commandProcessChain = context.getOverseer().getCommandProcessChain();
        commandProcessChain.apply(commandContext);
    }

    public void initiate(final String name) throws ChamberInitializationException {
        context.set(ChamberContext.Key.CHAMBER, this);
        context.set(ChamberContext.Key.CHAMBER_NAME, name);
        context.set(ChamberContext.Key.CONFIG, getModuleObject(Config.class));
        context.set(ChamberContext.Key.HOARD, getModuleObject(Hoard.class));
        context.set(ChamberContext.Key.RENOVATOR, getModuleObject(Renovator.class));
        context.set(ChamberContext.Key.PROCESSOR, getModuleObject(Processor.class));

        try {
            checkChamberDirectory(name);
            context.getConfig().loadFromFile();
            context.getRenovator().loadFurniture();
            context.getHoard().loadFromFile();
        } catch (final Throwable ex) {
            throw new ChamberInitializationException(ex);
        }
    }

    private void checkChamberDirectory(final String name) throws ChamberNotFoundException {
        final var dirPath = ChamberShepherd.CHAMBER_ROOT_DIR.resolve(name).normalize();
        final var file = dirPath.toFile();
        if (!file.isDirectory()) {
            throw new ChamberNotFoundException(name);
        }

        context.set(ChamberContext.Key.ROOT_DIR, dirPath);
    }

    public void terminate() {
        context.getHoard().saveToFile();
        context.getRenovator().terminateAllFurniture();
    }

    public void restart() throws ChamberInitializationException {
        terminate();

        final var chamberShepherd = applicationContext.getBean(ChamberShepherd.class);
        chamberShepherd.initiate(getContext().getChamberName());
    }

    private <T extends ChamberModule> T getModuleObject(final Class<T> moduleClass) {
        return applicationContext.getBean(moduleClass, this);
    }
}
