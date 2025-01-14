package burrow.common.converter

open class ConverterPair<L, R>(
    val left: LeftConverter<L, R>,
    val right: RightConverter<L, R>
)

fun interface LeftConverter<L, R> {
    fun toRight(left: L): R?
}

fun interface RightConverter<L, R> {
    fun toLeft(right: R?): L
}