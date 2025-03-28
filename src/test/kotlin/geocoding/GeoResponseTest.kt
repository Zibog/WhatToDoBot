package geocoding

import base.JsonTestBase
import com.dsidak.geocoding.CityInfo
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class GeoResponseTest : JsonTestBase {
    @Test
    fun testDecodeJson_example() {
        val file = File("$resources/geocoding/GeoResponse_example.json")
        val geoResponse: List<CityInfo> = json.decodeFromString<List<CityInfo>>(file.readText())

        assertEquals(5, geoResponse.size)
        checkCityInfo("London", true, 51.5073219, -0.1276474, "GB", "England", geoResponse[0])
        checkCityInfo("City of London", true, 51.5156177, -0.0919983, "GB", "England", geoResponse[1])
        checkCityInfo("London", true, 42.9832406, -81.243372, "CA", "Ontario", geoResponse[2])
        checkCityInfo("Chelsea", true, 51.4875167, -0.1687007, "GB", "England", geoResponse[3])
        checkCityInfo("London", false, 37.1289771, -84.0832646, "US", "Kentucky", geoResponse[4])
    }

    private fun checkCityInfo(
        name: String,
        withLocalNames: Boolean,
        latitude: Double,
        longitude: Double,
        country: String,
        state: String,
        cityInfo: CityInfo
    ) {
        assertEquals(name, cityInfo.name)
        assertEquals(withLocalNames, cityInfo.localNames.isNotEmpty())
        assertEquals(latitude, cityInfo.latitude)
        assertEquals(longitude, cityInfo.longitude)
        assertEquals(country, cityInfo.country)
        assertEquals(state, cityInfo.state)
    }
}