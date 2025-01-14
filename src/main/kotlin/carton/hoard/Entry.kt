package burrow.carton.hoard

class Entry(val id: Int, private val storage: Storage) {
    val store = mutableMapOf<String, Any>()
    private val converterPairContainer = storage.converterPairContainer

    fun set(props: Map<String, String>) {
        props.forEach { (key, value) ->
            store[key] = converterPairContainer.toRight(key, value)
        }
        storage.courier.post(EntrySetPropertiesEvent(this, props))
        storage.hasUpdated.set(true)
    }

    fun update(props: Map<String, Any>) {
        store.putAll(props)
        storage.courier.post(EntryUpdateEvent(this, props.keys.toList()))
        storage.hasUpdated.set(true)
    }

    operator fun set(key: String, leftValue: String) =
        set(mapOf(key to leftValue))

    fun update(key: String, rightValue: Any) = update(mapOf(key to rightValue))

    fun setIfAbsent(key: String, leftValue: String) {
        if (store.containsKey(key)) return
        set(key, leftValue)
    }

    fun updateIfAbsent(key: String, rightValue: Any) {
        if (store.containsKey(key)) return
        update(mapOf(key to rightValue))
    }

    operator fun <R> get(key: String): R? = getRight(key)

    private fun <R> getRight(key: String): R? {
        @Suppress("UNCHECKED_CAST")
        return store[key] as R
    }

    fun getLeft(key: String): String? =
        when (val rightValue = store[key]) {
            null -> null
            else -> converterPairContainer.toLeft(key, rightValue)
        }

    fun unset(keys: List<String>) {
        keys.forEach { key ->
            if (key == Key.ID) {
                throw ProtectedKeyException(key)
            }
            store.remove(key)
        }
        storage.courier.post(EntryUnsetPropertiesEvent(this, keys))
        storage.hasUpdated.set(true)
    }

    fun unset(key: String) = unset(listOf(key))

    fun containsKey(key: String): Boolean = store.containsKey(key)

    fun toProperties(): Map<String, String> = store
        .mapValues { (key, _) -> getLeft(key) }
        .filterValues { it != null }
        .mapValues { it.value!! }
        .toMutableMap()
        .apply { put(Key.ID, id.toString()) }

    object Key {
        const val ID = "id"
    }
}

class ProtectedKeyException(key: String) :
    RuntimeException("Protected key cannot be set: $key")