package geocoding

import com.dsidak.geocoding.Geocoding
import okhttp3.Request
import java.nio.charset.StandardCharsets
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class GeocodingTest {
    private val geocoding = Geocoding()

    @Test
    fun testExecuteRequest_onlyCity() {
        val url = Geocoding.toUrl("Sofia")
        val request = Request.Builder()
            .url(url)
            .header("charset", StandardCharsets.UTF_8.name())
            .get()
            .build()

        val response = geocoding.executeRequest(request)

        assert(response.isRight())
        val geoResponse = response.getOrNull()
        assertNotNull(geoResponse)
        val firstCity = geoResponse[0]
        assertEquals("Sofia", firstCity.name)
        assertEquals(42.6977028, firstCity.latitude)
        assertEquals(23.3217359, firstCity.longitude)
        assertEquals("BG", firstCity.country)
    }

    @Test
    fun testExecuteRequest_withCountry() {
        val url = Geocoding.toUrl("Sofia", "BG")
        val request = Request.Builder()
            .url(url)
            .header("charset", StandardCharsets.UTF_8.name())
            .get()
            .build()

        val response = geocoding.executeRequest(request)

        assert(response.isRight())
        val geoResponse = response.getOrNull()
        assertNotNull(geoResponse)
        val firstCity = geoResponse[0]
        assertEquals("Sofia", firstCity.name)
        assertEquals(42.6977028, firstCity.latitude)
        assertEquals(23.3217359, firstCity.longitude)
        assertEquals("BG", firstCity.country)
    }

    @Test
    fun testExecuteRequest_wrongCity() {
        val url = Geocoding.toUrl("NonexistentCity")
        val request = Request.Builder()
            .url(url)
            .header("charset", StandardCharsets.UTF_8.name())
            .get()
            .build()

        val response = geocoding.executeRequest(request)

        assert(response.isLeft())
        assertEquals("No results found for the given city", response.leftOrNull())
    }
}