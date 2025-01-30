package burrow.carton.core

import burrow.carton.core.command.NotFoundCommand
import burrow.kernel.Burrow
import burrow.kernel.config.Config
import burrow.kernel.furniture.Furnishing
import burrow.kernel.furniture.Renovator
import burrow.kernel.furniture.annotation.Furniture

@Furniture(
    version = Burrow.VERSION,
    description = "Dispatches ",
    type = Furniture.Type.COMPONENT
)
class NotFoundDispatcher(renovator: Renovator) : Furnishing(renovator) {
    override fun prepareConfig(config: Config) {
        registerConfigKey(ConfigKey.NOT_FOUND_DISPATCHER_COMMAND)
    }

    override fun modifyConfig(config: Config) {
        config.setIfAbsent(ConfigKey.NOT_FOUND_DISPATCHER_COMMAND, "")
    }

    override fun assemble() {
        registerCommand(NotFoundCommand::class)
    }

    object ConfigKey {
        const val NOT_FOUND_DISPATCHER_COMMAND = "not_found_dispatcher.command"
    }
}