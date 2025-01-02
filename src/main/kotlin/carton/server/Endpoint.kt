package burrow.carton.server

data class Endpoint(val host: String, val port: Int) {
    override fun toString(): String = "$host:$port"

    companion object {
        @JvmStatic
        fun fromString(string: String): Endpoint {
            val (host, portString) = string.split(":")
            return Endpoint(host, portString.toInt())
        }
    }
}