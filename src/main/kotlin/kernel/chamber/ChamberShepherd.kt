package burrow.kernel.chamber

import burrow.kernel.Burrow
import burrow.kernel.event.Event
import burrow.kernel.furniture.Blueprint
import burrow.kernel.path.PathBound
import java.nio.file.Path

class ChamberShepherd(val burrow: Burrow) : PathBound {
    private val rootPath = burrow.getPath().resolve(CHAMBERS_DIR)

    val chambers = mutableMapOf<String, Chamber>()
    private val blueprints = mutableMapOf<String, Blueprint>()

    override fun getPath(): Path = rootPath

    @Throws(BuildChamberException::class)
    fun buildChamber(chamberName: String) {
        val chamber = Chamber(this, chamberName)

        try {
            burrow.courier.post(ChamberPreBuildEvent(chamber))
            chamber.checkChamberRootDirectory()
            chamber.renovator.load()
            chamber.config.load()
            chamber.renovator.initializeFurnishings()
            burrow.courier.post(ChamberPostBuildEvent(chamber))

        } catch (ex: Exception) {
            throw BuildChamberException(chamberName, ex)
        }

        chambers[chamberName] = chamber
        blueprints[chamberName] = Blueprint(
            chamber.config.clone(),
            chamber.renovator.furnishingIds
        )
    }

    operator fun get(chamberName: String): Chamber =
        getOrBuildChamber(chamberName)

    @Throws(BuildChamberException::class)
    fun getOrBuildChamber(chamberName: String): Chamber {
        if (!chamberExists(chamberName)) {
            buildChamber(chamberName)
        }

        return getChamber(chamberName)!!
    }

    private fun chamberExists(chamberName: String): Boolean =
        chambers.containsKey(chamberName)

    private fun getChamber(chamberName: String): Chamber? =
        chambers[chamberName]

    @Throws(ChamberNotBuiltException::class)
    private fun getBuiltChamber(chamberName: String): Chamber =
        chambers[chamberName] ?: throw ChamberNotBuiltException(chamberName)

    @Throws(ChamberNotBuiltException::class, DestroyChamberException::class)
    fun destroyChamber(chamberName: String) {
        val chamber = getBuiltChamber(chamberName)

        try {
            burrow.courier.post(ChamberPreDestroyEvent(chamber))
            chamber.config.save()
            chamber.renovator.discardFurnishings()
            burrow.courier.post(ChamberPostDestroyEvent(chamber))
        } catch (ex: Exception) {
            throw DestroyChamberException(chamberName, ex)
        }

        chambers.remove(chamberName)
        blueprints.remove(chamberName)
    }

    @Throws(
        ChamberNotBuiltException::class,
        DestroyChamberException::class,
        BuildChamberException::class
    )
    fun rebuildChamber(chamberName: String) {
        destroyChamber(chamberName)
        buildChamber(chamberName)
    }

    @Throws(ChamberNotBuiltException::class)
    fun getBluePrint(chamberName: String): Blueprint =
        blueprints[chamberName] ?: throw ChamberNotBuiltException(chamberName)

    companion object {
        /**
         * The relative path to the chambers root directory.
         */
        const val CHAMBERS_DIR = "chambers"

        /**
         * The name of the root chamber.
         */
        const val ROOT_CHAMBER_NAME = "."
    }
}

class BuildChamberException(chamberName: String, cause: Exception) :
    RuntimeException("Failed to build chamber: $chamberName", cause)

class ChamberNotBuiltException(chamberName: String) :
    RuntimeException("Chamber not built: $chamberName")

class DestroyChamberException(chamberName: String, cause: Exception) :
    RuntimeException("Failed to destroy chamber: $chamberName", cause)

class ChamberPreBuildEvent(val chamber: Chamber) : Event()
class ChamberPostBuildEvent(val chamber: Chamber) : Event()
class ChamberPreDestroyEvent(val chamber: Chamber) : Event()
class ChamberPostDestroyEvent(val chamber: Chamber) : Event()