package burrow.kernel.path

interface Loadable : PathBound {
    /**
     * Loads data from the path associated with the object.
     */
    fun load()
}