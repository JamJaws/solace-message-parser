package com.jamjaws.solace.message.parser

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import java.io.File

class App(private val inputPathname: String, private val outputPathname: String) {

    private val mapper = jacksonObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)

    fun files(): List<File> =
        File(inputPathname).also { println(it.absolutePath) }.listFiles { _, name -> name.endsWith(".txt") }?.toList()
            .orEmpty()

    fun parse(file: File): List<Message> =
        file.useLines { lines ->
            lines.windowed(2, 1, true)
                .map { Pair(it.first(), it.getOrNull(1)) }
                .fold<Pair<String, String?>, Pair<List<Message>, Message>>(
                    Pair(emptyList(), Message())
                ) { (messages, message), (line, nextLine) ->
                    when {
                        line.startsWith("JMSDestination:") -> {
                            messages to message.copy(jmsDestination = line.substringAfter("JMSDestination:").trim())
                        }

                        line.startsWith("JMSMessageID:") -> {
                            messages to message.copy(jmsMessageID = line.substringAfter("JMSMessageID:").trim())
                        }

                        line.startsWith("JMSTimestamp:") -> {
                            messages to message.copy(jmsTimestamp = line.substringAfter("JMSTimestamp:").trim())
                        }

                        line.startsWith("JMSType:") -> {
                            messages to message.copy(jmsType = line.substringAfter("JMSType:").trim())
                        }

                        line.contains("^ {2}[0-9a-z]{2} ".toRegex()) -> {
                            val updatedMessage = line.substringAfterLast("    ").trim()
                                .let { message.copy(json = message.json + it) }
                            //  handle the last line of the message
                            if (nextLine?.isBlank() != false) {
                                try {
                                    mapper.readTree(updatedMessage.json)
                                    messages + updatedMessage.copy(validJson = true) to Message()
                                } catch (e: Exception) {
                                    messages + updatedMessage.copy(validJson = false, exception = e) to Message()
                                }
                            } else {
                                messages to updatedMessage
                            }
                        }

                        else -> Pair(messages, message)
                    }
                }.first
        }

    fun print(message: Message) {
        println(
            """${message.jmsMessageID}
            | destination: ${message.jmsDestination}
            | timestamp: ${message.jmsTimestamp}
            | type: ${message.jmsType}
            | json valid: ${if (message.validJson) "✅" else "❌"}
            | ${message.json}
            ${if (message.exception != null) "| ${message.exception.stackTraceToString()}" else "|"} 
            | 
            """.trimMargin()
        )
    }

    fun writeJson(message: Message, outputFolder: File = File(outputPathname)) {
        val directory = File(outputFolder, message.destination).also(File::mkdirs)
        if (message.validJson) {
            mapper.writeValue(File(directory, "${message.jmsMessageID}.json"), mapper.readTree(message.json))
        } else {
            File(directory, "${message.jmsMessageID}.json").writeText(message.json)
        }
    }
}

fun main(args: Array<String>) {
    val parser = ArgParser("Solace message parser")
    val input by parser.option(ArgType.String, shortName = "i", description = "Input directory").default("input")
    val output by parser.option(ArgType.String, shortName = "o", description = "Output file name").default("output")
    parser.parse(args)

    val app = App(input, output)
    app.files().flatMap(app::parse).onEach(app::print).forEach(app::writeJson)
}
