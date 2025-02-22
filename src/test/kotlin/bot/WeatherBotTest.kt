package bot

import com.dsidak.bot.WeatherBot
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.telegram.telegrambots.abilitybots.api.db.DBContext
import org.telegram.telegrambots.abilitybots.api.db.MapDBContext
import org.telegram.telegrambots.abilitybots.api.objects.MessageContext
import org.telegram.telegrambots.abilitybots.api.sender.SilentSender
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.User
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
        db = MapDBContext.offlineInstance("test")
        // Create bot
        bot = WeatherBot(OkHttpTelegramClient(TOKEN), BOT_USERNAME)
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
    fun testSayHelloWorld() {
        val update = Update()
        // Create a new User
        val user = User(USER_ID, "A", false)
        // Context is a necessary consumer item for the ability
        val context = MessageContext.newContext(update, user, CHAT_ID, bot)

        // Consume a context in the lambda declaration, so we pass the context to the action logic
        bot.sayHelloWorld().action().accept(context)

        // We verify that the silent sender was called only ONCE and sent Hello World to CHAT_ID
        // The silent sender here is a mock!
        Mockito.verify(sender, times(1)).send("Hello world!", CHAT_ID)
    }

    companion object {
        private const val USER_ID = 666L
        private const val CHAT_ID = 666L
        private const val TOKEN = "TOKEN"
        private const val BOT_USERNAME = "TestBot"
    }
}