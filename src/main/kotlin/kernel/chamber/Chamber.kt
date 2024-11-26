package burrow.kernel.chamber

import burrow.kernel.Burrow
import burrow.kernel.command.Processor
import burrow.kernel.config.Config
import burrow.kernel.event.EventBus
import burrow.kernel.furnishing.Furnishing
import burrow.kernel.furnishing.FurnishingNotFoundException
import burrow.kernel.furnishing.Renovator
import burrow.kernel.palette.Palette
import burrow.kernel.palette.PicocliPalette
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.reflect.KClass

class Chamber(val burrow: Burrow, val name: String) {
    val rootPath: Path = burrow.chambersPath.resolve(name).normalize()

    val config = Config(this)
    val renovator = Renovator(this)
    val processor = Processor(this)
    val affairManager = EventBus()
    var palette: Palette = PicocliPalette()

    @Throws(BuildChamberException::class)
    fun build() {
        try {
            checkChamberRootDirectory()
            renovator.loadFurnishings()
            config.loadFromFile()
            renovator.initializeFurnishings()
        } catch (throwable: Throwable) {
            throw BuildChamberException(name, throwable)
        }
    }

    private fun checkChamberRootDirectory() {
        if (name.isEmpty() || !rootPath.isDirectory()) {
            throw ChamberNotFoundException(name)
        }
    }

    fun destroy() {
        renovator.depTree.resolveWithoutDuplicates { it.discard() }
        config.saveToFile()
    }

    @Throws(BuildChamberException::class, DestroyChamberException::class)
    fun rebuild() {
        val chamberShepherd = burrow.chamberShepherd
        chamberShepherd.destroyChamber(name)
        chamberShepherd.buildChamber(name)
    }

    @Throws(FurnishingNotFoundException::class)
    fun <F : Furnishing> use(furnishingClass: KClass<F>): F {
        return renovator.getFurnishing(furnishingClass)
            ?: throw FurnishingNotFoundException(furnishingClass.java.name)
    }
}

class ChamberNotFoundException(private val name: String) :
    RuntimeException("Chamber not found: $name") {
    fun getName(): String = name
}

class ConfigFileNotFoundException(private val path: Path) :
    RuntimeException("Config file not found: $path") {
    fun getPath(): Path = path
}

class BuildChamberException(private val name: String, cause: Throwable) :
    RuntimeException("Failed to build chamber: $name", cause) {
    fun getName(): String = name
}

class DestroyChamberException(private val name: String, cause: Throwable) :
    RuntimeException("Failed to destroy chamber: $name", cause) {
    fun getName(): String = name
}