package burrow.client.cli

import picocli.CommandLine.ExitCode
import java.util.concurrent.Callable

abstract class BurrowClientCliCommand(protected val cli: BurrowClientCli) :
    Callable<Int> {
    override fun call(): Int {
        return ExitCode.OK
    }
}