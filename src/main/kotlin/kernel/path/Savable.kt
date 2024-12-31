package burrow.kernel.path

interface Savable : PathBound {
    /**
     * Saves data to the path associated with the object.
     */
    fun save()
}
