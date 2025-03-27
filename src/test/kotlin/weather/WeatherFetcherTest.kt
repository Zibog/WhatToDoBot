package weather

import base.HttpTestBase
import com.dsidak.weather.WeatherFetcher
import okhttp3.Request
import org.mockito.Mockito.`when`
import java.io.File
import java.net.UnknownHostException
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class WeatherFetcherTest : HttpTestBase() {
    private val weatherFetcher = WeatherFetcher(httpClient)

    @Test
    fun testExecuteRequest_currentWeather() {
        val date = LocalDate.now()
        val url = WeatherFetcher.toUrl("Sofia", date)

        val request = Request.Builder()
            .url(url)
            .header("charset", StandardCharsets.UTF_8.name())
            .get()
            .build()

        val file = File("$resources/weather/Sofia_BG_current.json")
        mockResponse(request, file.readText())

        val response = weatherFetcher.executeRequest(request)

        assert(response.isRight())
        val weatherResponse = response.getOrNull()
        assertNotNull(weatherResponse)
        assertEquals("Sofia", weatherResponse.cityName)
        assertEquals("BG", weatherResponse.country)
    }

    @Test
    fun testExecuteRequest_currentWeather_wrongCity() {
        val date = LocalDate.now()
        val url = WeatherFetcher.toUrl("Diaspar", date)

        val request = Request.Builder()
            .url(url)
            .header("charset", StandardCharsets.UTF_8.name())
            .get()
            .build()

        mockResponse(request, code = 404, message = "Not Found")

        val response = weatherFetcher.executeRequest(request)

        assert(response.isLeft())
        assertEquals("Request failed: 404 Not Found", response.leftOrNull())
    }

    @Test
    fun testExecuteRequest_noNetwork() {
        val date = LocalDate.now()
        val url = WeatherFetcher.toUrl("Sofia", date)

        val request = Request.Builder()
            .url(url)
            .header("charset", StandardCharsets.UTF_8.name())
            .get()
            .build()

        // Simulate a network error
        `when`(httpClient.newCall(request)).thenReturn(call)
        `when`(httpClient.newCall(request).execute()).thenThrow(UnknownHostException())
        val offlineWeatherFetcher = WeatherFetcher(httpClient)

        val response = offlineWeatherFetcher.executeRequest(request)

        assert(response.isLeft())
        assertEquals(
            "Failed to execute request due to: unknown error: can't connect to remote service",
            response.leftOrNull()
        )
    }

    @Test
    fun testExecuteRequest_forecast() {
        val date = LocalDate.now().plusDays(1)
        val url = WeatherFetcher.toUrl("München", date)

        val request = Request.Builder()
            .url(url)
            .header("charset", StandardCharsets.UTF_8.name())
            .get()
            .build()

        val file = File("$resources/weather/Munich_DE_forecast.json")
        mockResponse(request, file.readText())

        val response = weatherFetcher.executeRequest(request)

        assert(response.isRight())
        val weatherResponse = response.getOrNull()
        assertNotNull(weatherResponse)
        assertEquals("Munich", weatherResponse.cityName)
        assertEquals("DE", weatherResponse.country)
    }
}