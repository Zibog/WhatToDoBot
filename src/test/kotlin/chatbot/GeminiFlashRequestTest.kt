package chatbot

import base.JsonTestBase
import com.dsidak.chatbot.Content
import com.dsidak.chatbot.ContentPart
import com.dsidak.chatbot.GeminiFlashRequest
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class GeminiFlashRequestTest : JsonTestBase {
    @Test
    fun testEncodeDecodeJson_simple() {
        val requestContent = GeminiFlashRequest(
            contents = listOf(
                Content(
                    parts = listOf(ContentPart(text = "Explain how AI works"))
                )
            )
        )
        val jsonRequest = json.encodeToString(requestContent)
        val file = File("$resources/GeminiRequest_simple.json")
        val expectedJson = file.readText()
        assertEquals(expectedJson, jsonRequest)

        val decodedRequest = json.decodeFromString<GeminiFlashRequest>(expectedJson)
        assertEquals(1, decodedRequest.contents.size)
        val content = decodedRequest.contents[0]
        assertEquals(1, content.parts.size)
        val part = content.parts[0]
        assertEquals("Explain how AI works", part.text)
    }

    @Test
    fun testEncodeDecodeJson_Paris() {
        val requestContent = GeminiFlashRequest(
            contents = listOf(
                Content(
                    role = "user",
                    parts = listOf(ContentPart(text = "What is the capital of France?"))
                )
            )
        )
        val jsonRequest = json.encodeToString(requestContent)
        val file = File("$resources/GeminiRequest_Paris.json")
        val expectedJson = file.readText()
        assertEquals(expectedJson, jsonRequest)

        val decodedRequest = json.decodeFromString<GeminiFlashRequest>(expectedJson)
        assertEquals(1, decodedRequest.contents.size)
        val content = decodedRequest.contents[0]
        assertEquals(1, content.parts.size)
        assertEquals("user", content.role)
        val part = content.parts[0]
        assertEquals("What is the capital of France?", part.text)
    }
}