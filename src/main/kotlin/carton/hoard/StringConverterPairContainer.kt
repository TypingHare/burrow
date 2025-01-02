package burrow.carton.hoard

import burrow.kernel.converter.StringConverterPair
import burrow.kernel.converter.StringConverterPairs

class StringConverterPairContainer(
    private val defaultConverterPair: StringConverterPair<String>
    = StringConverterPairs.IDENTITY,
) {
    private val converterPairs = mutableMapOf<String, StringConverterPair<*>>()

    fun add(key: String, converterPair: StringConverterPair<*>) {
        converterPairs[key] = converterPair
    }

    @Suppress("UNCHECKED_CAST")
    fun <R> toRight(key: String, leftValue: String): R {
        val converter = converterPairs.getOrDefault(key, defaultConverterPair)

        return converter.leftConverter.toRight(leftValue) as R
    }

    @Suppress("UNCHECKED_CAST")
    fun <R> toLeft(key: String, rightValue: R?): String {
        val converter = converterPairs.getOrDefault(
            key,
            defaultConverterPair
        ) as StringConverterPair<R>

        return converter.rightConverter.toLeft(rightValue)
    }
}