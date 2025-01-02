package burrow.carton.hoard

class Entry(
    val id: Int,
    private val converterPairsContainer: StringConverterPairContainer
) {
    val store = mutableMapOf<String, Any>()

    operator fun set(key: String, leftValue: String) {
        store[key] = converterPairsContainer.toRight(key, leftValue)
    }

    fun setIfAbsent(key: String, leftValue: String) {
        if (!store.containsKey(key)) {
            set(key, leftValue)
        }
    }

    operator fun <R> get(key: String): R? {
        @Suppress("UNCHECKED_CAST")
        return store[key] as R
    }

    fun unset(key: String) {
        if (key == Key.ID) {
            throw ProtectedKeyException(key)
        }

        store.remove(key)
    }

    fun containsKey(key: String): Boolean = store.containsKey(key)

    fun <R> getLeft(key: String): String =
        converterPairsContainer.toLeft(key, get<R>(key))

    fun toProperties(): Map<String, String> = store
        .mapValues { (key, value) ->
            converterPairsContainer.toLeft(key, value)
        }
        .toMutableMap()
        .apply { put(Key.ID, id.toString()) }

    object Key {
        const val ID = "id"
    }
}

class ProtectedKeyException(key: String) :
    RuntimeException("Protected key cannot be set: $key")