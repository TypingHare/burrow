package burrow.furniture.script;

import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import burrow.core.common.ColorUtility;
import burrow.core.common.Values;
import burrow.furniture.hoard.HoardFurniture;
import burrow.furniture.pair.PairFurniture;
import burrow.furniture.shellcore.ShellCoreFurniture;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.io.IOException;
import java.util.ArrayList;

@CommandLine.Command(
    name = "exec",
    description = "Execute the command associated with a given label."
)
@CommandType(ScriptFurniture.COMMAND_TYPE)
public class ExecCommand extends Command {
    @CommandLine.Parameters(
        index = "0",
        description = "The label associated with the command."
    )
    private String label;

    @CommandLine.Option(
        names = {"--post", "-p"},
        description = "Post executes the command.",
        defaultValue = "false"
    )
    private Boolean post;

    public ExecCommand(@NotNull final CommandContext commandContext) {
        super(commandContext);
    }

    @Override
    public Integer call() throws IOException, InterruptedException {
        final var pairFurniture = use(PairFurniture.class);
        final var idSet = pairFurniture.getIdSetByKey(label);
        if (idSet.isEmpty()) {
            final var errorMessage = "No command associated with such label: " + label;
            buffer.append(ColorUtility.render(errorMessage, ColorUtility.Type.MESSAGE_ERROR));
            return CommandLine.ExitCode.USAGE;
        }

        final var id = new ArrayList<>(idSet).getFirst();
        final var hoard = use(HoardFurniture.class).getHoard();
        final var properties = hoard.get(id).getProperties();
        final var command = properties.get(ScriptFurniture.EntryKey.COMMAND);

        final var workingDirectory = properties.getOrDefault(
            ScriptFurniture.EntryKey.WORKING_DIRECTORY,
            commandContext.getEnvironment().getWorkingDirectory());
        commandContext.getEnvironment().setWorkingDirectory(workingDirectory);

        final var isPost = Values.Bool.isTrue(properties.get(ScriptFurniture.EntryKey.IS_POST));
        if (isPost || post) {
            commandContext.setPostCommand(command);
        } else {
            use(ShellCoreFurniture.class).dispatch(commandContext, command);
        }

        return CommandLine.ExitCode.OK;
    }
}
