package weather

import base.JsonTestBase
import com.dsidak.weather.*
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class WeatherResponseTest : JsonTestBase {
    @Test
    fun testDecodeJson_current_city() {
        var file = File("$resources/weather/Newtonhill_GB_current.json")
        var weatherResponse: WeatherResponse = json.decodeFromString<CurrentWeatherResponse>(file.readText())

        checkCoordinates(-2.15, 57.0, weatherResponse.coordinates)
        assertEquals(1, weatherResponse.weather.size)
        checkWeather(804, "Clouds", "overcast clouds", "04d", weatherResponse.weather[0])
        checkMain(8.48, 4.9, 79, weatherResponse.main)
        checkWind(7.3, 189, 13.48, weatherResponse.wind)
        checkClouds(100, weatherResponse.clouds)
        assertEquals(1647347424, weatherResponse.dt)
        assertEquals("GB", weatherResponse.country)
        assertEquals(0, weatherResponse.timezone)
        assertEquals("Newtonhill", weatherResponse.cityName)

        file = File("$resources/weather/Sofia_BG_current.json")
        weatherResponse = json.decodeFromString<CurrentWeatherResponse>(file.readText())

        checkCoordinates(23.3242, 42.6975, weatherResponse.coordinates)
        assertEquals(1, weatherResponse.weather.size)
        checkWeather(804, "Clouds", "overcast clouds", "04n", weatherResponse.weather[0])
        checkMain(-1.57, -6.96, 68, weatherResponse.main)
        checkWind(5.14, 100, wind = weatherResponse.wind)
        checkClouds(100, weatherResponse.clouds)
        assertEquals(1740356208, weatherResponse.dt)
        assertEquals("BG", weatherResponse.country)
        assertEquals(7200, weatherResponse.timezone)
        assertEquals("Sofia", weatherResponse.cityName)
    }

    @Test
    fun testDecodeJson_forecast_city() {
        val file = File("$resources/weather/Munich_DE_forecast.json")
        val weatherResponse: WeatherResponse = json.decodeFromString<ForecastWeatherResponse>(file.readText())

        checkCoordinates(11.5755, 48.1374, weatherResponse.coordinates)
        assertEquals(1, weatherResponse.weather.size)
        checkWeather(800, "Clear", "clear sky", "01n", weatherResponse.weather[0])
        checkMain(2.38, 2.38, 79, weatherResponse.main)
        checkWind(1.33, 113, 1.36, weatherResponse.wind)
        checkClouds(8, weatherResponse.clouds)
        assertEquals(1741122000, weatherResponse.dt)
        assertEquals("DE", weatherResponse.country)
        assertEquals(3600, weatherResponse.timezone)
        assertEquals("Munich", weatherResponse.cityName)
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
        humidity: Int,
        main: Main
    ) {
        assertEquals(temperature, main.temperature)
        assertEquals(feelsLike, main.feelsLike)
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
}