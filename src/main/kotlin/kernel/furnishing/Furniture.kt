package burrow.kernel.furnishing

annotation class Furniture(
    val label: String = "",
    val description: String,
    val type: String = Type.MAIN
) {
    object Type {
        const val ROOT = "ROOT"
        const val MAIN = "MAIN"
        const val COMPONENT = "COMPONENT"
    }
}
