package chatbot

import com.dsidak.chatbot.Client
import com.dsidak.weather.*
import org.mockito.Mockito
import java.time.LocalDate
import kotlin.test.Test

class ClientTest {
    @Test
    fun testGenerateContent_current() {
        val client = Client()
        val geminiResponse = client.generateContent(mockCurrent(), LocalDate.now())
        assert(geminiResponse.isNotEmpty())
    }

    @Test
    fun testGenerateContent_forecast() {
        val client = Client()
        val geminiResponse = client.generateContent(mockForecast(), LocalDate.now().plusDays(2))
        assert(geminiResponse.isNotEmpty())
    }

    private fun mockCurrent(): WeatherResponse {
        return mockWeatherResponse(Mockito.mock(CurrentWeatherResponse::class.java))
    }

    private fun mockForecast(): WeatherResponse {
        return mockWeatherResponse(Mockito.mock(ForecastWeatherResponse::class.java))
    }

    private fun mockWeatherResponse(mock: WeatherResponse): WeatherResponse {
        Mockito.`when`(mock.cityName).thenReturn("London")
        Mockito.`when`(mock.country).thenReturn("GB")
        val weather = mockWeather()
        Mockito.`when`(mock.weather).thenReturn(weather)
        val main = mockMain()
        Mockito.`when`(mock.main).thenReturn(main)
        val wind = mockWind()
        Mockito.`when`(mock.wind).thenReturn(wind)
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