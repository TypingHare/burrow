package burrow.kernel.terminal

data class TerminalSize(val width: Int, val height: Int) {
    companion object {
        @JvmStatic
        fun fromString(string: String): TerminalSize {
            val (width, height) = string.split(" ").map(String::toInt)
            return TerminalSize(width, height)
        }
    }

    override fun toString(): String = "$width $height"
}