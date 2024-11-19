package burrow.kernel.chamber

import burrow.kernel.Burrow
import burrow.kernel.event.Event

class ChamberShepherd(private val burrow: Burrow) {
    val chambers = mutableMapOf<String, Chamber>()

    @Throws(BuildChamberException::class)
    fun buildChamber(chamberName: String) {
        val chamber = Chamber(burrow, chamberName)

        burrow.affairManager.post(ChamberPreBuildEvent(chamber))
        chamber.build()
        burrow.affairManager.post(ChamberPostBuildEvent(chamber))

        chambers[chamberName] = chamber
    }

    private fun hasChamber(chamberName: String) =
        chambers.containsKey(chamberName)

    private fun getChamber(chamberName: String) = chambers[chamberName]

    private fun getOrBuildChamber(chamberName: String): Chamber {
        if (!hasChamber(chamberName)) {
            buildChamber(chamberName)
        }

        return getChamber(chamberName)!!
    }

    operator fun get(chamberName: String): Chamber =
        getOrBuildChamber(chamberName)

    fun destroyChamber(chamberName: String) {
        if (!hasChamber(chamberName)) {
            println("Chamber not found: $chamberName")
            return
        }

        val chamber = chambers[chamberName]!!

        burrow.affairManager.post(ChamberPreDestroyEvent(chamber))
        chamber.destroy()
        burrow.affairManager.post(ChamberPostDestroyEvent(chamber))

        chambers.remove(chamberName)
    }
}

class ChamberPreBuildEvent(val chamber: Chamber) : Event()

class ChamberPostBuildEvent(val chamber: Chamber) : Event()

class ChamberPreDestroyEvent(val chamber: Chamber) : Event()

class ChamberPostDestroyEvent(val chamber: Chamber) : Event()