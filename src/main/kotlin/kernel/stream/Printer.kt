package burrow.kernel.stream

import burrow.kernel.chamber.Chamber
import java.io.PrintWriter

abstract class Printer<C>(
    protected val writer: PrintWriter,
    protected val context: C
) {
    abstract fun print()
}

abstract class ChamberBasedPrinter(
    writer: PrintWriter,
    protected val chamber: Chamber
) :
    Printer<Chamber>(writer, chamber)