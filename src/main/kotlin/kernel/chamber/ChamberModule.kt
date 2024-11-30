package burrow.kernel.chamber

abstract class ChamberModule(val chamber: Chamber) {
    protected val burrow = chamber.chamberShepherd.burrow
}

abstract class ExtendedChamberModule(chamber: Chamber) :
    ChamberModule(chamber) {
    protected val config = chamber.config
    protected val renovator = chamber.renovator
    protected val processor = chamber.processor
    protected val courier = chamber.courier
}