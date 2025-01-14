package burrow.common.converter

open class StringConverterPair<R>(
    left: LeftConverter<String, R>,
    right: RightConverter<String, R>
) : ConverterPair<String, R>(left, right) {
    companion object {
        val IDENTITY = StringConverterPair({ it }, { it ?: "" })
        val INT = StringConverterPair({ it.toInt() }, { it?.toString() ?: "0" })
        val LONG =
            StringConverterPair({ it.toLong() }, { it?.toString() ?: "0" })
        val FLOAT =
            StringConverterPair({ it.toFloat() }, { it?.toString() ?: "0.0" })
        val DOUBLE =
            StringConverterPair({ it.toDouble() }, { it?.toString() ?: "0.0" })
        val BOOLEAN = StringConverterPair(
            {
                when (it.lowercase()) {
                    "true" -> true
                    else -> false
                }
            },
            {
                when (it) {
                    true -> "true"
                    else -> "false"
                }
            }
        )
    }
}