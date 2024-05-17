package me.jameschan.burrow.command.builtin;

import me.jameschan.burrow.command.Command;
import me.jameschan.burrow.context.RequestContext;
import picocli.CommandLine;

@CommandLine.Command(
    name = "exist",
    mixinStandardHelpOptions = true,
    description = "Check if an entry exist."
)
public class ExistCommand extends Command {
    @CommandLine.Parameters(index = "0")
    private Integer id;

    public ExistCommand(RequestContext context) {
        super(context);
    }

    @Override
    public Integer call() {
        final var hoard = context.getHoard();
        buffer.append(hoard.exist(id) ? "true" : "false");

        return 0;
    }
}
