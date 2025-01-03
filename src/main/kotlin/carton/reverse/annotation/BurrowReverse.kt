package burrow.carton.reverse.annotation

annotation class BurrowReverse(
    val config: Array<ConfigItem> = [],
)

annotation class ConfigItem(
    val key: String,
    val defaultValue: String,
    val converterPairType: ConverterPairType = ConverterPairType.IDENTITY
)

enum class ConverterPairType {
    IDENTITY, INT, LONG, FLOAT, DOUBLE, BOOLEAN
}