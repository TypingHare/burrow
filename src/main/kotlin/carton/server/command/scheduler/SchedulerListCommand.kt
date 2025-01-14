package burrow.carton.server.command.scheduler

import burrow.carton.server.Scheduler
import burrow.kernel.chamber.ChamberShepherd
import burrow.kernel.stream.TablePrinter
import burrow.kernel.stream.TablePrinterContext
import burrow.kernel.terminal.BurrowCommand
import burrow.kernel.terminal.Command
import burrow.kernel.terminal.CommandData
import java.time.Duration
import java.time.Instant
import kotlin.math.max

@BurrowCommand(
    name = "scheduler.list",
    header = ["List all built chambers and elapsed time."]
)
class SchedulerListCommand(data: CommandData) : Command(data) {
    override fun call(): Int {
        val scheduler = use(Scheduler::class)
        val table = mutableListOf<List<String>>().apply {
            add(
                listOf(
                    "Chamber",
                    "Elapsed Time (seconds)",
                    "Duration to destroy"
                )
            )
        }

        val thresholdSeconds = scheduler.getThresholdMs() / 1000
        val now = Instant.now()
        scheduler.postBuildInstantMap.forEach { (chamberName, instant) ->
            val durationSeconds = Duration.between(instant, now).toSeconds()
            val durationToDestroySeconds = thresholdSeconds - durationSeconds
            val durationToDestroy = when (chamberName) {
                ChamberShepherd.ROOT_CHAMBER_NAME -> "*"
                else -> max(durationToDestroySeconds, 0L).toString()
            }

            table.add(
                listOf(
                    chamberName,
                    durationSeconds.toString(),
                    durationToDestroy
                )
            )
        }

        val context = TablePrinterContext(table, getTerminalWidth())
        TablePrinter(stdout, context).print()

        return 0
    }
}