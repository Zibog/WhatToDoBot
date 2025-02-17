package bot

import com.dsidak.bot.BotProperties
import com.dsidak.bot.loadProperties
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class StartupTest {

    @Test
    fun testLoadProperties() {
        // TODO: pass a path to testable properties here?
        val properties = loadProperties()
        assertEquals("https://api.open-meteo.com/v1", properties.getProperty(BotProperties.WEATHER_API_URL))
    }
}