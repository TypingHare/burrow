package burrow.kernel.chamber

abstract class ChamberModule(val chamber: Chamber) {
    protected val burrow = chamber.chamberShepherd.burrow
    protected val chamberShepherd = chamber.chamberShepherd
}

abstract class ExtendedChamberModule(chamber: Chamber) :
    ChamberModule(chamber) {
    protected val courier = chamber.courier
    protected val config = chamber.config
    protected val renovator = chamber.renovator
    protected val interpreter = chamber.interpreter
}