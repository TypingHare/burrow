package burrow.carton.dictator

import burrow.carton.core.Core
import burrow.carton.dictator.command.ChamberExistCommand
import burrow.carton.dictator.command.ChamberListCommand
import burrow.kernel.Burrow
import burrow.kernel.chamber.ChamberPostBuildEvent
import burrow.kernel.chamber.ChamberPostDestroyEvent
import burrow.kernel.config.Config
import burrow.kernel.furniture.Furnishing
import burrow.kernel.furniture.Renovator
import burrow.kernel.furniture.annotation.Dependency
import burrow.kernel.furniture.annotation.Furniture
import burrow.kernel.furniture.annotation.RequiredDependencies
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.nio.file.Files
import kotlin.io.path.exists

@Furniture(
    version = Burrow.VERSION,
    description = "Allows developers to manage chambers.",
    type = Furniture.Type.ROOT
)
@RequiredDependencies(Dependency(Core::class, Burrow.VERSION))
class Dictator(renovator: Renovator) : Furnishing(renovator) {
    val chamberInfoMap = mutableMapOf<String, ChamberInfo>()

    override fun assemble() {
        registerCommand(ChamberExistCommand::class)
        registerCommand(ChamberListCommand::class)

        burrow.courier.subscribe(ChamberPostBuildEvent::class) {
            val chamber = it.chamber
            val chamberName = chamber.name

            chamberInfoMap[chamberName] = ChamberInfo(
                chamberName,
                config[Core.ConfigKey.DESCRIPTION] ?: ""
            )
        }

        burrow.courier.subscribe(ChamberPostDestroyEvent::class) {
            chamberInfoMap.remove(it.chamber.name)
        }
    }

    private fun getAllChamberDirs(
        addChambersDirectory: Boolean = false
    ): List<File> =
        chamberShepherd.getPath().toFile()
            .listFiles()!!
            .toMutableList()
            .apply {
                if (addChambersDirectory) {
                    add(chamberShepherd.getPath().toFile())
                }
            }
            .filter { it.isDirectory() }

    fun getAllChamberNames(): List<String> =
        getAllChamberDirs().map { it.name }

    fun getBuiltChamberInfoList(): List<ChamberInfo> =
        chamberInfoMap.values.toList()

    fun getAvailableChamberInfoList(): List<ChamberInfo> =
        getAllChamberDirs().map { file ->
            val chamberName = file.name
            ChamberInfo(chamberName, getDescriptionFromConfigFile(chamberName))
        }

    private fun getDescriptionFromConfigFile(chamberName: String): String {
        val configFilePath = chamberShepherd.getPath()
            .resolve(chamberName)
            .resolve(Config.FILE_NAME)
        if (!configFilePath.exists()) {
            return ""
        }

        val content = Files.readString(configFilePath)
        val type = object : TypeToken<Map<String, String>>() {}.type
        val map = Gson().fromJson<Map<String, String>>(content, type)

        return map["description"] ?: ""
    }
}