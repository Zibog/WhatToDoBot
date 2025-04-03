package bot

import base.HttpTestBase
import com.dsidak.bot.CommandHandler
import com.dsidak.chatbot.GeminiClient
import com.dsidak.db.DatabaseManager
import com.dsidak.db.schemas.Location
import com.dsidak.geocoding.GeocodingFetcher
import com.dsidak.weather.WeatherFetcher
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.jupiter.api.assertDoesNotThrow
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.telegram.telegrambots.abilitybots.api.objects.MessageContext
import org.telegram.telegrambots.meta.api.objects.User
import kotlin.random.Random
import kotlin.test.Test

class CommandHandlerTest : HttpTestBase() {
    private var handler: CommandHandler
    private val geminiHttpClient: OkHttpClient = mock()
    private val weatherHttpClient: OkHttpClient = mock()
    private val geocodingFetcher: GeocodingFetcher = mock()

    init {
        val weatherFetcher = WeatherFetcher(weatherHttpClient)
        val geminiClient = GeminiClient(geminiHttpClient)
        handler = CommandHandler(weatherFetcher, geocodingFetcher, geminiClient)
    }

    @Test
    fun testLocation_presentInDb() {
        val ctx: MessageContext = mock()
        val user = User.builder()
            .id(Random.nextLong())
            .firstName("test")
            .userName("test")
            .isBot(false)
            .build()
        whenever(ctx.user()).thenReturn(user)
        whenever(ctx.arguments()).thenReturn(arrayOf("Sofia", "BG"))

        val location = Location("Sofia", "BG", 42.6977, 23.3068)

        whenever(
            geocodingFetcher.fetchCoordinates(
                any(),
                any()
            )
        ).thenThrow(IllegalArgumentException("Should not be called"))

        runBlocking {
            DatabaseManager.locationService.create(location)
            assertDoesNotThrow { handler.handleLocationCommand(ctx) }
        }
    }
}