package burrow.carton.dictator

import burrow.carton.dictator.command.*
import burrow.carton.standard.Standard
import burrow.kernel.Burrow
import burrow.kernel.chamber.Chamber
import burrow.kernel.chamber.ChamberPostBuildEvent
import burrow.kernel.chamber.ChamberPostDestroyEvent
import burrow.kernel.furnishing.Furnishing
import burrow.kernel.furnishing.annotation.DependsOn
import burrow.kernel.furnishing.annotation.Furniture
import com.google.gson.Gson
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

@DependsOn(Standard::class)
@Furniture(
    version = Burrow.VERSION.NAME,
    description = "Dictator allows developers to manage chambers.",
    type = Furniture.Type.ROOT
)
class Dictator(chamber: Chamber) : Furnishing(chamber) {
    // Mapping from chamber names to chamber info
    val chamberInfoMap = mutableMapOf<String, ChamberInfo>()

    override fun assemble() {
        registerCommand(ChamberNewCommand::class)
        registerCommand(ChamberListCommand::class)
        registerCommand(ChamberBuildCommand::class)
        registerCommand(ChamberDestroyCommand::class)
        registerCommand(ChamberExistCommand::class)

        burrow.affairManager.subscribe(ChamberPostBuildEvent::class) {
            val chamber = it.chamber
            val chamberName = chamber.name
            val alias = chamber.config.get<String>(Standard.ConfigKey.ALIAS)!!
            val description =
                config.get<String>(Standard.ConfigKey.DESCRIPTION)!!
            chamberInfoMap[chamberName] =
                ChamberInfo(chamberName, alias, description)
        }

        burrow.affairManager.subscribe(ChamberPostDestroyEvent::class) {
            val chamberName = it.chamber.name
            chamberInfoMap.remove(chamberName)
        }
    }

    fun getAllChamberDirs(): List<File> {
        return burrow.chambersPath.toFile().listFiles()!!.toList()
            .filter { it.isDirectory() }
    }

    fun getAllChamberInfo(): List<ChamberInfo> {
        val chamberInfoList = mutableListOf<ChamberInfo>()
        for (dir in getAllChamberDirs()) {
            val name = dir.name
            chamberInfoList.add(ChamberInfo(name, name, ""))
        }

        return chamberInfoList
    }

    fun createFurnishingsJson(path: Path) {
        val furnishingsJsonPath =
            path.resolve(Burrow.Standard.FURNISHINGS_FILE_NAME)
        val content = Gson().toJson(Default.FURNISHING_LIST)
        Files.write(furnishingsJsonPath, content.toByteArray())
    }

    object Default {
        val FURNISHING_LIST = listOf(Standard::class.java.name)
    }
}

data class ChamberInfo(
    val name: String,
    val alias: String,
    val description: String,
)