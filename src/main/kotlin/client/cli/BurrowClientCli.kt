package burrow.client.cli

import ExitCommand
import burrow.client.Client
import burrow.client.LocalClient
import burrow.client.SocketBasedClient
import burrow.common.CommandLexer
import burrow.kernel.Burrow
import burrow.kernel.BurrowInitializationException
import burrow.kernel.palette.PicocliPalette
import org.jline.reader.LineReader
import org.jline.reader.LineReaderBuilder
import org.jline.reader.UserInterruptException
import org.jline.reader.impl.history.DefaultHistory
import org.jline.terminal.Terminal
import org.jline.terminal.TerminalBuilder
import picocli.CommandLine
import picocli.CommandLine.*
import java.net.SocketException
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.Callable
import kotlin.system.exitProcess


@Command(
    name = "burrow-cli",
    version = [Burrow.VERSION.NAME]
)
class BurrowClientCli : Callable<Int> {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            CommandLine(BurrowClientCli()).execute(*args)
        }
    }

    @Parameters(
        index = "0",
        paramLabel = "<initial-chamber-name>",
        description = ["The name of the initial chamber."],
        defaultValue = Burrow.Standard.ROOT_CHAMBER_NAME
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

    private var host = "localhost"
    private var port = 0

    private var client: Client? = null

    var terminal: Terminal? = null

    private var currentChamberName: String = Burrow.Standard.ROOT_CHAMBER_NAME

    override fun call(): Int {
        if (version) {
            val annotation = javaClass.getAnnotation(Command::class.java)
            val name = annotation.name
            val version = annotation.version
            println(name + " v" + version[0])

            return ExitCode.OK
        }

        initializeBurrowClient()

        if (initialChamberName != currentChamberName) {
            useChamber(initialChamberName)
        }

        val reader = initializeTerminal()
        while (true) {
            try {
                val command = reader.readLine(getPromptString()).trim()
                if (command.isNotEmpty()) {
                    executeCommand(command)
                }
            } catch (ex: UserInterruptException) {
                exit()
            } catch (ex: BurrowInitializationException) {
                System.err.println("Failed to initialize burrow!")
                exitProcess(ExitCode.SOFTWARE)
            }
        }
    }

    private fun initializeBurrowClient() {
        if (server.isEmpty()) {
            client = LocalClient()
        } else {
            val sp = server.split(":")
            if (sp.size != 2) {
                System.err.println("Invalid server: $server")
                exitProcess(ExitCode.USAGE)
            }

            host = sp[0].trim()
            port = sp[1].toInt()
            connect(Default.INITIALIZATION_CONNECT_ATTEMPTS)
        }
    }

    private fun connect(attempts: Int): Boolean {
        for (attempt in 1..attempts) {
            try {
                client = SocketBasedClient(host, port)
                return true
            } catch (ex: Exception) {
                System.err.println("Failed to connect to server: $host:$port")

                if (attempt < attempts) {
                    val delay = attempt * attempt * 500
                    System.err.println("Attempting to connect again in $delay milliseconds.")
                    Thread.sleep(delay.toLong())
                }
            }
        }

        return false
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

    private fun getPromptString(): String {
        return PicocliPalette().color(
            "$currentChamberName> ",
            Burrow.Highlights.CHAMBER
        )
    }

    private fun executeCommand(command: String) {
        if (command.startsWith("/")) {
            val args = CommandLexer.tokenizeCommandString(command)
            val commandName =
                args[0].substring(1).lowercase(Locale.getDefault())
            when (commandName) {
                CliCommand.COMMANDS -> CommandLine(CommandsCommand(this)).execute()
                CliCommand.EXIT -> CommandLine(ExitCommand(this)).execute()
                CliCommand.USE -> CommandLine(UseCommand(this)).execute(
                    *args.drop(1).toTypedArray()
                )

                CliCommand.CLEAR -> CommandLine(ClearCommand(this)).execute()
                else -> {
                    println("Invalid CLI command: $commandName")
                }
            }

            return
        }

        val client = this.client!!
        val fullCommand = "$currentChamberName $command"

        try {
            client.executeCommand(fullCommand)
        } catch (ex: SocketException) {
            println("Failed to connected to the server. Reconnecting...")
            connect(Default.RECONNECT_ATTEMPTS)
            executeCommand(command)
        }
    }

    fun useChamber(chamberName: String) {
        if (!checkChamberExist(chamberName)) {
            println("Chamber does not exist: $chamberName")
            listAllAvailableChambers()
        } else {
            currentChamberName = chamberName
        }
    }

    private fun listAllAvailableChambers() {
        val tempChamberName = this.currentChamberName
        this.currentChamberName = "."
        client?.executeCommand(". chamber.list --all")
        currentChamberName = tempChamberName
    }

    private fun checkChamberExist(chamberName: String): Boolean {
        try {
            val client = client!!
            val exitCode = client.executeCommand(
                ". chamber.exist $chamberName --silent --blueprint"
            )
            return exitCode == ExitCode.OK
        } catch (ex: Exception) {
            return false
        }
    }

    fun exit() {
        try {
            client?.close()
            terminal?.close()
        } catch (ignored: Throwable) {
        } finally {
            println("See you | 再见 | 得閒飲茶 | またね")
            exitProcess(ExitCode.OK)
        }
    }

    object CliCommand {
        const val COMMANDS = "commands"
        const val EXIT = "exit"
        const val USE = "use"
        const val CLEAR = "clear"
    }

    object Default {
        const val INITIALIZATION_CONNECT_ATTEMPTS = 3
        const val RECONNECT_ATTEMPTS = 3
    }
}