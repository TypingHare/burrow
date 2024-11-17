package burrow.carton.dictator

import burrow.carton.dictator.command.ChamberDestroyCommand
import burrow.carton.dictator.command.ChamberListCommand
import burrow.carton.standard.Standard
import burrow.kernel.chamber.Chamber
import burrow.kernel.chamber.ChamberPostBuildEvent
import burrow.kernel.chamber.ChamberPostDestroyEvent
import burrow.kernel.furnishing.DependsOn
import burrow.kernel.furnishing.Furnishing
import burrow.kernel.furnishing.Furniture

@DependsOn([Standard::class])
@Furniture(
    version = "0.0.0",
    description = "Dictator allows developers to manage chambers.",
    type = Furniture.Type.ROOT
)
class Dictator(chamber: Chamber) : Furnishing(chamber) {
    // Mapping from chamber names to chamber info
    val chamberInfoMap = mutableMapOf<String, ChamberInfo>()

    override fun assemble() {
        registerCommand(ChamberListCommand::class)
        registerCommand(ChamberDestroyCommand::class)

        burrow.affairManager.subscribe(ChamberPostBuildEvent::class) {
            val chamber = it.chamber
            val name = chamber.name
            val alias = config.get<String>(Standard.ConfigKey.ALIAS)!!
            val description =
                config.get<String>(Standard.ConfigKey.DESCRIPTION)!!
            chamberInfoMap[name] = ChamberInfo(name, alias, description)
        }

        burrow.affairManager.subscribe(ChamberPostDestroyEvent::class) {
            chamberInfoMap.remove(it.chamber.name)
        }
    }

    fun getAllChamberInfo(): List<ChamberInfo> {
        val chamberInfoList = mutableListOf<ChamberInfo>()
        for (file in burrow.chambersPath.toFile().listFiles()!!) {
            if (file == null || !file.isDirectory()) {
                continue
            }

            val name = file.name
            chamberInfoList.add(ChamberInfo(name, name, ""))
        }

        return chamberInfoList
    }
}

data class ChamberInfo(
    val name: String,
    val alias: String,
    val description: String,
)