package geocoding

import com.dsidak.dotenv
import com.dsidak.geocoding.Geocoding
import kotlin.test.Test
import kotlin.test.assertEquals

class GeocodingUnitTest {
    @Test
    fun testToUrl_onlyCity_defaultLimit() {
        assertEquals(
            "https://api.openweathermap.org/geo/1.0/direct?q=London&limit=1&appid=${dotenv["WEATHER_API_KEY"]}",
            Geocoding.toUrl("London").toString(),
            "City without country with default limit"
        )
    }

    @Test
    fun testToUrl_onlyCity_withLimit() {
        assertEquals(
            "https://api.openweathermap.org/geo/1.0/direct?q=London&limit=5&appid=${dotenv["WEATHER_API_KEY"]}",
            Geocoding.toUrl("London", 5).toString(),
            "City without country with specified limit"
        )
    }
}