package burrow.furniture.standard;

import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import picocli.CommandLine;

import java.util.ArrayList;

@CommandLine.Command(name = "fls", description = "Prints the list of furniture of this chamber.")
@CommandType(StandardFurniture.COMMAND_TYPE)
public class FurnitureListCommand extends Command {
    @CommandLine.Option(
        names = {"-f", "--full"},
        defaultValue = "false",
        description = "Whether print furniture list in simple-name form.")
    private Boolean listFullNames;

    public FurnitureListCommand(final CommandContext commandContext) {
        super(commandContext);
    }

    @Override
    public Integer call() {
        final var lines = new ArrayList<String>();

        if (listFullNames) {
            final var nameList = StandardFurniture.getFurnitureFullNameList(context);
            var i = 0;
            for (final var name : nameList) {
                lines.add(String.format("[%d] %s", i++, name));
            }
        } else {
            final var fullNameMap = StandardFurniture.getFurnitureFullNameMap(context);
            var i = 0;
            for (final var name : fullNameMap.keySet()) {
                final var fullNameList = fullNameMap.get(name);
                if (fullNameList.size() == 1) {
                    lines.add(String.format("[%d] %s", i++, name));
                } else {
                    for (final String fullName : fullNameList) {
                        lines.add(String.format("[%d] %s (%s)", i++, name, fullName));
                    }
                }
            }
        }

        bufferAppendLines(lines);

        return CommandLine.ExitCode.OK;
    }
}
