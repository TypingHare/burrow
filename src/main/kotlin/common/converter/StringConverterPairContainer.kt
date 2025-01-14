package burrow.common.converter

open class StringConverterPairContainer<R>(
    defaultConverterPair: StringConverterPair<R>
) : ConverterPairContainer<String, R>(defaultConverterPair)

class AnyStringConverterPairContainer(
    @Suppress("UNCHECKED_CAST")
    defaultConverterPair: StringConverterPair<Any> = StringConverterPair.IDENTITY as StringConverterPair<Any>
) : StringConverterPairContainer<Any>(defaultConverterPair)