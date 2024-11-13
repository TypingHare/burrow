package burrow.kernel.chamber

abstract class ChamberModule(protected val chamber: Chamber) {
    protected val burrow = chamber.burrow
    protected val config = chamber.config
    protected val renovator = chamber.renovator
    protected val processor = chamber.processor
}