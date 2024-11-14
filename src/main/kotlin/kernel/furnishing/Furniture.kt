package burrow.kernel.furnishing

annotation class Furniture(
    val version: String,
    val description: String,
    val type: String = Type.MAIN,
    val label: String = "",
) {
    object Type {
        const val ROOT = "ROOT"
        const val MAIN = "MAIN"
        const val COMPONENT = "COMPONENT"
    }
}
