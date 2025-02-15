package bot

import com.dsidak.bot.EchoBot
import com.dsidak.bot.TgBot
import com.dsidak.bot.loadProperties
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class EchoBotTest {
    private lateinit var bot: TgBot

    @BeforeTest
    fun setUp() {
        bot = EchoBot(loadProperties())
    }

    @AfterTest
    fun tearDown() {
        bot.close()
    }

    @Test
    fun testEchoBot() {
        assertTrue(bot.getBot().getMe().isSuccess, "Bot should start successfully")
    }
}