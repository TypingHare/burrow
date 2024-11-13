package burrow.furnishing.scheduler

import burrow.kernel.Burrow
import burrow.kernel.chamber.Chamber
import burrow.kernel.chamber.ChamberPostBuildEvent
import burrow.kernel.chamber.ChamberPreBuildEvent
import burrow.kernel.config.Config
import burrow.kernel.furnishing.Furnishing
import burrow.kernel.furnishing.Furniture
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.Instant
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

@Furniture(
    description = "",
    type = Furniture.Type.ROOT
)
class Scheduler(chamber: Chamber) : Furnishing(chamber) {
    companion object {
        private val logger = LoggerFactory.getLogger(Scheduler::class.java)
    }

    private val taskScheduler: ScheduledExecutorService =
        Executors.newScheduledThreadPool(1);
    private val preBuildInstantMap = mutableMapOf<String, Instant>()
    private val postBuildInstantMap = mutableMapOf<String, Instant>()

    override fun prepareConfig(config: Config) {
        config.addKey(
            ConfigKey.INTERVAL_MS,
            { it.toLong() },
            { it.toString() }
        )
        config.addKey(
            ConfigKey.THRESHOLD_MS,
            { it.toLong() },
            { it.toString() }
        )
    }

    override fun modifyConfig(config: Config) {
        config.setIfAbsent(ConfigKey.INTERVAL_MS, Default.INTERVAL_MS)
        config.setIfAbsent(ConfigKey.THRESHOLD_MS, Default.THRESHOLD_MS)
    }

    override fun assemble() {
        val intervalMs = getIntervalMs()
        taskScheduler.scheduleAtFixedRate(
            this::callback,
            intervalMs,
            intervalMs,
            TimeUnit.MILLISECONDS
        )

        burrow.affairManager.subscribe(ChamberPreBuildEvent::class) {
            preBuildInstantMap[it.chamber.name] = Instant.now()
        }

        burrow.affairManager.subscribe(ChamberPostBuildEvent::class) { event ->
            val chamberName = event.chamber.name
            if (chamberName == Burrow.Standard.ROOT_CHAMBER_NAME) {
                return@subscribe
            }

            val now = Instant.now()
            postBuildInstantMap[chamberName] = now

            val startInstant = preBuildInstantMap[chamberName]!!
            val duration = Duration.between(startInstant, now).toMillis()
            logger.info("Started chamber $chamberName in $duration ms")
        }
    }

    override fun discard() {
        taskScheduler.shutdownNow()
    }

    private fun callback() {
        val chamberNames = burrow.chamberShepherd.chambers.keys
        for (chamberName in chamberNames) {
            if (chamberName == Burrow.Standard.ROOT_CHAMBER_NAME) {
                continue
            }

            val startInstant = preBuildInstantMap[chamberName]!!
            val duration =
                Duration.between(startInstant, Instant.now()).toMillis()
            if (duration < getThresholdMs()) {
                return
            }

            burrow.chamberShepherd.destroyChamber(chamberName)
        }
    }

    private fun getIntervalMs() = config.get<Long>(ConfigKey.INTERVAL_MS)!!

    private fun getThresholdMs() = config.get<Long>(ConfigKey.THRESHOLD_MS)!!

    object Default {
        const val INTERVAL_MS = 3000L;
        const val THRESHOLD_MS = 600000L;
    }

    object ConfigKey {
        const val INTERVAL_MS = "scheduler.interval_ms"
        const val THRESHOLD_MS = "scheduler.threshold_ms"
    }
}