package burrow.carton.hoard

class Entry(val id: Int, val properties: MutableMap<String, String>) {
    val store = mutableMapOf<String, Any>()

    init {
        properties[Key.ID] = id.toString()
    }

    fun set(key: String, value: Any) {
        store[key] = value
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: String): T? = store[key] as T?

    fun setProperty(key: String, value: String) {
        properties[key] = value
    }

    fun getProperty(key: String): String? = properties[key]

    fun unset(key: String) {
        properties.remove(key)
        store.remove(key)
    }

    object Key {
        const val ID = "id"
    }
}