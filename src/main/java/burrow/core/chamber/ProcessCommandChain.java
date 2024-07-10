package burrow.core.chamber;

import burrow.chain.Chain;
import burrow.core.command.CommandContext;
import burrow.core.common.Environment;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class ProcessCommandChain extends Chain<CommandContext> {
    public CommandContext apply(
        @NotNull final ChamberContext chamberContext,
        @NotNull final List<String> args,
        @NotNull final Environment environment
    ) {
        final var context = new CommandContext();
        context.setChamberContext(chamberContext);
        context.setCommandArgs(args);
        context.setEnvironment(environment);

        return super.apply(context);
    }
}
