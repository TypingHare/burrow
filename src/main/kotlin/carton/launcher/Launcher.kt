package burrow.carton.launcher

import burrow.carton.cradle.Cradle
import burrow.carton.hoard.Entry
import burrow.carton.hoard.HoardPair
import burrow.carton.launcher.command.LaunchCommand
import burrow.carton.launcher.command.LauncherSetCommand
import burrow.kernel.Burrow
import burrow.kernel.config.Config
import burrow.kernel.furniture.Furnishing
import burrow.kernel.furniture.Renovator
import burrow.kernel.furniture.annotation.Dependency
import burrow.kernel.furniture.annotation.Furniture
import burrow.kernel.furniture.annotation.RequiredDependencies

@Furniture(
    version = Burrow.VERSION,
    description = "Launch the value associated with a given name.",
    type = Furniture.Type.COMPONENT
)
@RequiredDependencies(
    Dependency(HoardPair::class, Burrow.VERSION),
    Dependency(Cradle::class, Burrow.VERSION)
)
class Launcher(renovator: Renovator) : Furnishing(renovator) {
    override fun prepareConfig(config: Config) {
        registerConfigKey(ConfigKey.DEFAULT_LAUNCHER)
    }

    override fun modifyConfig(config: Config) {
        config.setIfAbsent(ConfigKey.DEFAULT_LAUNCHER, "open")
    }

    override fun assemble() {
        registerCommand(LaunchCommand::class)
        registerCommand(LauncherSetCommand::class)
    }

    object ConfigKey {
        const val DEFAULT_LAUNCHER = "launcher.default_launcher"
    }

    object EntryKey {
        const val LAUNCHER = "launcher"
    }
}

fun extractLauncher(entry: Entry): String =
    entry.get<String>(Launcher.EntryKey.LAUNCHER)
        ?: throw LauncherNotDefinedException(entry.id)

class LauncherNotDefinedException(val id: Int) :
    RuntimeException("Launcher not defined for entry: $id")