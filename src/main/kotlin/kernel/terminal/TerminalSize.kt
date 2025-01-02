package burrow.kernel.terminal

data class TerminalSize(val width: Int, val height: Int) {
    companion object {
        fun parse(string: String): TerminalSize {
            val (width, height) = string.split(" ").map(String::toInt)
            return TerminalSize(width, height)
        }
    }

    override fun toString(): String = "$width $height"
}