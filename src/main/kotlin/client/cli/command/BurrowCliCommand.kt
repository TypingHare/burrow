package burrow.client.cli.command

import burrow.client.cli.BurrowCli
import java.util.concurrent.Callable

abstract class BurrowCliCommand(protected val cli: BurrowCli) : Callable<Int>