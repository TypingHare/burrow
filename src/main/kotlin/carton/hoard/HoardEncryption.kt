package burrow.carton.hoard

import burrow.carton.cradle.Cradle
import burrow.carton.hoard.command.encryption.HoardSaveCommand
import burrow.common.converter.StringConverterPair
import burrow.kernel.Burrow
import burrow.kernel.config.Config
import burrow.kernel.furniture.Furnishing
import burrow.kernel.furniture.Renovator
import burrow.kernel.furniture.annotation.Dependency
import burrow.kernel.furniture.annotation.Furniture
import burrow.kernel.furniture.annotation.RequiredDependencies
import burrow.kernel.path.Persistable
import java.io.File
import java.nio.file.Path
import kotlin.io.path.deleteIfExists

@Furniture(
    version = Burrow.VERSION,
    description = "Encrypts the storage.",
    type = Furniture.Type.MAIN
)
@RequiredDependencies(
    Dependency(Hoard::class, Burrow.VERSION),
    Dependency(Cradle::class, Burrow.VERSION)
)
class HoardEncryption(renovator: Renovator) : Furnishing(renovator),
    Persistable {
    private val hoard = use(Hoard::class)
    private val path = Path.of(hoard.getPath().toString() + ".enc")

    override fun prepareConfig(config: Config) {
        registerConfigKey(
            ConfigKey.ENCRYPT_STORAGE,
            StringConverterPair.BOOLEAN
        )
        registerConfigKey(ConfigKey.SECRET)
    }

    override fun modifyConfig(config: Config) {
        config.setIfAbsent(ConfigKey.ENCRYPT_STORAGE, true)
        config.setIfAbsent(ConfigKey.SECRET, "123456")
    }

    override fun assemble() {
        registerCommand(HoardSaveCommand::class)
    }

    override fun discard() {
        encryptStorageFile()
        hoard.getPath().deleteIfExists()
    }

    override fun getPath(): Path = path

    override fun save() {
        hoard.save()
        encryptStorageFile()
        hoard.getPath().deleteIfExists()
    }

    override fun load() {
        decryptStorageFile()
        hoard.load()
        hoard.getPath().deleteIfExists()
    }

    /**
     * Decrypts the storage file before it is loaded by Hoard.
     */
    private fun decryptStorageFile() {
        if (!config.getNotNull<Boolean>(ConfigKey.ENCRYPT_STORAGE)) {
            return
        }

        val storagePath = hoard.storage.getPath()
        if (!File("$storagePath.enc").exists()) {
            return
        }

        val secret = config.getNotNull<String>(ConfigKey.SECRET)
        val systemCommand = """
                openssl enc -aes-256-cbc -d -in $storagePath.enc -out $storagePath -k '$secret'
            """.trimIndent()

        use(Cradle::class).executeCommand(systemCommand)
    }

    /**
     * Encrypts the storage file after it is saved by Hoard.
     */
    private fun encryptStorageFile() {
        if (!config.getNotNull<Boolean>(ConfigKey.ENCRYPT_STORAGE)) {
            return
        }

        val storagePath = hoard.storage.getPath()
        val secret = config.getNotNull<String>(ConfigKey.SECRET)
        val systemCommand = """
                openssl enc -aes-256-cbc -salt -in $storagePath -out $storagePath.enc -k '$secret'
            """.trimIndent()

        use(Cradle::class).executeCommand(systemCommand)
    }

    object ConfigKey {
        const val ENCRYPT_STORAGE = "hoard.encrypt_storage"
        const val SECRET = "hoard.secret"
    }
}