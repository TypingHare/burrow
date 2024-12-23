package burrow.kernel.chamber

abstract class ChamberModule(protected val chamber: Chamber) {
    protected val burrow = chamber.burrow
}

abstract class ExtendedChamberModule(chamber: Chamber) :
    ChamberModule(chamber) {
    protected val config = chamber.config
    protected val renovator = chamber.renovator
    protected val processor = chamber.processor
    protected val affairManager = chamber.courier
    protected val palette = chamber.palette
}