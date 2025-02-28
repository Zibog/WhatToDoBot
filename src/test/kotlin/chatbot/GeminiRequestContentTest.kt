package chatbot

import com.dsidak.chatbot.GeminiRequestContent
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class GeminiRequestContentTest {
    private val path = "src/test/resources"

    @Test
    fun testDecodeEncodeJson() {
        val resourceName = "Gemini_simple.json"
        val file = File("$path/$resourceName")
        val originalJson = file.readText()

        val requestContent = Json.decodeFromString<GeminiRequestContent>(originalJson)
        assertEquals(1, requestContent.contents.size)
        val content = requestContent.contents[0]
        assertEquals(1, content.parts.size)
        val part = content.parts[0]
        assertEquals("Explain how AI works", part.text)

        val jsonRequest = Json.encodeToString(requestContent)
        assertEquals(originalJson, jsonRequest)
    }
}