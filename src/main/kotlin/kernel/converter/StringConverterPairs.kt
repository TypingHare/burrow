package burrow.kernel.converter

object StringConverterPairs {
    val IDENTITY = StringConverterPair({ it }, { it ?: "" })
    val INT = StringConverterPair({ it.toInt() }, { it?.toString() ?: "0" })
    val LONG = StringConverterPair({ it.toLong() }, { it?.toString() ?: "0" })
    val FLOAT =
        StringConverterPair({ it.toFloat() }, { it?.toString() ?: "0.0" })
    val DOUBLE =
        StringConverterPair({ it.toDouble() }, { it?.toString() ?: "0.0" })
    val BOOLEAN =
        StringConverterPair({ it.toBoolean() }, { it?.toString() ?: "false" })
}