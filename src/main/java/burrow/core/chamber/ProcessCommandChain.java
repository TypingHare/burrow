package burrow.core.chamber;

import burrow.chain.Chain;
import burrow.core.command.CommandContext;
import burrow.core.common.Environment;
import org.springframework.lang.NonNull;

import java.util.List;

public final class ProcessCommandChain extends Chain<CommandContext> {
    public CommandContext apply(
        @NonNull final ChamberContext chamberContext,
        @NonNull final List<String> args,
        @NonNull final Environment environment
    ) {
        final var context = new CommandContext();
        CommandContext.Hook.chamberContext.set(context, chamberContext);
        CommandContext.Hook.commandArgs.set(context, args);
        CommandContext.Hook.environment.set(context, environment);

        return super.apply(context);
    }
}
