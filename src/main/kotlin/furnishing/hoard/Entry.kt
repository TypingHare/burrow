package burrow.furnishing.hoard

class Entry(val id: Int, val properties: MutableMap<String, String>) {
    private val entries = mutableMapOf<String, Any>()

    fun set(key: String, value: Any) {
        entries[key] = value
    }

    fun get(key: String): Any? = entries[key]

    fun setProperty(key: String, value: String) {
        properties[key] = value
    }

    fun getProperty(key: String): String? = properties[key]

    fun del(key: String) {
        properties.remove(key)
        entries.remove(key)
    }
}