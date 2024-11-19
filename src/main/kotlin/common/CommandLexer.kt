package burrow.common

import java.util.*

object CommandLexer {
    @JvmStatic
    fun tokenizeCommandString(commandString: String): List<String> {
        val tokens = mutableListOf<String>()
        val tokenizer = StringTokenizer(commandString, " \"", true)
        var insideQuote = false
        var currentToken = StringBuilder()

        while (tokenizer.hasMoreTokens()) {
            val token = tokenizer.nextToken()
            when {
                token == "\"" -> {
                    insideQuote = !insideQuote
                    if (!insideQuote && currentToken.isNotEmpty()) {
                        tokens.add(currentToken.toString())
                        currentToken = StringBuilder()
                    }
                }

                token == " " && !insideQuote -> {
                    if (currentToken.isNotEmpty()) {
                        tokens.add(currentToken.toString())
                        currentToken = StringBuilder()
                    }
                }

                else -> currentToken.append(token)
            }
        }
        if (currentToken.isNotEmpty()) {
            tokens.add(currentToken.toString())
        }
        return tokens
    }
}