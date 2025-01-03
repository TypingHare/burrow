package burrow.client.cli.command

import burrow.client.cli.BurrowCli
import burrow.kernel.terminal.BurrowCommand
import burrow.kernel.terminal.ExitCode

@BurrowCommand(
    name = BurrowCli.CliCommand.HELP,
    description = ["Displays the usage of Burrow command line interface."]
)
class HelpCommand(cli: BurrowCli) : BurrowCliCommand(cli) {
    override fun call(): Int {
        val commandClasses = mutableListOf(
            HelpCommand::class,
            UseCommand::class,
            ExitCommand::class,
            ClearCommand::class,
        )

        for (commandClass in commandClasses) {
            val burrowCommand =
                commandClass.java.getAnnotation(BurrowCommand::class.java)
            val name = burrowCommand.name
            val description = burrowCommand.description[0]
            println("${name.padEnd(10)} $description")
        }

        return ExitCode.OK
    }
}