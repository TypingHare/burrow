package burrow.carton.server

import burrow.kernel.Burrow
import burrow.kernel.chamber.*
import burrow.kernel.config.Config
import burrow.kernel.furniture.Furnishing
import burrow.kernel.furniture.Renovator
import burrow.kernel.furniture.annotation.Dependency
import burrow.kernel.furniture.annotation.Furniture
import burrow.kernel.furniture.annotation.RequiredDependencies
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.Instant
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Furniture(
    version = Burrow.VERSION,
    description = "Schedules chambers.",
    type = Furniture.Type.ROOT
)
@RequiredDependencies(Dependency(Server::class, Burrow.VERSION))
class Scheduler(renovator: Renovator) : Furnishing(renovator) {
    private val taskScheduler = Executors.newScheduledThreadPool(1)
    private val preBuildInstantMap = mutableMapOf<String, Instant>()
    private val postBuildInstantMap = mutableMapOf<String, Instant>()
    private val preDestroyInstantMap = mutableMapOf<String, Instant>()
    private val postDestroyInstantMap = mutableMapOf<String, Instant>()

    init {
        preBuildInstantMap[ChamberShepherd.ROOT_CHAMBER_NAME] = Instant.now()
    }

    override fun prepareConfig(config: Config) {
        config.addKey(ConfigKey.INTERVAL_MS, Config.Handler.LONG)
        config.addKey(ConfigKey.THRESHOLD_MS, Config.Handler.LONG)
    }

    override fun modifyConfig(config: Config) {
        config.setIfAbsent(ConfigKey.INTERVAL_MS, Default.INTERVAL_MS)
        config.setIfAbsent(ConfigKey.THRESHOLD_MS, Default.THRESHOLD_MS)
    }

    object Default {
        const val INTERVAL_MS = 3000L
        const val THRESHOLD_MS = 600000L
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

        courier.subscribe(ChamberPreBuildEvent::class) {
            preBuildInstantMap[it.chamber.name] = Instant.now()
        }

        courier.subscribe(ChamberPostBuildEvent::class) { event ->
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
            logger.info("Started chamber $chamberName in $duration ms")
        }

        courier.subscribe(ChamberPreDestroyEvent::class) {
            preDestroyInstantMap[it.chamber.name] = Instant.now()
        }

        courier.subscribe(ChamberPostDestroyEvent::class) {
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
            logger.info("Destroyed chamber $chamberName in $duration ms")
        }
    }

    override fun discard() {
        taskScheduler.shutdownNow()
    }

    private fun callback() {
        val chamberNames = chamberShepherd.chambers.keys
        for (chamberName in chamberNames) {
            if (chamberName == ChamberShepherd.ROOT_CHAMBER_NAME) {
                continue
            }

            val startInstant = preBuildInstantMap[chamberName]!!
            val duration =
                Duration.between(startInstant, Instant.now()).toMillis()
            if (duration < getThresholdMs()) {
                return
            }

            chamberShepherd.destroyChamber(chamberName)
        }
    }

    private fun getIntervalMs() = config.getNotNull<Long>(ConfigKey.INTERVAL_MS)

    private fun getThresholdMs() =
        config.getNotNull<Long>(ConfigKey.THRESHOLD_MS)

    companion object {
        private val logger = LoggerFactory.getLogger(Scheduler::class.java)
    }

    object ConfigKey {
        const val INTERVAL_MS = "scheduler.interval_ms"
        const val THRESHOLD_MS = "scheduler.threshold_ms"
    }
}