package com.jamjaws.solace.message.parser

import com.fasterxml.jackson.core.JsonParseException
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import java.io.File
import java.nio.file.Path
import kotlin.io.path.createTempDirectory
import kotlin.io.path.deleteExisting
import kotlin.test.Test

class AppTest {
    private val app = App(AppTest::class.java.getResource("/input")!!.file, "output")
    private val tempDir = createTempDirectory().also(Path::deleteExisting)

    @Test
    fun `should list txt files messages`() {
        app.files().size shouldBe 2
    }

    @Test
    fun `should set validJson true for valid json in event`() {
        app.files().flatMap(app::parse).map(Message::validJson).count { it } shouldBe 1
    }

    @Test
    fun `should set validJson false for valid json in event`() {
        app.files().flatMap(app::parse).map(Message::validJson).count { !it } shouldBe 1
    }

    @Test
    fun `should parse destination`() {
        app.parse(file("input_1.txt")).single().destination shouldBe "jamjaws.parser"
    }

    @Test
    fun `should parse message id`() {
        app.parse(file("input_1.txt")).single().jmsMessageID shouldBe "ID:123.123.123.bib7sh81hw0876npfky:550"
    }

    @Test
    fun `should parse message type`() {
        app.parse(file("input_1.txt")).single().jmsType shouldBe "COOL_TYPE"
    }

    @Test
    fun `should parse message json`() {
        app.parse(file("input_1.txt"))
            .single().json shouldBe """{"everything":42,"date":"2024-06-07","name":"Jane Doe","nested":{"nested":"value"}}"""
    }

    @Test
    fun `should have no exception for a valid message`() {
        app.parse(file("input_1.txt")).single().exception shouldBe null
    }

    @Test
    fun `should have exception for a invalid message`() {
        app.parse(file("input_2.txt")).single().exception.shouldBeTypeOf<JsonParseException>()
    }

    @Test
    fun `should parse json for a invalid message`() {
        app.parse(file("input_2.txt"))
            .single().json shouldBe """{"everything":42,"date":"2024-06-07","name":"Jane Doe","nested":{nested":"value"}}"""
    }

    @Test
    fun `should write json content to a file`() {
        app.parse(file("input_1.txt")).single().let { app.writeJson(it, tempDir.toFile()) }
        val file = File(tempDir.toFile(), "jamjaws.parser/ID:123.123.123.bib7sh81hw0876npfky:550.json")
        file.isFile shouldBe true
        file.readText() shouldBe """
            {
              "everything" : 42,
              "date" : "2024-06-07",
              "name" : "Jane Doe",
              "nested" : {
                "nested" : "value"
              }
            }
        """.trimIndent()
    }

    private fun file(name: String) = File(AppTest::class.java.getResource("/input/$name")!!.file)
}
