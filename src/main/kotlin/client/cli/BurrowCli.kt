package burrow.client.cli

import burrow.carton.server.Endpoint
import burrow.client.Client
import burrow.client.CommandLexer
import burrow.client.LocalClient
import burrow.client.SocketBasedClient
import burrow.client.cli.command.ClearCommand
import burrow.client.cli.command.ExitCommand
import burrow.client.cli.command.HelpCommand
import burrow.client.cli.command.UseCommand
import burrow.kernel.BuildBurrowException
import burrow.kernel.Burrow
import burrow.kernel.chamber.ChamberShepherd
import burrow.kernel.terminal.BurrowCommand
import burrow.kernel.terminal.ExitCode
import burrow.kernel.terminal.Option
import burrow.kernel.terminal.Parameters
import org.jline.reader.LineReader
import org.jline.reader.LineReaderBuilder
import org.jline.reader.UserInterruptException
import org.jline.reader.impl.history.DefaultHistory
import org.jline.terminal.Terminal
import org.jline.terminal.TerminalBuilder
import picocli.CommandLine
import java.io.IOException
import java.net.SocketException
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.time.Instant
import java.util.concurrent.Callable
import kotlin.system.exitProcess

@BurrowCommand(
    name = "burrow-cli",
    version = [Burrow.VERSION],
    description = ["Burrow command-line interface."]
)
class BurrowCli : Callable<Int> {
    @Parameters(
        index = "0",
        paramLabel = "<initial-chamber-name>",
        description = ["The name of the initial chamber."],
        defaultValue = ChamberShepherd.ROOT_CHAMBER_NAME
    )
    var initialChamberName = ""

    @Option(
        names = ["-v", "--version"],
        description = ["Display the version of Burrow CLI."],
        defaultValue = "false"
    )
    private var version = false

    @Option(
        names = ["-r", "--server"],
        paramLabel = "<server>",
        description = ["Server host and port."],
        defaultValue = ""
    )
    private var server = ""

    private var endpoint = Endpoint("localhost", 4710)
    private var client: Client? = null
    var terminal: Terminal? = null

    private var currentChamberName = ChamberShepherd.ROOT_CHAMBER_NAME
    private var lastExecutionDuration: Duration? = null
    private var lastExecutionExitCode: Int? = null

    override fun call(): Int {
        if (version) {
            val annotation = javaClass.getAnnotation(BurrowCommand::class.java)
            val name = annotation.name
            val version = annotation.version[0]
            println("$name v$version")

            return ExitCode.OK
        }

        initializeBurrowClient()

        if (initialChamberName != currentChamberName) {
            useChamber(initialChamberName)
        }

        val reader = initializeTerminal()
        CommandLine(ClearCommand(this)).execute()

        while (true) {
            try {
                val command = reader.readLine(getPromptString()).trim()
                if (command.isNotEmpty()) {
                    executeCommand(command)
                }
            } catch (ex: UserInterruptException) {
                exit()
            } catch (ex: BuildBurrowException) {
                System.err.println("Failed to initialize burrow!")
                exitProcess(ExitCode.SOFTWARE)
            }
        }
    }

    private fun executeCommand(command: String) {
        if (command.startsWith("/")) {
            val args = CommandLexer.tokenizeCommandString(command)
            when (val commandName = args[0].substring(1).lowercase()) {
                CliCommand.HELP -> CommandLine(HelpCommand(this)).execute()
                CliCommand.EXIT -> CommandLine(ExitCommand(this)).execute()
                CliCommand.USE -> CommandLine(UseCommand(this)).execute(
                    *args.drop(1).toTypedArray()
                )
                CliCommand.CLEAR -> CommandLine(ClearCommand(this)).execute()
                else -> {
                    println("Invalid CLI command: $commandName")
                }
            }

            lastExecutionDuration = null
            lastExecutionExitCode = null

            println()
            return
        }

        var currentChamberName = this.currentChamberName
        var realCommand = command
        if (command.startsWith(ROOT_CHAMBER_CALLER)) {
            currentChamberName = ChamberShepherd.ROOT_CHAMBER_NAME
            realCommand = command.substring(ROOT_CHAMBER_CALLER.length)
        }

        val client = this.client!!
        val fullCommand = "$currentChamberName $realCommand"
        val start = Instant.now()

        try {
            lastExecutionExitCode = client.executeCommand(fullCommand)
        } catch (ex: SocketException) {
            println("Failed to connected to the server. Reconnecting...")
            connect()
            executeCommand(command)
        } finally {
            lastExecutionDuration = Duration.between(start, Instant.now())
            println()
        }
    }

    fun exit() {
        try {
            client?.close()
            terminal?.close()
        } catch (ignored: Throwable) {
        } finally {
            println(BYE_MESSAGE)
            exitProcess(ExitCode.OK)
        }
    }

    private fun initializeBurrowClient() {
        if (server.isEmpty()) {
            client = LocalClient()
        } else {
            try {
                val (host, port) = server.split(":")
                endpoint = Endpoint(host, port.toInt())
            } catch (ex: Exception) {
                System.err.println("Invalid server: $server")
            }

            connect()
        }
    }

    private fun connect() {
        for (attempt in 1..INITIALIZATION_CONNECT_ATTEMPTS) {
            try {
                client = SocketBasedClient(endpoint)
                return
            } catch (ex: Exception) {
                if (attempt < INITIALIZATION_CONNECT_ATTEMPTS) {
                    val delay = attempt * attempt * 500
                    System.err.println("Attempting to reconnect in $delay milliseconds.")
                    Thread.sleep(delay.toLong())
                }
            }
        }

        throw IOException("Failed to connect to $endpoint")
    }

    private fun initializeTerminal(): LineReader {
        terminal = TerminalBuilder.builder()
            .encoding(StandardCharsets.UTF_8)
            .build()

        return LineReaderBuilder.builder()
            .terminal(terminal)
            .history(DefaultHistory())
            .option(LineReader.Option.DISABLE_EVENT_EXPANSION, true)
            .build()
    }

    fun useChamber(chamberName: String) {
        if (!checkChamberExist(chamberName)) {
            println("Chamber does not exist: $chamberName")
            listAllAvailableChambers()
        } else {
            currentChamberName = chamberName
        }
    }

    private fun checkChamberExist(chamberName: String): Boolean {
        try {
            val client = client!!
            val exitCode = client.executeCommand(
                ". chamber.exist $chamberName --quiet --blueprint"
            )
            return exitCode == ExitCode.OK
        } catch (ex: Exception) {
            return false
        }
    }

    private fun getPromptString(): String {
        val exitCodeString = lastExecutionExitCode.let { it?.toString() ?: "" }
        val durationString = lastExecutionDuration.let {
            if (it != null) "${it.toMillis()}ms" else ""
        }

        return """
            $currentChamberName  $exitCodeString  $durationString
            ➜ 
        """.trimIndent()
    }

    private fun listAllAvailableChambers() {
        val client = client!!
        println("Available blueprints are as follows:")
        client.executeCommand(". chamber.list --blueprint")
    }

    companion object {
        const val INITIALIZATION_CONNECT_ATTEMPTS = 3
        const val ROOT_CHAMBER_CALLER = "@"
        const val BYE_MESSAGE = "Bye | 再见 | 得閒飲茶 | またね"

        @JvmStatic
        fun main(args: Array<String>) {
            System.setProperty("slf4j.internal.verbosity", "WARN")
            CommandLine(BurrowCli()).execute(*args)
        }
    }

    object CliCommand {
        const val HELP = "help"
        const val EXIT = "exit"
        const val USE = "use"
        const val CLEAR = "clear"
    }
}