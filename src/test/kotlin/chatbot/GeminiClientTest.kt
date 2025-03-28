package chatbot

import base.HttpTestBase
import com.dsidak.chatbot.FinishReason
import com.dsidak.chatbot.GeminiClient
import com.dsidak.weather.*
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.File
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GeminiClientTest : HttpTestBase() {
    private val geminiClient = GeminiClient(httpClient)

    @Test
    fun testExecuteRequest() {
        val file = File("$resources/chatbot/GeminiResponse_current.json")
        mockResponse(file.readText())

        val geminiResponse = geminiClient.executeRequest(DEFAULT_REQUEST)

        assertTrue(geminiResponse.isRight())
        val response = geminiResponse.getOrNull()
        assertNotNull(response)
        assertEquals(1, response.candidates.size)
        assertEquals(FinishReason.STOP, response.candidates[0].finishReason)
    }

    @Test
    fun testGenerateContent_current() {
        val file = File("$resources/chatbot/GeminiResponse_current.json")
        mockResponse(file.readText())

        val geminiResponse = geminiClient.generateContent(mockCurrent(), LocalDate.now())
        assertEquals(
            "I recommend you to embrace the moodiness of a cloudy London day with a visit to the Viktor Wynd Museum of Curiosities.",
            geminiResponse
        )
    }

    @Test
    fun testGenerateContent_forecast() {
        val file = File("$resources/chatbot/GeminiResponse_forecast.json")
        mockResponse(file.readText())

        val geminiResponse = geminiClient.generateContent(mockForecast(), LocalDate.now().plusDays(2))
        assertEquals(
            "I recommend you to embrace the moodiness of London's cloudy weather on March 30th with a self-guided \"Literary London\" walking tour.",
            geminiResponse
        )
    }

    private fun mockCurrent(): WeatherResponse {
        return mockWeatherResponse(mock<CurrentWeatherResponse>())
    }

    private fun mockForecast(): WeatherResponse {
        return mockWeatherResponse(mock<ForecastWeatherResponse>())
    }

    private fun mockWeatherResponse(mock: WeatherResponse): WeatherResponse {
        whenever(mock.cityName).thenReturn("London")
        whenever(mock.country).thenReturn("GB")
        val weather = mockWeather()
        whenever(mock.weather).thenReturn(weather)
        val main = mockMain()
        whenever(mock.main).thenReturn(main)
        val wind = mockWind()
        whenever(mock.wind).thenReturn(wind)
        return mock
    }

    private fun mockWeather(): List<Weather> {
        val mock: Weather = mock()
        whenever(mock.description).thenReturn("Cloudy")
        return listOf(mock)
    }

    private fun mockMain(): Main {
        val mock: Main = mock()
        whenever(mock.temperature).thenReturn(10.0)
        whenever(mock.feelsLike).thenReturn(8.0)
        whenever(mock.humidity).thenReturn(80)
        return mock
    }

    private fun mockWind(): Wind {
        val mock: Wind = mock()
        whenever(mock.speed).thenReturn(5.0)
        return mock
    }
}