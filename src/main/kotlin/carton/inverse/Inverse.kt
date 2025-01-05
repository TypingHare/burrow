package burrow.carton.inverse

import burrow.carton.inverse.annotation.CONFIG_ITEM_VALUE_DEFAULT
import burrow.carton.inverse.annotation.ConverterPairType
import burrow.carton.inverse.annotation.InverseRegisterCommands
import burrow.carton.inverse.annotation.InverseSetConfig
import burrow.common.converter.StringConverterPair
import burrow.common.converter.StringConverterPairs
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
    description = "Allows developers to create furnishings more easily.",
    type = Furniture.Type.COMPONENT
)
class Inverse(renovator: Renovator) : Furnishing(renovator) {
    init {
        courier.subscribe(FurnishingPostPrepareConfigEvent::class) {
            val furnishing = it.furnishing
            val inverseSetConfig =
                furnishing::class.java.getAnnotation(InverseSetConfig::class.java)
            if (inverseSetConfig != null) {
                for (configItem in inverseSetConfig.config) {
                    val converterPair = getStringConverterPair(configItem.type)
                    config.addKey(configItem.key, converterPair)
                }
            }
        }

        courier.subscribe(FurnishingPostModifyConfigEvent::class) {
            val furnishing = it.furnishing
            val inverseSetConfig =
                furnishing::class.java.getAnnotation(InverseSetConfig::class.java)
            if (inverseSetConfig != null) {
                for (configItem in inverseSetConfig.config) {
                    when (val value = configItem.value) {
                        CONFIG_ITEM_VALUE_DEFAULT -> config.setIfAbsent(
                            configItem.key,
                            configItem.defaultValue
                        )
                        else -> config[configItem.key] = value
                    }
                }
            }
        }

        courier.subscribe(FurnishingPostAssembleEvent::class) {
            val furnishing = it.furnishing
            val inverseRegisterCommands =
                furnishing::class.java.getAnnotation(InverseRegisterCommands::class.java)
            if (inverseRegisterCommands != null) {
                scanCommands(furnishing)
            }
        }
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
            ConverterPairType.IDENTITY -> StringConverterPairs.IDENTITY
            ConverterPairType.INT -> StringConverterPairs.INT
            ConverterPairType.LONG -> StringConverterPairs.LONG
            ConverterPairType.FLOAT -> StringConverterPairs.FLOAT
            ConverterPairType.DOUBLE -> StringConverterPairs.DOUBLE
            ConverterPairType.BOOLEAN -> StringConverterPairs.BOOLEAN
        }
    }
}