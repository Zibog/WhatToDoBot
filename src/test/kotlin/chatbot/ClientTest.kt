package chatbot

import com.dsidak.chatbot.Client
import com.dsidak.weather.*
import org.mockito.Mockito
import java.time.LocalDate
import kotlin.test.Test

class ClientTest {
    @Test
    fun testGenerateContent() {
        val client = Client()
        val geminiResponse = client.generateContent(mockWeatherResponse(), LocalDate.now())
        assert(geminiResponse.isNotEmpty())
    }

    private fun mockWeatherResponse(): WeatherResponse {
        val mock = Mockito.mock(WeatherResponse::class.java)
        Mockito.`when`(mock.cityName).thenReturn("London")
        val sys = mockSys()
        Mockito.`when`(mock.sys).thenReturn(sys)
        val weather = mockWeather()
        Mockito.`when`(mock.weather).thenReturn(weather)
        val main = mockMain()
        Mockito.`when`(mock.main).thenReturn(main)
        val wind = mockWind()
        Mockito.`when`(mock.wind).thenReturn(wind)
        return mock
    }

    private fun mockSys(): Sys {
        val mock = Mockito.mock(Sys::class.java)
        Mockito.`when`(mock.country).thenReturn("GB")

        return mock
    }

    private fun mockWeather(): List<Weather> {
        val mock = Mockito.mock(Weather::class.java)
        Mockito.`when`(mock.description).thenReturn("Cloudy")
        return listOf(mock)
    }

    private fun mockMain(): Main {
        val mock = Mockito.mock(Main::class.java)
        Mockito.`when`(mock.temperature).thenReturn(10.0)
        Mockito.`when`(mock.feelsLike).thenReturn(8.0)
        Mockito.`when`(mock.humidity).thenReturn(80)
        return mock
    }

    private fun mockWind(): Wind {
        val mock = Mockito.mock(Wind::class.java)
        Mockito.`when`(mock.speed).thenReturn(5.0)
        return mock
    }
}