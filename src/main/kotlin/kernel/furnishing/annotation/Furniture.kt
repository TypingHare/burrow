package burrow.kernel.furnishing.annotation

/**
 * A piece of furniture has a version, a description, and a type.
 */
annotation class Furniture(
    val version: String,
    val description: String,
    val type: String = Type.MAIN,
) {
    object Type {
        // Only the root chamber can install
        const val ROOT = "ROOT"

        // A chamber should have only one main furnishing. However, if two main
        // furnishings do not cause conflicts, then they can be installed
        // together
        const val MAIN = "MAIN"

        // A chamber can has multiple component furnishings
        const val COMPONENT = "COMPONENT"
    }
}
