package me.jameschan.burrow.command.builtin;

import me.jameschan.burrow.command.Command;
import me.jameschan.burrow.context.RequestContext;
import picocli.CommandLine;

import java.util.Map;

@CommandLine.Command(
    name = "new",
    mixinStandardHelpOptions = true,
    description = "Create a new entry."
)
public class NewCommand extends Command {
    public NewCommand(final RequestContext context) {
        super(context);
    }

    @Override
    public Integer call() {
        final var hoard = context.getHoard();
        final var entry = hoard.create(Map.of());
        buffer.append("Created entry").append(entry.getId());

        return 0;
    }
}
