package burrow.carton.dictator

import burrow.kernel.Burrow
import burrow.kernel.chamber.*
import burrow.kernel.config.Config
import burrow.kernel.config.ConfigItemHandler
import burrow.kernel.furnishing.Furnishing
import burrow.kernel.furnishing.Furniture
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.Instant
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

@Furniture(
    version = "0.0.0",
    description = "Scheduler.",
    type = Furniture.Type.ROOT
)
class Scheduler(chamber: Chamber) : Furnishing(chamber) {
    companion object {
        private val logger = LoggerFactory.getLogger(Scheduler::class.java)
    }

    private val taskScheduler: ScheduledExecutorService =
        Executors.newScheduledThreadPool(1)
    private val preBuildInstantMap = mutableMapOf<String, Instant>()
    private val postBuildInstantMap = mutableMapOf<String, Instant>()
    private val preDestroyInstantMap = mutableMapOf<String, Instant>()
    private val postDestroyInstantMap = mutableMapOf<String, Instant>()

    init {
        preBuildInstantMap[Burrow.Standard.ROOT_CHAMBER_NAME] = Instant.now()
    }

    override fun prepareConfig(config: Config) {
        val configItemHandler = ConfigItemHandler(
            { it.toLong() },
            { it.toString() }
        )
        config.addKey(ConfigKey.INTERVAL_MS, configItemHandler)
        config.addKey(ConfigKey.THRESHOLD_MS, configItemHandler)
    }

    override fun modifyConfig(config: Config) {
        config.setIfAbsent(ConfigKey.INTERVAL_MS, Default.INTERVAL_MS)
        config.setIfAbsent(ConfigKey.THRESHOLD_MS, Default.THRESHOLD_MS)
    }

    @Suppress("DuplicatedCode")
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
            val now = Instant.now()
            postBuildInstantMap[chamberName] = now

            // Remove chamber name from destroy instant maps
            preDestroyInstantMap.remove(chamberName)
            postDestroyInstantMap.remove(chamberName)

            if (!preBuildInstantMap.containsKey(chamberName)) {
                return@subscribe
            }

            val startInstant = preBuildInstantMap[chamberName]
            val duration = Duration.between(startInstant, now).toMillis()
            val coloredChamberName =
                palette.color(chamberName, Burrow.Highlights.CHAMBER)
            logger.info("Started chamber $coloredChamberName in $duration ms")
        }

        burrow.affairManager.subscribe(ChamberPreDestroyEvent::class) {
            preDestroyInstantMap[it.chamber.name] = Instant.now()
        }

        burrow.affairManager.subscribe(ChamberPostDestroyEvent::class) {
            val chamberName = it.chamber.name
            val now = Instant.now()
            postDestroyInstantMap[chamberName] = now

            // Remove chamber name from build instant maps
            preBuildInstantMap.remove(chamberName)
            postBuildInstantMap.remove(chamberName)

            if (!preDestroyInstantMap.containsKey(chamberName)) {
                return@subscribe
            }

            val startInstant = preDestroyInstantMap[chamberName]
            val duration = Duration.between(startInstant, now).toMillis()
            val coloredChamberName =
                palette.color(chamberName, Burrow.Highlights.CHAMBER)
            logger.info("Destroyed chamber $coloredChamberName in $duration ms")
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
        const val INTERVAL_MS = 3000L
        const val THRESHOLD_MS = 600000L
    }

    object ConfigKey {
        const val INTERVAL_MS = "scheduler.interval_ms"
        const val THRESHOLD_MS = "scheduler.threshold_ms"
    }
}