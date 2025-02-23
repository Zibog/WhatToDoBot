package bot

import com.dsidak.bot.BotProperties
import com.dsidak.bot.WeatherBot
import org.mockito.Mockito.*
import org.telegram.telegrambots.abilitybots.api.db.DBContext
import org.telegram.telegrambots.abilitybots.api.db.MapDBContext
import org.telegram.telegrambots.abilitybots.api.objects.MessageContext
import org.telegram.telegrambots.abilitybots.api.sender.SilentSender
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.objects.message.Message
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test


class WeatherBotTest {
    // Bot to test
    private lateinit var bot: WeatherBot
    // Sender to mock
    private lateinit var sender: SilentSender
    // Offline DB instance for the test
    private lateinit var db: DBContext

    @BeforeTest
    fun setUp() {
        // Offline DB instance will get deleted at JVM shutdown
        db = MapDBContext.offlineInstance("test${Random.nextInt()}")
        // Create bot with our offline DB
        bot = WeatherBot(OkHttpTelegramClient(TOKEN), BOT_USERNAME + Random.nextInt(), db)
        // Call onRegister() to initialize abilities etc.
        bot.onRegister()
        // Create a new sender as a mock
        sender = mock(SilentSender::class.java)
        // Set your bot silent sender to the mocked sender
        bot.setSilentSender(sender)
    }

    @AfterTest
    fun tearDown() {
        db.clear()
    }

    @Test
    fun testHelloWorldCommand() {
        val update = Update()
        // Create a new User
        val user = User(USER_ID, FIRST_NAME, false)
        // Context is a necessary consumer item for the ability
        val context = MessageContext.newContext(update, user, CHAT_ID, bot)

        // Consume a context in the lambda declaration, so we pass the context to the action logic
        bot.helloWorldCommand().action().accept(context)

        // We verify that the silent sender was called only ONCE and sent Hello World to CHAT_ID
        // The silent sender here is a mock!
        verify(sender, times(1)).send("Hello world!", CHAT_ID)
    }

    @Test
    fun testWeather() {
        val update = mockFullUpdate("/weather tomorrow")
        bot.consume(update)
        verify(sender, times(1)).send("Please, provide your location first using /location <city>", USER.id)
    }

    @Test
    fun testWeatherCommand_wrongArgumentsNumber() {
        val zeroArgUpdate = mockFullUpdate("/weather")
        bot.consume(zeroArgUpdate)
        verify(sender, times(1)).send("Sorry, this feature requires 1 additional input.", USER.id)

        val multipleArgsUpdate = mockFullUpdate("/weather 1 2 3")
        bot.consume(multipleArgsUpdate)
        verify(sender, times(2)).send("Sorry, this feature requires 1 additional input.", USER.id)
    }

    @Test
    fun testWeatherCommand_invalidArgument() {
        val invalidArgUpdate = mockFullUpdate("/weather yesterday")
        bot.consume(invalidArgUpdate)
        verify(sender, times(1)).send("Invalid argument 'yesterday'. Please, provide valid offset", USER.id)

        val invalidArgUpdate2 = mockFullUpdate("/weather 666")
        bot.consume(invalidArgUpdate2)
        verify(sender, times(1)).send("Invalid argument '666'. Please, provide valid offset", USER.id)

        val invalidArgUpdate3 = mockFullUpdate("/weather -1")
        bot.consume(invalidArgUpdate3)
        verify(sender, times(1)).send("Invalid argument '-1'. Please, provide valid offset", USER.id)

        val outOfBound = BotProperties.UPPER_BOUND + 1
        val invalidArgUpdate4 = mockFullUpdate("/weather $outOfBound")
        bot.consume(invalidArgUpdate4)
        verify(sender, times(1)).send("Invalid argument '$outOfBound'. Please, provide valid offset", USER.id)
    }

    @Test
    fun testLocationCommand_setThenUpdateLocation() {
        val update = mockFullUpdate("/location Sofia")
        bot.consume(update)
        verify(sender, times(1)).send("Location set to Sofia", USER.id)

        val update2 = mockFullUpdate("/location Plovdiv")
        bot.consume(update2)
        verify(sender, times(1)).send("Location updated from Sofia to Plovdiv", USER.id)

        val update3 = mockFullUpdate("/location Tbilisi")
        bot.consume(update3)
        verify(sender, times(1)).send("Location updated from Plovdiv to Tbilisi", USER.id)
    }

    @Test
    fun testLocationCommand_wrongArgumentsNumber() {
        val zeroArgUpdate = mockFullUpdate("/location")
        bot.consume(zeroArgUpdate)
        verify(sender, times(1)).send("Sorry, this feature requires 1 additional input.", USER.id)

        val multipleArgsUpdate = mockFullUpdate("/location Tut Tam")
        bot.consume(multipleArgsUpdate)
        verify(sender, times(2)).send("Sorry, this feature requires 1 additional input.", USER.id)
    }

    private fun mockFullUpdate(args: String, fromUser: User = USER): Update {
        bot.users()[USER.id] = USER
        bot.users()[CREATOR.id] = CREATOR
        bot.userIds()[USER.userName] = USER.id
        bot.userIds()[CREATOR.userName] = CREATOR.id

        bot.admins().add(CREATOR.id)

        val user: User = mockUser(fromUser)

        val update = mock(Update::class.java)
        `when`(update.hasMessage()).thenReturn(true)
        val message = mock(Message::class.java)
        `when`(message.from).thenReturn(user)
        `when`(message.text).thenReturn(args)
        `when`(message.hasText()).thenReturn(true)
        `when`(message.isUserMessage).thenReturn(true)
        `when`(message.chatId).thenReturn(fromUser.id)
        `when`(update.message).thenReturn(message)
        return update
    }

    private fun mockUser(fromUser: User): User {
        val user = mock(User::class.java)
        `when`(user.id).thenReturn(fromUser.id)
        `when`(user.userName).thenReturn(fromUser.userName)
        `when`(user.firstName).thenReturn(fromUser.firstName)
        `when`(user.lastName).thenReturn(fromUser.lastName)

        return user
    }

    companion object {
        // User-specific constants
        private const val USER_ID = 666L
        private const val FIRST_NAME = "Abobus"

        // Chat-specific constants
        private const val CHAT_ID = 666L

        // Bot-specific constants
        private const val TOKEN = "TOKEN"
        private const val BOT_USERNAME = "TestBot"

        private val USER =
            User.builder().id(1).firstName("fname").lastName("lname").userName("uname").isBot(false).build()
        private val CREATOR =
            User.builder().id(777).firstName("creatorFirst").lastName("creatorLast").userName("creatorUsername")
                .isBot(false).build()
    }
}