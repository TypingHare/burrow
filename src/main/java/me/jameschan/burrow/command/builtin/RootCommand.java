package me.jameschan.burrow.command.builtin;

import me.jameschan.burrow.command.Command;
import me.jameschan.burrow.context.RequestContext;
import picocli.CommandLine;

@CommandLine.Command(
    name = "root",
    mixinStandardHelpOptions = true,
    description = "Prints the root directory of the application."
)
public class RootCommand extends Command {
    public RootCommand(final RequestContext context) {
        super(context);
    }

    @Override
    public Integer call() {
        buffer.append(context.getRootDir().toString());

        return 0;
    }
}
