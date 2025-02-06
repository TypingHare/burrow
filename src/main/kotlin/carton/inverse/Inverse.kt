package burrow.carton.inverse

import burrow.carton.inverse.annotation.*
import burrow.common.converter.StringConverterPair
import burrow.kernel.Burrow
import burrow.kernel.furniture.*
import burrow.kernel.furniture.annotation.Furniture
import burrow.kernel.terminal.Command
import org.reflections.Reflections
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import org.reflections.util.FilterBuilder
import java.net.URL

@Furniture(
    version = Burrow.VERSION,
    description = "Allow developers to create furnishings more easily.",
    type = Furniture.Type.COMPONENT
)
class Inverse(renovator: Renovator) : Furnishing(renovator) {
    init {
        courier.subscribe(FurnishingPostPrepareConfigEvent::class) { event ->
            val inverseSetConfig =
                getInverseSetConfig(event.furnishing) ?: return@subscribe
            inverseSetConfig.config.forEach { configItem ->
                getStringConverterPair(configItem.type).let {
                    @Suppress("UNCHECKED_CAST") val converterPair =
                        getStringConverterPair(configItem.type) as StringConverterPair<Any>
                    event.furnishing.registerConfigKey(
                        configItem.key,
                        converterPair
                    )
                }
            }
        }

        courier.subscribe(FurnishingPostModifyConfigEvent::class) { event ->
            val inverseSetConfig =
                getInverseSetConfig(event.furnishing) ?: return@subscribe
            inverseSetConfig.config.forEach { registerConfigItem(it) }
        }

        courier.subscribe(FurnishingPostAssembleEvent::class) { event ->
            getInverseRegisterCommands(event.furnishing) ?: return@subscribe
            scanCommands(event.furnishing)
        }
    }

    private fun getInverseRegisterCommands(furnishing: Furnishing): InverseRegisterCommands? {
        return furnishing::class.java.getAnnotation(InverseRegisterCommands::class.java)
    }

    private fun getInverseSetConfig(furnishing: Furnishing): InverseSetConfig? {
        return furnishing::class.java.getAnnotation(InverseSetConfig::class.java)
    }

    private fun registerConfigItem(configItem: ConfigItem) {
        val key = configItem.key
        when (val value = configItem.value) {
            CONFIG_ITEM_VALUE_DEFAULT -> setConfigItemWithDefaultValue(
                key,
                configItem.defaultValue
            )
            else -> setConfigItem(key, value)
        }
    }

    private fun setConfigItem(key: String, value: String) {
        config[key] = config.converterPairContainer.toRight(key, value)
    }

    private fun setConfigItemWithDefaultValue(
        key: String,
        defaultValue: String
    ) {
        if (defaultValue == CONFIG_ITEM_VALUE_DEFAULT) {
            throw ConfigItemNoValueException(key)
        }

        config.setIfAbsent(
            key,
            config.converterPairContainer.toRight(key, defaultValue)
        )
    }

    private fun scanCommands(furnishing: Furnishing) {
        val furnishingId = furnishing.javaClass.name
        val commandPackage = "${furnishing.javaClass.packageName}.command"
        val classLoader =
            warehouse.furnishingIdToCarton[furnishingId]!!.classLoader
        val filterBuilder = FilterBuilder().apply {
            includePackage(commandPackage)
        }
        val urlCollection: List<URL> =
            ClasspathHelper.forPackage(commandPackage, classLoader).toList()
        val configuration = ConfigurationBuilder()
            .filterInputsBy(filterBuilder)
            .setUrls(urlCollection)
            .addClassLoaders(classLoader)
        val commandClasses = Reflections(configuration)
            .getSubTypesOf(Command::class.java)

        for (commandClass in commandClasses) {
            furnishing.registerCommand(commandClass.kotlin)
        }
    }

    private fun getStringConverterPair(
        converterPairType: ConverterPairType
    ): StringConverterPair<*> {
        return when (converterPairType) {
            ConverterPairType.IDENTITY -> StringConverterPair.IDENTITY
            ConverterPairType.INT -> StringConverterPair.INT
            ConverterPairType.LONG -> StringConverterPair.LONG
            ConverterPairType.FLOAT -> StringConverterPair.FLOAT
            ConverterPairType.DOUBLE -> StringConverterPair.DOUBLE
            ConverterPairType.BOOLEAN -> StringConverterPair.BOOLEAN
        }
    }
}

class ConfigItemNoValueException(key: String) :
    RuntimeException("Both default value and value are no specified: $key")