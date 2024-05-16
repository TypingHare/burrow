package me.jameschan.burrow.command;

import me.jameschan.burrow.context.RequestContext;
import picocli.CommandLine;

@CommandLine.Command(name = "unknown")
public class UnknownCommand extends Command {
    public UnknownCommand(RequestContext context) {
        super(context);
    }

    @Override
    public Integer call() {
        return 0;
    }
}
