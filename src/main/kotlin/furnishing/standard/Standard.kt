package burrow.furnishing.standard

import burrow.kernel.chamber.Chamber
import burrow.kernel.chamber.Chamber.ConfigKey
import burrow.kernel.config.Config
import burrow.kernel.furnishing.Furnishing
import burrow.kernel.furnishing.Furniture

@Furniture(
    description = "Standard Furnishing of all chambers.",
    type = Furniture.Type.COMPONENT
)
class Standard(chamber: Chamber) : Furnishing(chamber) {
    override fun prepareConfig(config: Config) {
        config.addKey(ConfigKey.ALIAS)
        config.addKey(ConfigKey.DESCRIPTION)
        renovator.prepareConfig(config)
    }

    override fun modifyConfig(config: Config) {
        config.setIfAbsent(ConfigKey.ALIAS, chamber.name)
        config.setIfAbsent(ConfigKey.DESCRIPTION, "")
        renovator.prepareConfig(config)
    }
}