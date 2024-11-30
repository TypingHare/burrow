package burrow.kernel.chamber

import burrow.kernel.Burrow
import burrow.kernel.DirectoryBound
import burrow.kernel.event.Event

class ChamberShepherd(val burrow: Burrow) : DirectoryBound(
    burrow.rootDirPath.resolve(CHAMBERS_DIR).normalize()
) {
    private val chambers = mutableMapOf<String, Chamber>()

    @Throws(BuildChamberException::class)
    fun buildChamber(chamberName: String) {
        val chamber = Chamber(this, chamberName)

        try {
            burrow.courier.post(ChamberPreBuildEvent(chamber))
            chamber.checkChamberRootDirectory()
            chamber.renovator.loadFromFile()
            chamber.config.loadFromFile()
            chamber.renovator.initializeFurnishings()
            burrow.courier.post(ChamberPostBuildEvent(chamber))
        } catch (ex: Exception) {
            throw BuildChamberException(chamberName, ex)
        }

        chambers[chamberName] = chamber
    }

    private fun chamberExists(chamberName: String): Boolean =
        chambers.containsKey(chamberName)

    private fun getChamber(chamberName: String): Chamber? =
        chambers[chamberName]

    @Throws(BuildChamberException::class)
    private fun getOrBuildChamber(chamberName: String): Chamber {
        if (chamberExists(chamberName)) {
            buildChamber(chamberName)
        }

        return getChamber(chamberName)!!
    }

    private fun getBuiltChamber(chamberName: String): Chamber =
        chambers[chamberName] ?: throw ChamberNotBuiltException(chamberName)

    @Throws(ChamberNotBuiltException::class, DestroyChamberException::class)
    fun destroyChamber(chamberName: String) {
        val chamber = getBuiltChamber(chamberName)

        try {
            burrow.courier.post(ChamberPreDestroyEvent(chamber))
            // TODO
            chamber.config.saveToFile()
            burrow.courier.post(ChamberPostDestroyEvent(chamber))
        } catch (ex: Exception) {
            throw DestroyChamberException(chamberName, ex)
        }

        chambers.remove(chamberName)
    }

    @Throws(
        ChamberNotBuiltException::class,
        DestroyChamberException::class,
        BuildChamberException::class
    )
    fun rebuild(chamberName: String) {
        destroyChamber(chamberName)
        buildChamber(chamberName)
    }

    companion object {
        /**
         * The relative path to the chambers root directory.
         */
        private const val CHAMBERS_DIR = "chambers"

        /**
         * The name of the root chamber.
         */
        const val ROOT_CHAMBER_NAME = "chamber"
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
