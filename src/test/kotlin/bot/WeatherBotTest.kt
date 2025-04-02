package bot

import base.HttpTestBase
import com.dsidak.bot.CommandHandler
import com.dsidak.bot.WeatherBot
import com.dsidak.chatbot.GeminiClient
import com.dsidak.configuration.config
import com.dsidak.geocoding.GeocodingFetcher
import com.dsidak.weather.WeatherFetcher
import okhttp3.OkHttpClient
import org.junit.jupiter.api.Disabled
import org.mockito.kotlin.*
import org.telegram.telegrambots.abilitybots.api.sender.SilentSender
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.objects.message.Message
import java.io.File
import kotlin.random.Random
import kotlin.test.Test

class WeatherBotTest : HttpTestBase() {
    // Bot to test
    private var bot: WeatherBot
    // Sender to mock
    private val sender: SilentSender = mock()
    private var user: User = User.builder()
        .id(Random.nextLong())
        .firstName("fname")
        .lastName("lname")
        .userName("uname")
        .isBot(false)
        .build()
    private val geminiHttpClient: OkHttpClient = mock()
    private val weatherHttpClient: OkHttpClient = mock()
    private val geoHttpClient: OkHttpClient = mock()

    init {
        // Create bot with mocked http client
        val weatherFetcher = WeatherFetcher(weatherHttpClient)
        val geocodingFetcher = GeocodingFetcher(geoHttpClient)
        val geminiClient = GeminiClient(geminiHttpClient)
        bot = WeatherBot(
            OkHttpTelegramClient(TOKEN),
            BOT_USERNAME + Random.nextInt(),
            CommandHandler(weatherFetcher, geocodingFetcher, geminiClient)
        )
        // Call onRegister() to initialize abilities etc.
        bot.onRegister()
        // Set your bot silent sender to the mocked sender
        bot.setSilentSender(sender)
    }

    @Test
    fun testWeather() {
        var file = File("$resources/geocoding/GeoResponse_Sofia.json")
        mockResponse(file.readText(), httpClient = geoHttpClient)
        val update = mockFullUpdate("/weather today")
        bot.consume(update)
        verify(sender, times(1)).sendMd(
            "Please, provide your location first using /location <city>, [country]",
            user.id
        )

        val updateLocation = mockFullUpdate("/location Sofia")
        bot.consume(updateLocation)
        verify(
            sender,
            times(1)
        ).sendMd(
            "Location set to Sofia, BG. If location is wrong, set it using `/location <city>, <country>`",
            user.id
        )

        file = File("$resources/weather/Sofia_BG_current.json")
        mockResponse(file.readText(), httpClient = weatherHttpClient)
        file = File("$resources/chatbot/GeminiResponse_current.json")
        mockResponse(file.readText(), httpClient = geminiHttpClient)
        // Try to ask again with city set
        bot.consume(update)
        verify(sender, times(1)).sendMd(
            argThat { contains("the weather is") and contains("I recommend you to") },
            eq(user.id)
        )
    }

    @Test
    fun testWeatherCommand_defaultValue() {
        var file = File("$resources/geocoding/GeoResponse_Sofia.json")
        mockResponse(file.readText(), httpClient = geoHttpClient)
        val updateLocation = mockFullUpdate("/location Sofia")
        bot.consume(updateLocation)

        file = File("$resources/weather/Sofia_BG_current.json")
        mockResponse(file.readText(), httpClient = weatherHttpClient)
        file = File("$resources/chatbot/GeminiResponse_current.json")
        mockResponse(file.readText(), httpClient = geminiHttpClient)
        val update = mockFullUpdate("/weather")
        bot.consume(update)
        verify(sender, times(1)).sendMd(
            argThat { contains("the weather is") and contains("I recommend you to") },
            eq(user.id)
        )
    }

