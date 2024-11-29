package burrow.carton.hoard

class Entry(val id: Int, val props: MutableMap<String, String>) {
    val store = mutableMapOf<String, Any>()

    init {
        props[Key.ID] = id.toString()
    }

    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(key: String): T? = store[key] as T?

    operator fun set(key: String, value: Any) {
        store[key] = value
    }

    fun setIfAbsent(key: String, value: Any) {
        if (!props.containsKey(key)) {
            set(key, value)
        }
    }

    @Throws(ProtectedKeyException::class)
    fun setProp(key: String, value: String) {
        if (key == Key.ID) {
            throw ProtectedKeyException(key)
        }

        props[key] = value
    }

    fun setPropIfAbsent(key: String, value: String) {
        if (!props.containsKey(key)) {
            setProp(key, value)
        }
    }

    fun getProp(key: String): String? = props[key]

    fun unset(key: String) {
        props.remove(key)
        store.remove(key)
    }

    object Key {
        const val ID = "\$ID\$"
    }
}

class ProtectedKeyException(key: String) :
    RuntimeException("Protected cannot be set: $key")