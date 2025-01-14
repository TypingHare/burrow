package burrow.kernel.chamber

abstract class ChamberModule(val chamber: Chamber) {
    val burrow = chamber.chamberShepherd.burrow
    val warehouse = burrow.warehouse
    val chamberShepherd = burrow.chamberShepherd
}

abstract class ExtendedChamberModule(chamber: Chamber) :
    ChamberModule(chamber) {
    val courier = chamber.courier
    val config = chamber.config
    val renovator = chamber.renovator
    val interpreter = chamber.interpreter
}