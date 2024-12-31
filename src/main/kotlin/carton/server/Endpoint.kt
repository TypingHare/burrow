package burrow.carton.server

data class Endpoint(val host: String, val port: Int) {
    override fun toString(): String = "$host:$port"
}