package chatbot

import base.JsonTestBase
import com.dsidak.chatbot.FinishReason
import com.dsidak.chatbot.GeminiFlashResponse
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class GeminiFlashResponseTest : JsonTestBase {
    @Test
    fun testDecodeEncodeJson_Paris() {
        val file = File("$resources/GeminiResponse_Paris.json")
        val response = json.decodeFromString<GeminiFlashResponse>(file.readText())

        assertEquals(1, response.candidates.size)
        val candidate = response.candidates[0]
        val content = candidate.content
        assertEquals(1, content.parts.size)
        assertEquals("model", content.role)
        val part = content.parts[0]
        assertEquals("The capital of France is Paris.", part.text)
        val finishReason = candidate.finishReason
        assertEquals(FinishReason.STOP.name, finishReason.name)
    }
}