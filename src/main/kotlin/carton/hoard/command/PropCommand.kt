package burrow.carton.hoard.command

import burrow.carton.hoard.Hoard
import burrow.carton.hoard.checkId
import burrow.kernel.command.Command
import burrow.kernel.command.CommandData
import picocli.CommandLine
import picocli.CommandLine.*

@CommandLine.Command(
    name = "prop",
    description = ["Retrieve the value of a property."]
)
class PropCommand(data: CommandData) : Command(data) {
    @Parameters(
        index = "0",
        description = [
            "The unique ID of the entry."
        ]
    )
    var id = 0

    @Parameters(
        index = "1",
        description = [
            "The key that associated with the value to retrieve."
        ]
    )
    var key = ""

    @Option(
        names = ["-r", "--raw"],
        description = ["Retrieve raw property."]
    )
    var shouldRetrieveRawValue = false

    override fun call(): Int {
        if (!checkId(id, stderr)) return ExitCode.USAGE
        if (key.isEmpty()) return ExitCode.OK

        val hoard = use(Hoard::class)
        val entry = hoard[id]
        if (shouldRetrieveRawValue) {
            val rawValue = entry.getProperty(key)
            if (rawValue == null) {
                stdout.println(palette.color("null", Hoard.Highlights.NULL))
            } else {
                stdout.println(
                    palette.color(
                        "\"$rawValue\"",
                        Hoard.Highlights.VALUE
                    )
                )
            }
        } else {
            val rawValue = entry.get<Any>(key)?.toString()
            if (rawValue == null) {
                stdout.println(palette.color("null", Hoard.Highlights.NULL))
            } else {
                stdout.println(
                    palette.color(
                        rawValue.toString(),
                        Hoard.Highlights.VALUE
                    )
                )
            }
        }

        return ExitCode.OK
    }
}