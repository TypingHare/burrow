package burrow.kernel.furniture

interface FurnishingLifeCycle {
    /**
     * Assembles the furnishing.
     */
    fun assemble() = Unit

    /**
     * Launches the furnishing.
     */
    fun launch() = Unit
}