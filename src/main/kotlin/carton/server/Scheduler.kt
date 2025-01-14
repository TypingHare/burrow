package burrow.carton.server

import burrow.carton.server.command.scheduler.SchedulerListCommand
import burrow.common.converter.StringConverterPair
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
    val postBuildInstantMap = mutableMapOf<String, Instant>()
    private val preDestroyInstantMap = mutableMapOf<String, Instant>()
    private val postDestroyInstantMap = mutableMapOf<String, Instant>()

    init {
        preBuildInstantMap[ChamberShepherd.ROOT_CHAMBER_NAME] = Instant.now()
    }

    override fun prepareConfig(config: Config) {
        registerConfigKey(ConfigKey.INTERVAL_MS, StringConverterPair.LONG)
        registerConfigKey(ConfigKey.THRESHOLD_MS, StringConverterPair.LONG)
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
        registerCommand(SchedulerListCommand::class)

        val intervalMs = getIntervalMs()
        taskScheduler.scheduleAtFixedRate(
            this::destroyTimeoutChambers,
            intervalMs,
            intervalMs,
            TimeUnit.MILLISECONDS
        )

        burrow.courier.subscribe(ChamberPreBuildEvent::class) {
            val chamberName = it.chamber.name
            preDestroyInstantMap.remove(chamberName)
            postDestroyInstantMap.remove(chamberName)

            preBuildInstantMap[it.chamber.name] = Instant.now()
        }

        burrow.courier.subscribe(ChamberPostBuildEvent::class) { event ->
            val chamberName = event.chamber.name
            val now = Instant.now()
            postBuildInstantMap[chamberName] = now

            if (!preBuildInstantMap.containsKey(chamberName)) {
                return@subscribe
            }

            val startInstant = preBuildInstantMap[chamberName]
            val duration = Duration.between(startInstant, now).toMillis()
            logger.info("Built chamber $chamberName in $duration ms")
        }

        burrow.courier.subscribe(ChamberPreDestroyEvent::class) {
            val chamberName = it.chamber.name
            preBuildInstantMap.remove(chamberName)
            postBuildInstantMap.remove(chamberName)

            preDestroyInstantMap[it.chamber.name] = Instant.now()
        }

        burrow.courier.subscribe(ChamberPostDestroyEvent::class) {
            val chamberName = it.chamber.name
            val now = Instant.now()
            postDestroyInstantMap[chamberName] = now

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

    private fun destroyTimeoutChambers() {
        val chamberNames = chamberShepherd.chambers.keys
        val thresholdMs = getThresholdMs()
        for (chamberName in chamberNames.toList()) {
            if (chamberName == ChamberShepherd.ROOT_CHAMBER_NAME) {
                continue
            }

            val startInstant = preBuildInstantMap[chamberName]!!
            val durationMs =
                Duration.between(startInstant, Instant.now()).toMillis()
            if (durationMs < thresholdMs) {
                return
            }

            chamberShepherd.destroyChamber(chamberName)
        }
    }

    private fun getIntervalMs() = config.getNotNull<Long>(ConfigKey.INTERVAL_MS)

    fun getThresholdMs() =
        config.getNotNull<Long>(ConfigKey.THRESHOLD_MS)

    companion object {
        private val logger = LoggerFactory.getLogger(Scheduler::class.java)
    }

    object ConfigKey {
        const val INTERVAL_MS = "scheduler.interval_ms"
        const val THRESHOLD_MS = "scheduler.threshold_ms"
    }
}