package me.jameschan.burrow.command.builtin;

import me.jameschan.burrow.command.Command;
import me.jameschan.burrow.context.RequestContext;
import picocli.CommandLine;

@CommandLine.Command(
    name = "entry",
    mixinStandardHelpOptions = true,
    description = "Find an entry."
)
public class EntryCommand extends Command {
    @CommandLine.Parameters(index = "0")
    private Integer id;

    public EntryCommand(RequestContext context) {
        super(context);
    }

    @Override
    public Integer call() {
        final var hoard = context.getHoard();
        final var entry = hoard.getById(id);
        buffer.append("[").append(entry.getId()).append("] { }");

        return 0;
    }
}
