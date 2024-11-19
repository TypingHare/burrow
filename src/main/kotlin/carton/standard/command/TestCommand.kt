package burrow.carton.standard.command

import burrow.kernel.command.Command
import burrow.kernel.command.CommandData
import picocli.CommandLine

@CommandLine.Command(
    name = "test",
    description = ["Tests if the chamber exists."]
)
class TestCommand(data: CommandData) : Command(data)