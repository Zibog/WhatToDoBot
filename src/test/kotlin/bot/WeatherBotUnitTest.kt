package bot

import com.dsidak.bot.WeatherBot
import com.dsidak.configuration.config
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class WeatherBotUnitTest {
    @Test
    fun testParseDayOffset() {
        val date = LocalDate.of(2025, 2, 22)

        // Expected offsets
        assertEquals(LocalDate.of(2025, 2, 22), WeatherBot.offsetDate(date, "today"))
        assertEquals(LocalDate.of(2025, 2, 23), WeatherBot.offsetDate(date, "tomorrow"))
        assertEquals(LocalDate.of(2025, 2, 22), WeatherBot.offsetDate(date, config.lowerBound.toString()))
        assertEquals(LocalDate.of(2025, 2, 24), WeatherBot.offsetDate(date, "2"))
        assertEquals(LocalDate.of(2025, 2, 27), WeatherBot.offsetDate(date, config.upperBound.toString()))

        // Wrong offsets
        assertEquals(LocalDate.EPOCH, WeatherBot.offsetDate(date, "yesterday"))
        assertEquals(LocalDate.EPOCH, WeatherBot.offsetDate(date, "666"))
        assertEquals(LocalDate.EPOCH, WeatherBot.offsetDate(date, "-1"))
        assertEquals(LocalDate.EPOCH, WeatherBot.offsetDate(date, "-1"))
        assertEquals(LocalDate.EPOCH, WeatherBot.offsetDate(date, (config.upperBound + 1).toString()))
    }
}