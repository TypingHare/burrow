package burrow.kernel.chamber

import burrow.kernel.DirectoryBound
import burrow.kernel.command.Processor
import burrow.kernel.config.Config
import burrow.kernel.event.EventBus
import burrow.kernel.furnishing.Renovator
import kotlin.io.path.isDirectory

class Chamber(
    val chamberShepherd: ChamberShepherd,
    val name: String
) : DirectoryBound(chamberShepherd.rootDirPath.resolve(name)) {

    val config = Config(this)
    val renovator = Renovator(this)
    val processor = Processor(this)
    val courier = EventBus()

    @Throws(BlueprintNotFoundException::class)
    fun checkChamberRootDirectory() {
        if (name.isBlank() || !rootDirPath.isDirectory()) {
            throw BlueprintNotFoundException(name)
        }
    }
}

class BlueprintNotFoundException(chamberName: String) :
    RuntimeException("Chamber not found: $chamberName")