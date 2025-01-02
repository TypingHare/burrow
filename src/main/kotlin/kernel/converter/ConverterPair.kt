package burrow.kernel.converter

data class ConverterPair<L, R>(
    val leftConverter: LeftConverter<L, R>,
    val rightConverter: RightConverter<L, R>
)

fun interface LeftConverter<L, R> {
    fun toRight(left: L): R?
}

fun interface RightConverter<L, R> {
    fun toLeft(right: R?): L
}