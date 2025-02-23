package weather

import com.dsidak.Secrets
import com.dsidak.weather.Fetcher
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class FetcherUnitTest {
    @Test
    fun testToUrl_currentWeather() {
        assertEquals(
            "https://api.openweathermap.org/data/2.5/weather?q=London&appid=${Secrets.WEATHER_API_KEY}",
            Fetcher.toUrl("London", LocalDate.now())
        )
    }

    @Test
    fun testToUrl_forecast() {
        assertEquals(
            "https://api.openweathermap.org/data/2.5/forecast/daily?q=London&appid=${Secrets.WEATHER_API_KEY}&cnt=1",
            Fetcher.toUrl("London", LocalDate.now().plusDays(1))
        )
        assertEquals(
            "https://api.openweathermap.org/data/2.5/forecast/daily?q=London&appid=${Secrets.WEATHER_API_KEY}&cnt=6",
            Fetcher.toUrl("London", LocalDate.now().plusDays(6))
        )
    }
}