    @Test
    fun testWeatherCommand_invalidArgument() {
        val invalidArgUpdate = mockFullUpdate("/weather yesterday")
        bot.consume(invalidArgUpdate)
        verify(sender, times(1)).sendMd("Invalid argument 'yesterday'. Please, provide a valid offset", user.id)

        val invalidArgUpdate2 = mockFullUpdate("/weather 666")
        bot.consume(invalidArgUpdate2)
        verify(sender, times(1)).sendMd("Invalid argument '666'. Please, provide a valid offset", user.id)

        val invalidArgUpdate3 = mockFullUpdate("/weather -1")
        bot.consume(invalidArgUpdate3)
        verify(sender, times(1)).sendMd("Invalid argument '-1'. Please, provide a valid offset", user.id)

        val outOfBound = config.upperBound + 1
        val invalidArgUpdate4 = mockFullUpdate("/weather $outOfBound")
        bot.consume(invalidArgUpdate4)
        verify(sender, times(1)).sendMd("Invalid argument '$outOfBound'. Please, provide a valid offset", user.id)

        val multipleArgsUpdate = mockFullUpdate("/weather 9 9 9")
        bot.consume(multipleArgsUpdate)
        verify(sender, times(1)).sendMd("Invalid argument '9'. Please, provide a valid offset", user.id)
    }

    @Test
    fun testLocationCommand_setThenUpdateLocation() {
        var file = File("$resources/geocoding/GeoResponse_Sofia.json")
        mockResponse(file.readText(), httpClient = geoHttpClient)
        val update = mockFullUpdate("/location Sofia")
        bot.consume(update)
        verify(
            sender,
            times(1)
        ).sendMd(
            "Location set to Sofia, BG. If location is wrong, set it using `/location <city>, <country>`",
            user.id
        )

        file = File("$resources/geocoding/GeoResponse_Plovdiv.json")
        mockResponse(file.readText(), httpClient = geoHttpClient)
        val update2 = mockFullUpdate("/location Plovdiv")
        bot.consume(update2)
        verify(
            sender,
            times(1)
        ).sendMd(
            "Location updated from Sofia, BG to Plovdiv, BG. If location is wrong, set it using `/location <city>, <country>`",
            user.id
        )

        file = File("$resources/geocoding/GeoResponse_Tbilisi.json")
        mockResponse(file.readText(), httpClient = geoHttpClient)
        val update3 = mockFullUpdate("/location Tbilisi")
        bot.consume(update3)
        verify(
            sender,
            times(1)
        ).sendMd(
            "Location updated from Plovdiv, BG to Tbilisi, GE. If location is wrong, set it using `/location <city>, <country>`",
            user.id
        )
    }

    @Test
    fun testLocationCommand_setCityWithCountry() {
        val file = File("$resources/geocoding/GeoResponse_Sofia_BG.json")
        mockResponse(file.readText(), httpClient = geoHttpClient)
        val update = mockFullUpdate("/location Sofia, BG")
        bot.consume(update)
        verify(
            sender,
            times(1)
        ).sendMd(
            "Location set to Sofia, BG. If location is wrong, set it using `/location <city>, <country>`",
            user.id
        )

        val zeroArgUpdate = mockFullUpdate("/location")
        bot.consume(zeroArgUpdate)
        verify(sender, times(1)).sendMd("Your current location is Sofia, BG", user.id)
    }

    @Test
    fun testLocationCommand_noLocationSet() {
        val zeroArgUpdate = mockFullUpdate("/location")
        bot.consume(zeroArgUpdate)
        verify(sender, times(1)).sendMd("No location was set", user.id)
    }

    @Test
    fun testLocationCommand_wrongArgumentsNumber() {
        val multipleArgsUpdate = mockFullUpdate("/location Tut Tam Sam")
        bot.consume(multipleArgsUpdate)
        verify(sender, times(1)).sendMd("Sorry, this feature requires 1 or 2 additional inputs", user.id)
    }

    @Test
    fun testLocationCommand_nonexistentCity() {
        mockResponse(httpClient = geoHttpClient)
        val update = mockFullUpdate("/location NonexistentCity")
        bot.consume(update)
        verify(sender, times(1)).sendMd(
            "No results found for the city NonexistentCity. Try to specify the country",
            user.id
        )
    }

