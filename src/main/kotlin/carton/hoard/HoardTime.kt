package burrow.carton.hoard

import burrow.common.converter.StringConverterPair
import burrow.kernel.Burrow
import burrow.kernel.config.Config
import burrow.kernel.furniture.Furnishing
import burrow.kernel.furniture.Renovator
import burrow.kernel.furniture.annotation.Dependency
import burrow.kernel.furniture.annotation.Furniture
import burrow.kernel.furniture.annotation.RequiredDependencies
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Furniture(
    version = Burrow.VERSION,
    description = "Adds creation time and update time for entries.",
    type = Furniture.Type.COMPONENT
)
@RequiredDependencies(Dependency(Hoard::class, Burrow.VERSION))
class HoardTime(renovator: Renovator) : Furnishing(renovator) {
    override fun prepareConfig(config: Config) {
        registerConfigKey(
            ConfigKey.CREATED_AT_ENABLED,
            StringConverterPair.BOOLEAN
        )
        registerConfigKey(
            ConfigKey.UPDATED_AT_ENABLED,
            StringConverterPair.BOOLEAN
        )
        registerConfigKey(ConfigKey.CREATED_AT_FORMAT)
        registerConfigKey(ConfigKey.UPDATED_AT_FORMAT)
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
        courier.subscribe(EntryRestoreEvent::class) { event ->
            val entry = event.entry
            val props = event.props

            if (props.containsKey(EntryKey.CREATED_AT)) {
                entry.update(
                    EntryKey.CREATED_AT,
                    props[EntryKey.CREATED_AT]!!.toLong()
                )
            }

            if (props.containsKey(EntryKey.UPDATED_AT)) {
                entry.update(
                    EntryKey.UPDATED_AT,
                    props[EntryKey.UPDATED_AT]!!.toLong()
                )
            }
        }

        courier.subscribe(EntryCreateEvent::class) {
            setCreateTime(it.entry)
            setUpdatedTime(it.entry)
        }

        courier.subscribe(EntrySetPropertiesEvent::class) {
            setUpdatedTime(it.entry)
        }

        courier.subscribe(EntryUnsetPropertiesEvent::class) {
            setUpdatedTime(it.entry)
        }
    }

    override fun launch() {
        val createdAtEnabled = createdAtEnabled()
        val updatedAtEnabled = updatedAtEnabled()
        val createdAtFormat = createdAtFormat()
        val updatedAtFormat = updatedAtFormat()

        val storage = use(Hoard::class).storage
        courier.subscribe(FormatEntryEvent::class) {
            if (createdAtEnabled) {
                val createdAt: Long? = it.entry[EntryKey.CREATED_AT]
                if (createdAt != null) {
                    it.props[EntryKey.CREATED_AT] =
                        timestampToString(createdAt, createdAtFormat)
                }
            }

            if (updatedAtEnabled) {
                val updatedAt: Long? = it.entry[EntryKey.UPDATED_AT]
                if (updatedAt != null) {
                    it.props[EntryKey.UPDATED_AT] =
                        timestampToString(updatedAt, updatedAtFormat)
                }
            }
        }

        if (createdAtEnabled) {
            storage.addMapping(EntryKey.CREATED_AT, StringConverterPair.LONG)
        }

        if (updatedAtEnabled) {
            storage.addMapping(EntryKey.UPDATED_AT, StringConverterPair.LONG)
        }
    }

    private fun createdAtEnabled(): Boolean =
        config.getNotNull(ConfigKey.CREATED_AT_ENABLED)

    private fun updatedAtEnabled(): Boolean =
        config.getNotNull(ConfigKey.UPDATED_AT_ENABLED)

    private fun createdAtFormat(): String =
        config.getNotNull(ConfigKey.CREATED_AT_FORMAT)

    private fun updatedAtFormat(): String =
        config.getNotNull(ConfigKey.UPDATED_AT_FORMAT)

    private fun setCreateTime(entry: Entry) {
        if (createdAtEnabled()) {
            entry.update(EntryKey.CREATED_AT, System.currentTimeMillis())
        }
    }

    private fun setUpdatedTime(entry: Entry) {
        if (updatedAtEnabled()) {
            entry.update(EntryKey.UPDATED_AT, System.currentTimeMillis())
        }
    }

    private fun timestampToString(timestampMs: Long, format: String): String {
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