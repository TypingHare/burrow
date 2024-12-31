package burrow.kernel.config

data class ConfigItemHandler<T>(
    val reader: ConfigItemReader<T>,
    val writer: ConfigItemWriter<T>,
)

fun interface ConfigItemReader<T> {
    fun read(value: String): T?
}

fun interface ConfigItemWriter<T> {
    fun write(item: T?): String
}
