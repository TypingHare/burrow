package burrow.core.command;

import burrow.chain.event.Event;
import burrow.core.chamber.Chamber;
import burrow.core.chamber.ChamberModule;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Processor extends ChamberModule {
    public static final String DEFAULT_COMMAND_NAME = "";

    private final Map<String, Class<? extends Command>> commandClassStore = new HashMap<>();

    public Processor(@NonNull final Chamber chamber) {
        super(chamber);

        register(DefaultCommand.class);

        final var commandProcessChain = chamber.getContext()
            .getOverseer()
            .getCommandProcessChain();

        commandProcessChain.pre.use(this::execute);
        commandProcessChain.on(CommandNotFoundEvent.class, CommandNotFoundEvent::handler);
    }

    public void execute(@NonNull final CommandContext ctx) {
        final var commandName = ctx.getCommandName();
        final var args = ctx.getCommandArgs();

        if (!commandClassStore.containsKey(commandName)) {
            ctx.trigger(new CommandNotFoundEvent());
            return;
        }

        final var commandClass = commandClassStore.get(commandName);
        try {
            final var constructor = commandClass.getConstructor(CommandContext.class);
            final var command = constructor.newInstance(ctx);
            final var exitCode = new CommandLine(command)
                .setParameterExceptionHandler(command)
                .setExecutionExceptionHandler(command)
                .execute(args.toArray(new String[0]));
            ctx.set(CommandContext.Key.EXIT_CODE, exitCode);
        } catch (final Throwable ex) {
            ctx.set(CommandContext.Key.EXIT_CODE, CommandLine.ExitCode.SOFTWARE);
        }
    }

    public void register(@NonNull final Class<? extends Command> commandClass) {
        var commandAnnotation = Command.getCommandAnnotation(commandClass);
        commandClassStore.put(commandAnnotation.name(), commandClass);
    }

    public void disable(@NonNull final Class<? extends Command> commandClass) {
        commandClassStore.entrySet().removeIf(entry -> entry.getValue() == commandClass);
    }

    @NonNull
    public Collection<Class<? extends Command>> getAllCommands() {
        return commandClassStore.values();
    }

    @Nullable
    public Class<? extends Command> getCommand(@NonNull final String commandName) {
        return commandClassStore.get(commandName);
    }

    public final static class CommandNotFoundEvent extends Event {
        public static void handler(
            @NonNull final CommandContext ctx, @NonNull final CommandNotFoundEvent event) {
            ctx.getBuffer().append("Command not found: ").append(ctx.getCommandName());
            ctx.set(CommandContext.Key.EXIT_CODE, CommandLine.ExitCode.USAGE);
        }
    }
}
