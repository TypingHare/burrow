package burrow.carton.hoard

import burrow.kernel.Burrow
import burrow.kernel.chamber.Chamber
import burrow.kernel.config.Config
import burrow.kernel.furnishing.Furnishing
import burrow.kernel.furnishing.annotation.Furniture
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


@Furniture(
    version = Burrow.VERSION.NAME,
    description = "Creation time.",
    type = Furniture.Type.COMPONENT
)
class HoardTime(chamber: Chamber) : Furnishing(chamber) {
    private var createdAtEnabled = Default.CREATED_AT_ENABLED
    private var updatedAtEnabled = Default.UPDATED_AT_ENABLED
    private var createdAtFormat = Default.CREATED_AT_FORMAT
    private var updatedAtFormat = Default.UPDATED_AT_FORMAT

    override fun prepareConfig(config: Config) {
        config.addKey(ConfigKey.CREATED_AT_ENABLED, Config.Handler.BOOLEAN)
        config.addKey(ConfigKey.UPDATED_AT_ENABLED, Config.Handler.BOOLEAN)
        config.addKey(ConfigKey.CREATED_AT_FORMAT)
        config.addKey(ConfigKey.UPDATED_AT_FORMAT)
    }

    override fun modifyConfig(config: Config) {
        config.setIfAbsent(
            ConfigKey.CREATED_AT_ENABLED,
            Default.CREATED_AT_ENABLED
        )
        config.setIfAbsent(
            ConfigKey.UPDATED_AT_ENABLED,
            Default.UPDATED_AT_ENABLED
        )
        config.setIfAbsent(
            ConfigKey.CREATED_AT_FORMAT,
            Default.CREATED_AT_FORMAT
        )
        config.setIfAbsent(
            ConfigKey.UPDATED_AT_FORMAT,
            Default.UPDATED_AT_FORMAT
        )
    }

    override fun assemble() {
        affairManager.subscribe(EntryRestoreEvent::class) {
            it.entry.let { entry ->
                setCreateTime(entry)
                setUpdatedTime(entry)
            }
        }

        affairManager.subscribe(EntryCreateEvent::class) {
            it.entry.let { entry ->
                setCreateTime(entry)
                setUpdatedTime(entry)
            }
        }

        affairManager.subscribe(EntrySetPropertiesEvent::class) {
            setUpdatedTime(it.entry)
        }

        affairManager.subscribe(EntryUnsetPropertiesEvent::class) {
            setUpdatedTime(it.entry)
        }

        affairManager.subscribe(EntryStringifyEvent::class) {
            if (createdAtEnabled) {
                it.props[EntryKey.CREATED_AT] =
                    dateToString(
                        it.entry[EntryKey.CREATED_AT]!!,
                        createdAtFormat
                    )
            }

            if (updatedAtEnabled) {
                it.props[EntryKey.UPDATED_AT] =
                    dateToString(
                        it.entry[EntryKey.UPDATED_AT]!!,
                        updatedAtFormat
                    )
            }
        }
    }

    override fun launch() {
        createdAtEnabled = config.get<Boolean>(ConfigKey.CREATED_AT_ENABLED)!!
        updatedAtEnabled = config.get<Boolean>(ConfigKey.UPDATED_AT_ENABLED)!!
        createdAtFormat = config.get<String>(ConfigKey.CREATED_AT_FORMAT)!!
        updatedAtFormat = config.get<String>(ConfigKey.UPDATED_AT_FORMAT)!!
    }

    private fun setCreateTime(entry: Entry) {
        if (createdAtEnabled) {
            val timestampMs = System.currentTimeMillis()
            entry.setProp(EntryKey.CREATED_AT, timestampMs.toString())
            entry.set(EntryKey.CREATED_AT, timestampMs)
        }
    }

    private fun setUpdatedTime(entry: Entry) {
        if (updatedAtEnabled) {
            val timestampMs = System.currentTimeMillis()
            entry.setProp(EntryKey.UPDATED_AT, timestampMs.toString())
            entry.set(EntryKey.UPDATED_AT, timestampMs)
        }
    }

    private fun dateToString(timestampMs: Long, format: String): String {
        return LocalDateTime.ofInstant(
            Instant.ofEpochMilli(timestampMs),
            ZoneId.systemDefault()
        ).format(DateTimeFormatter.ofPattern(format))
    }

    object ConfigKey {
        const val CREATED_AT_ENABLED: String = "time.created_at.enabled"
        const val UPDATED_AT_ENABLED: String = "time.updated_at.enabled"
        const val CREATED_AT_FORMAT: String = "time.created_at.format"
        const val UPDATED_AT_FORMAT: String = "time.updated_at.format"
    }

    object Default {
        const val CREATED_AT_ENABLED = true
        const val UPDATED_AT_ENABLED = true
        const val CREATED_AT_FORMAT = "MMM dd, yyyy"
        const val UPDATED_AT_FORMAT = "MMM dd, yyyy"
    }

    object EntryKey {
        const val CREATED_AT: String = "created_at"
        const val UPDATED_AT: String = "updated_at"
    }
}