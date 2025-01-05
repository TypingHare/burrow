package burrow.carton.inverse.annotation

annotation class InverseSetConfig(
    vararg val config: ConfigItem = [],
)

annotation class ConfigItem(
    val key: String,
    val defaultValue: String = CONFIG_ITEM_VALUE_DEFAULT,
    val value: String = CONFIG_ITEM_VALUE_DEFAULT,
    val type: ConverterPairType = ConverterPairType.IDENTITY
)

const val CONFIG_ITEM_VALUE_DEFAULT = "__default_value__"

enum class ConverterPairType {
    IDENTITY, INT, LONG, FLOAT, DOUBLE, BOOLEAN
}