package weather

import com.dsidak.weather.*
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class WeatherResponseTest {
    private val path = "src/test/resources"

    @Test
    fun testDecodeJson_current_city() {
        var resourceName = "Newtonhill_GB_current.json"
        var file = File("$path/$resourceName")

        var weatherResponse = Json.decodeFromString<WeatherResponse>(file.readText())
        checkCoordinates(-2.15, 57.0, weatherResponse.coordinates)
        assertEquals(1, weatherResponse.weather.size)
        checkWeather(804, "Clouds", "overcast clouds", "04d", weatherResponse.weather[0])
        assertEquals("stations", weatherResponse.base)
        checkMain(8.48, 4.9, 8.18, 9.26, 1016, 79, weatherResponse.main)
        assertEquals(10000, weatherResponse.visibility)
        checkWind(7.3, 189, 13.48, weatherResponse.wind)
        checkClouds(100, weatherResponse.clouds)
        assertEquals(1647347424, weatherResponse.dt)
        checkSys(2, 2031790, "GB", 1647325488, 1647367827, weatherResponse.sys)
        assertEquals(0, weatherResponse.timezone)
        assertEquals(2641549, weatherResponse.id)
        assertEquals("Newtonhill", weatherResponse.name)
        assertEquals(200, weatherResponse.code)

        resourceName = "Sofia_BG_current.json"
        file = File("$path/$resourceName")

        weatherResponse = Json.decodeFromString<WeatherResponse>(file.readText())
        checkCoordinates(23.3242, 42.6975, weatherResponse.coordinates)
        assertEquals(1, weatherResponse.weather.size)
        checkWeather(804, "Clouds", "overcast clouds", "04n", weatherResponse.weather[0])
        assertEquals("stations", weatherResponse.base)
        checkMain(-1.57, -6.96, -2.17, -1.57, 1033, 68, weatherResponse.main)
        assertEquals(10000, weatherResponse.visibility)
        checkWind(5.14, 100, wind = weatherResponse.wind)
        checkClouds(100, weatherResponse.clouds)
        assertEquals(1740356208, weatherResponse.dt)
        checkSys(2, 2033225, "BG", 1740373841, 1740413370, weatherResponse.sys)
        assertEquals(7200, weatherResponse.timezone)
        assertEquals(727011, weatherResponse.id)
        assertEquals("Sofia", weatherResponse.name)
        assertEquals(200, weatherResponse.code)
    }

    private fun checkCoordinates(longitude: Double, latitude: Double, coordinates: Coordinates) {
        assertEquals(longitude, coordinates.longitude)
        assertEquals(latitude, coordinates.latitude)
    }

    @Suppress("SameParameterValue")
    private fun checkWeather(id: Int, main: String, description: String, icon: String, weather: Weather) {
        assertEquals(id, weather.id)
        assertEquals(main, weather.main)
        assertEquals(description, weather.description)
        assertEquals(icon, weather.icon)
    }

    private fun checkMain(
        temperature: Double,
        feelsLike: Double,
        tempMin: Double,
        tempMax: Double,
        pressure: Int,
        humidity: Int,
        main: Main
    ) {
        assertEquals(temperature, main.temperature)
        assertEquals(feelsLike, main.feelsLike)
        assertEquals(tempMin, main.tempMin)
        assertEquals(tempMax, main.tempMax)
        assertEquals(pressure, main.pressure)
        assertEquals(humidity, main.humidity)
    }

    private fun checkWind(speed: Double, deg: Int, gust: Double? = null, wind: Wind) {
        assertEquals(speed, wind.speed)
        assertEquals(deg, wind.degree)
        assertEquals(gust, wind.gust)
    }

    @Suppress("SameParameterValue")
    private fun checkClouds(all: Int, clouds: Clouds) {
        assertEquals(all, clouds.all)
    }

    @Suppress("SameParameterValue")
    private fun checkSys(type: Int, id: Int, country: String, sunrise: Long, sunset: Long, sys: Sys) {
        assertEquals(type, sys.type)
        assertEquals(id, sys.id)
        assertEquals(country, sys.country)
        assertEquals(sunrise, sys.sunrise)
        assertEquals(sunset, sys.sunset)
    }
}