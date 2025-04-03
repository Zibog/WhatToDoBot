package weather

import base.HttpTestBase
import com.dsidak.exception.RequestFailureException
import com.dsidak.weather.WeatherFetcher
import org.mockito.kotlin.whenever
import java.io.File
import java.net.UnknownHostException
import kotlin.test.Test
import kotlin.test.assertEquals

class WeatherFetcherTest : HttpTestBase() {
    private val weatherFetcher = WeatherFetcher(httpClient)

    @Test
    fun testExecuteRequest_currentWeather() {
        val file = File("$resources/weather/Sofia_BG_current.json")
        mockResponse(file.readText())

        val response = weatherFetcher.executeRequest(DEFAULT_REQUEST)
        assertEquals("Sofia", response.cityName)
        assertEquals("BG", response.country)
    }

    @Test
    fun testExecuteRequest_currentWeather_wrongCity() {
        mockResponse(code = 404, message = "Not Found")

        try {
            weatherFetcher.executeRequest(DEFAULT_REQUEST)
        } catch (e: RequestFailureException) {
            assertEquals("Request failed: 404 Not Found", e.message)
        }
    }

    @Test
    fun testExecuteRequest_noNetwork() {
        // Simulate a network error
        whenever(httpClient.newCall(DEFAULT_REQUEST)).thenReturn(call)
        whenever(httpClient.newCall(DEFAULT_REQUEST).execute()).thenThrow(UnknownHostException())
        val offlineWeatherFetcher = WeatherFetcher(httpClient)

        try {
            offlineWeatherFetcher.executeRequest(DEFAULT_REQUEST)
        } catch (e: RequestFailureException) {
            assertEquals("Failed to execute request due to: unknown error: can't connect to remote service", e.message)
        }
    }

    @Test
    fun testExecuteRequest_forecast() {
        val file = File("$resources/weather/Munich_DE_forecast.json")
        mockResponse(file.readText())

        val response = weatherFetcher.executeRequest(DEFAULT_REQUEST)
        assertEquals("Munich", response.cityName)
        assertEquals("DE", response.country)
    }
}