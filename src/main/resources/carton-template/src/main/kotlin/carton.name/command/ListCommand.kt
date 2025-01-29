package burrow.carton.name.command

import burrow.kernel.terminal.BurrowCommand
import burrow.kernel.terminal.Command
import burrow.kernel.terminal.CommandData

@BurrowCommand(
    name = "list",
    header = ["A template list command."],
)
class ListCommand(data: CommandData) : Command(data)