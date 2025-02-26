package weather

import com.dsidak.weather.Fetcher
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.net.UnknownHostException
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class FetcherTest {
    private val fetcher = Fetcher()

    @Test
    fun testExecuteRequest_currentWeather() {
        val date = LocalDate.now()
        val url = Fetcher.toUrl("Sofia", date)

        val request = Request.Builder()
            .url(url)
            .header("charset", StandardCharsets.UTF_8.name())
            .get()
            .build()

        val response = fetcher.executeRequest(request)

        assert(response.isRight())
        val weatherResponse = response.getOrNull()
        assertNotNull(weatherResponse)
        assertEquals(200, weatherResponse.code)
        assertEquals("Sofia", weatherResponse.cityName)
        assertEquals("BG", weatherResponse.sys.country)
    }

    @Test
    fun testExecuteRequest_currentWeather_wrongCity() {
        val date = LocalDate.now()
        val url = Fetcher.toUrl("Diaspar", date)

        val request = Request.Builder()
            .url(url)
            .header("charset", StandardCharsets.UTF_8.name())
            .get()
            .build()

        val response = fetcher.executeRequest(request)

        assert(response.isLeft())
        assertEquals("Request failed: 404 Not Found", response.leftOrNull())
    }

    @Test
    fun testExecuteRequest_noNetwork() {
        val date = LocalDate.now()
        val url = Fetcher.toUrl("Sofia", date)

        val request = Request.Builder()
            .url(url)
            .header("charset", StandardCharsets.UTF_8.name())
            .get()
            .build()

        // Simulate a network error
        val mockedClient = mock(OkHttpClient::class.java)
        `when`(mockedClient.newCall(request)).thenReturn(mock(Call::class.java))
        `when`(mockedClient.newCall(request).execute()).thenThrow(UnknownHostException())
        val offlineFetcher = Fetcher(mockedClient)

        val response = offlineFetcher.executeRequest(request)

        assert(response.isLeft())
        assertEquals("Failed to execute request: can't connect to remote service", response.leftOrNull())
    }
}