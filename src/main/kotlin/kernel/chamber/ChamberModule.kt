package burrow.kernel.chamber

import burrow.kernel.palette.Palette

abstract class ChamberModule(protected val chamber: Chamber) {
    protected val burrow = chamber.burrow
    protected val config = chamber.config
    protected val renovator = chamber.renovator
    protected val processor = chamber.processor
    protected val affairManager = chamber.affairManager
    protected val palette: Palette = chamber.palette
}