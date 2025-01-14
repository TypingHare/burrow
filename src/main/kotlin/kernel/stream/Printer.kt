package burrow.kernel.stream

import java.io.PrintWriter

abstract class Printer<C>(
    protected val writer: PrintWriter,
    protected val context: C
) {
    abstract fun print()
}