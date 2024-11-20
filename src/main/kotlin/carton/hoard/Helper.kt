package burrow.carton.hoard

import java.io.PrintWriter

fun checkId(id: Int, stderr: PrintWriter): Boolean {
    if (id <= 0) {
        stderr.println(
            "Error: Entry ID must be a positive integer. Provided ID: $id"
        )
        return false
    }

    return true
}

fun checkPairs(pairs: Array<String>, stderr: PrintWriter): Boolean {
    if (pairs.size % 2 == 1) {
        stderr.println(
            "Error: Keys and values must be provided in pairs. Received " +
                    "${pairs.size} argument(s). Example usage: key1 value1 " +
                    "key2 value2"
        )
        return false
    }

    return true
}