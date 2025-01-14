package burrow.common.converter

open class ConverterPairContainer<L, R>(
    private val defaultConverterPair: ConverterPair<L, R>
) {
    val converterPairs = mutableMapOf<String, ConverterPair<L, R>>()

    fun add(key: String, converterPair: ConverterPair<L, R>) {
        converterPairs[key] = converterPair
    }

    @Suppress("UNCHECKED_CAST")
    fun <R> toRight(key: String, leftValue: L): R =
        getPair(key).left.toRight(leftValue) as R

    fun toLeft(key: String, rightValue: R?): L =
        getPair(key).right.toLeft(rightValue)

    private fun getPair(key: String): ConverterPair<L, R> =
        converterPairs.getOrDefault(key, defaultConverterPair)
}