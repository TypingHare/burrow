package burrow.carton.hoard

import burrow.carton.hoard.command.tag.TagAddCommand
import burrow.carton.hoard.command.tag.TagDelCommand
import burrow.carton.hoard.command.tag.TagListCommand
import burrow.carton.hoard.command.tag.TagsCommand
import burrow.common.converter.StringConverterPair
import burrow.kernel.Burrow
import burrow.kernel.furniture.Furnishing
import burrow.kernel.furniture.Renovator
import burrow.kernel.furniture.annotation.Dependency
import burrow.kernel.furniture.annotation.Furniture
import burrow.kernel.furniture.annotation.RequiredDependencies

@Furniture(
    version = Burrow.VERSION,
    description = "",
    type = Furniture.Type.COMPONENT
)
@RequiredDependencies(
    Dependency(Hoard::class, Burrow.VERSION)
)
class HoardTag(renovator: Renovator) : Furnishing(renovator) {
    val tagSet = mutableSetOf<String>()

    override fun assemble() {
        registerCommand(TagAddCommand::class)
        registerCommand(TagDelCommand::class)
        registerCommand(TagsCommand::class)
        registerCommand(TagListCommand::class)

        use(Hoard::class).storage.addMapping(
            EntryKey.TAGS,
            ConverterParis.TAG_SET
        )
    }

    override fun launch() {
        use(Hoard::class).storage.getAllEntries().forEach {
            tagSet.addAll(getTags(it))
        }
    }

    fun getTags(entry: Entry): MutableSet<String> =
        entry.get<MutableSet<String>>(EntryKey.TAGS)!!

    companion object {
        const val TAG_DELIMITER = "||"
    }

    object EntryKey {
        const val TAGS = "tags"
    }

    object ConverterParis {
        val TAG_SET = StringConverterPair(
            { it.split(TAG_DELIMITER).toMutableSet() },
            { it?.joinToString(TAG_DELIMITER) ?: "" }
        )
    }
}