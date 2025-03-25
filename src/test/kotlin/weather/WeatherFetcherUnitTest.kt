package weather

import com.dsidak.dotenv
import com.dsidak.weather.WeatherFetcher
import org.junit.jupiter.api.assertThrows
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class WeatherFetcherUnitTest {
    @Test
    fun testToUrl_currentWeather_city() {
        assertEquals(
            "https://api.openweathermap.org/data/2.5/weather?appid=${dotenv["WEATHER_API_KEY"]}&units=metric&q=London",
            WeatherFetcher.toUrl("London", LocalDate.now()).toString(),
            "Current weather by city"
        )
    }

    @Test
    fun testToUrl_currentWeather_coordinates() {
        assertEquals(
            "https://api.openweathermap.org/data/2.5/weather?appid=${dotenv["WEATHER_API_KEY"]}&units=metric&lat=12.34&lon=43.21",
            WeatherFetcher.toUrl(12.34, 43.21, LocalDate.now()).toString(),
            "Current weather by coordinates"
        )
    }

    @Test
    fun testToUrl_forecast_city() {
        assertEquals(
            "https://api.openweathermap.org/data/2.5/forecast?appid=${dotenv["WEATHER_API_KEY"]}&units=metric&cnt=1&q=${
                URLEncoder.encode(
                    "München", StandardCharsets.UTF_8
                )
            }",
            WeatherFetcher.toUrl("München", LocalDate.now().plusDays(1)).toString(),
            "Forecast 1 day by city with encoding"
        )
        assertEquals(
            "https://api.openweathermap.org/data/2.5/forecast?appid=${dotenv["WEATHER_API_KEY"]}&units=metric&cnt=4&q=London",
            WeatherFetcher.toUrl("London", LocalDate.now().plusDays(4)).toString(),
            "Forecast 4 days by city"
        )
    }

    @Test
    fun testToUrl_forecast_coordinates() {
        assertEquals(
            "https://api.openweathermap.org/data/2.5/forecast?appid=${dotenv["WEATHER_API_KEY"]}&units=metric&cnt=3&q=London",
            WeatherFetcher.toUrl("London", LocalDate.now().plusDays(3)).toString(),
            "Forecast 3 days by coordinates"
        )
    }

    @Test
    fun testToUrl_wrongDate() {
        assertThrows<IllegalArgumentException>(
            "Less than Config.lowerBound",
        ) {
            WeatherFetcher.toUrl("London", LocalDate.now().minusDays(1))
        }

        assertThrows<IllegalArgumentException>(
            "More than Config.upperBound",
        ) {
            WeatherFetcher.toUrl("London", LocalDate.now().plusDays(66))
        }
    }
}