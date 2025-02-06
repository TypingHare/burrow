package burrow.carton.{{carton_name}}.command

import burrow.kernel.terminal.*

@BurrowCommand(
    name = "list",
    header = ["A template list command."],
)
class ListCommand(data: CommandData) : Command(data) {
    override fun call(): Int {
        return ExitCode.OK
    }
}