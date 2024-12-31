package burrow.kernel.config

interface ConfigLifeCycle {
    /**
     * Called before configs are imported from the file.
     */
    fun prepareConfig(config: Config) = Unit

    /**
     * Called after configs are imported from the file.
     */
    fun modifyConfig(config: Config) = Unit
}