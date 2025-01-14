package burrow.kernel.chamber

import burrow.common.event.EventBus
import burrow.kernel.config.Config
import burrow.kernel.furniture.Renovator
import burrow.kernel.path.PathBound
import burrow.kernel.terminal.Interpreter
import java.nio.file.Path
import kotlin.io.path.isDirectory

class Chamber(val chamberShepherd: ChamberShepherd, val name: String) :
    PathBound {
    private val path = chamberShepherd.getPath().resolve(name).normalize()

    val courier = EventBus()
    val config = Config(this)
    val renovator = Renovator(this)
    val interpreter = Interpreter(this)

    override fun getPath(): Path = path

    @Throws(BlueprintNotFoundException::class)
    fun checkBlueprintDirectory() {
        if (name.isBlank() || !path.isDirectory()) {
            throw BlueprintNotFoundException(path)
        }
    }
}

class BlueprintNotFoundException(path: Path) :
    RuntimeException("Blueprint not found: $path")