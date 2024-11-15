package burrow.carton.standard

import burrow.kernel.chamber.Chamber
import burrow.kernel.config.Config
import burrow.kernel.furnishing.Furnishing
import burrow.kernel.furnishing.Furniture

@Furniture(
    version = "0.0.0",
    description = "Standard Furnishing of all chambers.",
    type = Furniture.Type.COMPONENT
)
class Standard(chamber: Chamber) : Furnishing(chamber) {
    override fun assemble() {
        // Basic commands
        registerCommand(RootCommand::class)
        registerCommand(HelpCommand::class)

        // Furnishing commands
        registerCommand(FurnishingsCommand::class)

        // Command commands
        registerCommand(CommandsCommand::class)
    }

    override fun prepareConfig(config: Config) {
        config.addKey(ConfigKey.ALIAS)
        config.addKey(ConfigKey.DESCRIPTION)
    }

    override fun modifyConfig(config: Config) {
        config.setIfAbsent(ConfigKey.ALIAS, chamber.name)
        config.setIfAbsent(ConfigKey.DESCRIPTION, "")
    }

    object ConfigKey {
        const val ALIAS = "alias"
        const val DESCRIPTION = "description"
    }
}