package burrow.kernel.stream

import java.io.PrintWriter

abstract class PrintTask<C>(
    protected val writer: PrintWriter,
    protected val context: C
) {
    abstract fun print()
}