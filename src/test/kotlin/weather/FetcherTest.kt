package weather

import com.dsidak.weather.Fetcher
import okhttp3.Request
import org.junit.jupiter.api.Disabled
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import kotlin.test.Test

class FetcherTest {
    private val city = "Sofia"

    @Disabled
    @Test
    fun testExecuteRequest_currentWeather() {
        val date = LocalDate.now()
        val url = Fetcher.toUrl(city, date)

        val request = Request.Builder()
            .url(url)
            .header("charset", StandardCharsets.UTF_8.name())
            .get()
            .build()

        val response = Fetcher.executeRequest(request)
    }
}