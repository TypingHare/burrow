package burrow.core.command;

import burrow.chain.event.Event;
import burrow.core.chamber.Chamber;
import burrow.core.chamber.ChamberModule;
import burrow.core.common.ColorUtility;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine;

import java.util.HashMap;
import java.util.Map;

public final class Processor extends ChamberModule {
    public static final String DEFAULT_COMMAND_NAME = "";

    private final Map<String, Class<? extends Command>> commandClassStore = new HashMap<>();

    public Processor(@NotNull final Chamber chamber) {
        super(chamber);

        register(DefaultCommand.class);

        final var executeCommandChain = getChamber().getExecuteCommandChain();
        executeCommandChain.use(this::execute);
        executeCommandChain.on(CommandNotFoundEvent.class, CommandNotFoundEvent::handler);
    }

    @NotNull
    public Map<String, Class<? extends Command>> getCommandClassStore() {
        return commandClassStore;
    }

    @Nullable
    public Class<? extends Command> getCommand(@NotNull final String commandName) {
        return commandClassStore.get(commandName);
    }

    public void execute(@NotNull final CommandContext context, @Nullable final Runnable next) {
        final var commandName = CommandContext.Hook.commandName.getNonNull(context);
        final var commandArgs = CommandContext.Hook.commandArgs.getNonNull(context);

        if (!commandClassStore.containsKey(commandName)) {
            context.trigger(new CommandNotFoundEvent());
            return;
        }

        final var commandClass = commandClassStore.get(commandName);
        try {
            final var constructor = commandClass.getConstructor(CommandContext.class);
            final var command = constructor.newInstance(context);
            final var exitCode = new CommandLine(command)
                .setParameterExceptionHandler(command)
                .setExecutionExceptionHandler(command)
                .execute(commandArgs.toArray(new String[0]));
            CommandContext.Hook.exitCode.set(context, exitCode);
        } catch (final Throwable ex) {
            CommandContext.Hook.exitCode.set(context, CommandLine.ExitCode.SOFTWARE);
        }
    }

    public void register(@NotNull final Class<? extends Command> commandClass) {
        var commandAnnotation = Command.getCommandAnnotation(commandClass);
        commandClassStore.put(commandAnnotation.name(), commandClass);
    }

    public void disable(@NotNull final Class<? extends Command> commandClass) {
        commandClassStore.entrySet().removeIf(entry -> entry.getValue() == commandClass);
    }

    public final static class CommandNotFoundEvent extends Event {
        public static void handler(
            @NotNull final CommandContext context,
            @NotNull final CommandNotFoundEvent event
        ) {
            final var buffer = context.getBuffer();
            final var commandName = context.getCommandName();

            buffer.append("Command not found: ")
                .append(ColorUtility.render(commandName, ColorUtility.Type.NAME_COMMAND));
            CommandContext.Hook.exitCode.set(context, CommandLine.ExitCode.USAGE);
        }
    }
}
