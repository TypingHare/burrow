package burrow.client

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

fun main(args: Array<String>) {
//    val command = args.joinToString(" ")
    val command = ". chambers"

    try {
        val client = Socket("localhost", 12345)
        val output = PrintWriter(client.getOutputStream(), true)
        val input = BufferedReader(InputStreamReader(client.getInputStream()))

        output.println(command)

        var response: String?
        while (input.readLine().also { response = it } != null) {
            println(response)
        }

        client.close()
    } catch (e: IOException) {
        println("Error: Could not connect to Burrow server.")
    }
}