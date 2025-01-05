package burrow.common.palette

import picocli.CommandLine

class PicocliPalette : Palette() {
    override fun color(text: String, highlight: Highlight): String {
        val (fg, bg, style) = highlight
        if (style and Style.NONCOMBINE > 0) {
            return text
        }

        val modifierList = mutableListOf<String>()
        if (fg > 0) modifierList.add("fg($fg)")
        if (bg > 0) modifierList.add("bg($bg)")
        if (style and Style.BOLD > 0) modifierList.add("bold")
        if (style and Style.ITALIC > 0) modifierList.add("italic")
        if (style and Style.UNDERLINE > 0) modifierList.add("underline")
        if (style and Style.REVERSE > 0) modifierList.add("reverse")

        return when (modifierList.isEmpty()) {
            true -> text
            false -> modifierList.joinToString(",").let {
                CommandLine.Help.Ansi.ON.string("@|$it $text|@")
            }
        }
    }
}