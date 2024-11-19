package burrow.kernel.palette

abstract class Palette {
    abstract fun color(text: String, highlight: Highlight): String
}