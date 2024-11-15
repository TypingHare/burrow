package burrow.furnishing.hoard

class Entry(val id: Int, val properties: MutableMap<String, String>) {
    private val entries = mutableMapOf<String, Any>()

    init {
        properties[Key.ID] = id.toString()
    }

    fun set(key: String, value: Any) {
        entries[key] = value
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: String): T? = entries[key] as T?

    fun setProperty(key: String, value: String) {
        properties[key] = value
    }

    fun getProperty(key: String): String? = properties[key]

    fun del(key: String) {
        properties.remove(key)
        entries.remove(key)
    }

    object Key {
        const val ID = "id"
    }
}