package geocoding

import base.HttpTestBase
import com.dsidak.geocoding.GeocodingFetcher
import okhttp3.Request
import java.io.File
import java.nio.charset.StandardCharsets
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GeocodingFetcherTest : HttpTestBase() {
    private val geocodingFetcher = GeocodingFetcher(httpClient)

    @Test
    fun testExecuteRequest_onlyCity() {
        val file = File("$resources/geocoding/GeoResponse_Sofia.json")
        mockResponse(file.readText())

        val response = geocodingFetcher.executeRequest(DEFAULT_REQUEST)
        val firstCity = response[0]
        assertEquals("Sofia", firstCity.name)
        assertEquals(42.6977028, firstCity.latitude)
        assertEquals(23.3217359, firstCity.longitude)
        assertEquals("BG", firstCity.country)
    }

    @Test
    fun testExecuteRequest_withCountry() {
        val file = File("$resources/geocoding/GeoResponse_Sofia_BG.json")
        mockResponse(file.readText())

        val response = geocodingFetcher.executeRequest(DEFAULT_REQUEST)
        val firstCity = response[0]
        assertEquals("Sofia", firstCity.name)
        assertEquals(42.6977028, firstCity.latitude)
        assertEquals(23.3217359, firstCity.longitude)
        assertEquals("BG", firstCity.country)
    }

    @Test
    fun testExecuteRequest_wrongCity() {
        val url = GeocodingFetcher.toUrl("NonexistentCity")
        val request = Request.Builder()
            .url(url)
            .header("charset", StandardCharsets.UTF_8.name())
            .get()
            .build()

        mockResponse()

        val response = geocodingFetcher.executeRequest(request)
        assertTrue(response.isEmpty())
    }
}