    @Test
    fun testRestartCommand() {
        val restartUpdate = mockFullUpdate("/restart")
        bot.consume(restartUpdate)
        verify(sender, times(1)).sendMd("No location was set", user.id)

        val file = File("$resources/geocoding/GeoResponse_Sofia.json")
        mockResponse(file.readText(), httpClient = geoHttpClient)
        val updateLocation = mockFullUpdate("/location Sofia")
        bot.consume(updateLocation)
        val restartUpdate2 = mockFullUpdate("/restart")
        bot.consume(restartUpdate2)
        verify(sender, times(1)).sendMd("Location dropped from Sofia", user.id)

        val restartUpdate3 = mockFullUpdate("/restart")
        bot.consume(restartUpdate3)
        verify(sender, times(1)).sendMd("No location was set", user.id)
    }

    @Test
    fun testHelpCommand() {
        val update = mockFullUpdate("/help")
        bot.consume(update)
        verify(sender, times(1)).sendMd(
            """
            |How to use this bot?
            |
            |1. Set your location using `/location <city>, [country]`
            |This command is also used to update location.
            |The country is optional and should be a two-letter-length code.
            |Examples: `/location Sofia`, `/location Moscow`, `/location London, GB`
            |You can use this command without arguments to check your current location.
            |Example: `/location`
            |2. Request weather for the day using `/weather [offset]`
            |The offset can be a number from 0 to 5, *today* or *tomorrow*, where *today* is the default value.
            |The offset is the number of days from today, where 0 is today, 1 is tomorrow, etc.
            |Examples: `/weather 0`, `/weather today`, `/weather tomorrow`, `/weather 3`
            |
            |Optional commands (no arguments needed):
            |/restart drops your location
            |/help shows this instruction :)
            |/commands lists you with supported commands with short descriptions
            """.trimMargin(), user.id
        )
    }

    @Test
    fun testDefault_anyText() {
        var update = mockFullUpdate("Hello")
        bot.consume(update)
        val message = """This bot works only with commands. To check them, use /commands or /help"""
        verify(sender, times(1)).sendMd(message, user.id)

        update = mockFullUpdate("Hello, World!")
        bot.consume(update)
        verify(sender, times(2)).sendMd(message, user.id)
    }

    @Disabled("Can't capture arbitrary commands due to AbilityBot limitations")
    @Test
    fun testDefault_unknownCommand() {
        val update = mockFullUpdate("/unknown")
        bot.consume(update)
        val message = """This bot works only with commands. To check them, use /commands or /help"""
        verify(sender, times(1)).sendMd(message, user.id)
    }

    private fun mockFullUpdate(args: String, fromUser: User = user): Update {
        bot.users()[user.id] = user
        bot.users()[CREATOR.id] = CREATOR
        bot.userIds()[user.userName] = user.id
        bot.userIds()[CREATOR.userName] = CREATOR.id

        bot.admins().add(CREATOR.id)

        val user: User = mockUser(fromUser)

        val update: Update = mock()
        whenever(update.hasMessage()).thenReturn(true)
        val message: Message = mock()
        whenever(message.from).thenReturn(user)
        whenever(message.text).thenReturn(args)
        whenever(message.hasText()).thenReturn(true)
        whenever(message.isUserMessage).thenReturn(true)
        whenever(message.chatId).thenReturn(fromUser.id)
        whenever(update.message).thenReturn(message)
        return update
    }

    private fun mockUser(fromUser: User): User {
        val user: User = mock()
        whenever(user.id).thenReturn(fromUser.id)
        whenever(user.userName).thenReturn(fromUser.userName)
        whenever(user.firstName).thenReturn(fromUser.firstName)
        whenever(user.lastName).thenReturn(fromUser.lastName)

        return user
    }

    companion object {
        // Bot-specific constants
        private const val TOKEN = "TOKEN"
        private const val BOT_USERNAME = "TestBot"

        private val CREATOR =
            User.builder().id(777).firstName("creatorFirst").lastName("creatorLast").userName("creatorUsername")
                .isBot(false).build()
    }
}