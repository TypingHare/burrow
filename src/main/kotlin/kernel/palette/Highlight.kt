package burrow.kernel.palette

data class Highlight(val fg: Int, val bg: Int, val style: Int) {
    object Style {
        const val NONE = 0b0
        const val BOLD = 0b1
        const val ITALIC = 0b10
        const val UNDERLINE = 0b100
        const val UNDERCURL = 0b1000
        const val STRIKETHROUGH = 0b10000
        const val REVERSE = 0b100000
        const val NONCOMBINE = 0b1000000
    }
}