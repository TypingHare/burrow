package burrow.carton.hoard

class Entry(val id: Int, val props: MutableMap<String, String>) {
    val store = mutableMapOf<String, Any>()

    init {
        props[Key.ID] = id.toString()
    }

    fun set(key: String, value: Any) {
        store[key] = value
    }

    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(key: String): T? = store[key] as T?

    @Throws(ProtectedKeyException::class)
    fun setProp(key: String, value: String) {
        if (key == Key.ID) {
            throw ProtectedKeyException(key)
        }

        props[key] = value
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