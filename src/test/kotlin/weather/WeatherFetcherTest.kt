package weather

import base.HttpTestBase
import com.dsidak.weather.WeatherFetcher
import org.mockito.kotlin.whenever
import java.io.File
import java.net.UnknownHostException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class WeatherFetcherTest : HttpTestBase() {
    private val weatherFetcher = WeatherFetcher(httpClient)

    @Test
    fun testExecuteRequest_currentWeather() {
        val file = File("$resources/weather/Sofia_BG_current.json")
        mockResponse(file.readText())

        val response = weatherFetcher.executeRequest(DEFAULT_REQUEST)

        assert(response.isRight())
        val weatherResponse = response.getOrNull()
        assertNotNull(weatherResponse)
        assertEquals("Sofia", weatherResponse.cityName)
        assertEquals("BG", weatherResponse.country)
    }

    @Test
    fun testExecuteRequest_currentWeather_wrongCity() {
        mockResponse(code = 404, message = "Not Found")

        val response = weatherFetcher.executeRequest(DEFAULT_REQUEST)

        assert(response.isLeft())
        assertEquals("Request failed: 404 Not Found", response.leftOrNull())
    }

    @Test
    fun testExecuteRequest_noNetwork() {
        // Simulate a network error
        whenever(httpClient.newCall(DEFAULT_REQUEST)).thenReturn(call)
        whenever(httpClient.newCall(DEFAULT_REQUEST).execute()).thenThrow(UnknownHostException())
        val offlineWeatherFetcher = WeatherFetcher(httpClient)

        val response = offlineWeatherFetcher.executeRequest(DEFAULT_REQUEST)

        assert(response.isLeft())
        assertEquals(
            "Failed to execute request due to: unknown error: can't connect to remote service",
            response.leftOrNull()
        )
    }

    @Test
    fun testExecuteRequest_forecast() {
        val file = File("$resources/weather/Munich_DE_forecast.json")
        mockResponse(file.readText())

        val response = weatherFetcher.executeRequest(DEFAULT_REQUEST)

        assert(response.isRight())
        val weatherResponse = response.getOrNull()
        assertNotNull(weatherResponse)
        assertEquals("Munich", weatherResponse.cityName)
        assertEquals("DE", weatherResponse.country)
    }
}