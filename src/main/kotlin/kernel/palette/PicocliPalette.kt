package burrow.kernel.palette

import picocli.CommandLine

class PicocliPalette : Palette() {
    override fun color(text: String, highlight: Highlight): String {
        val (fg, bg, style) = highlight
        if (style and Highlight.Style.NONCOMBINE > 0) {
            return text
        }

        val modifierList = mutableListOf<String>()
        if (fg > 0) modifierList.add("fg($fg)")
        if (bg > 0) modifierList.add("bg($bg)")
        if (style and Highlight.Style.BOLD > 0) modifierList.add("bold")
        if (style and Highlight.Style.ITALIC > 0) modifierList.add("italic")
        if (style and Highlight.Style.UNDERLINE > 0) modifierList.add("underline")
        if (style and Highlight.Style.REVERSE > 0) modifierList.add("reverse")

        return if (modifierList.isEmpty()) text
        else {
            val modifier = modifierList.joinToString(",")
            CommandLine.Help.Ansi.ON.string("@|$modifier $text|@")
        }
    }
}