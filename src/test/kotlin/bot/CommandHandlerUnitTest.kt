package bot

import com.dsidak.bot.CommandHandler
import com.dsidak.configuration.config
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class CommandHandlerUnitTest {
    @Test
    fun testParseDayOffset() {
        val date = LocalDate.of(2025, 2, 22)

        // Expected offsets
        assertEquals(LocalDate.of(2025, 2, 22), CommandHandler.offsetDate(date, "today"))
        assertEquals(LocalDate.of(2025, 2, 23), CommandHandler.offsetDate(date, "tomorrow"))
        assertEquals(LocalDate.of(2025, 2, 22), CommandHandler.offsetDate(date, config.lowerBound.toString()))
        assertEquals(LocalDate.of(2025, 2, 24), CommandHandler.offsetDate(date, "2"))
        assertEquals(LocalDate.of(2025, 2, 27), CommandHandler.offsetDate(date, config.upperBound.toString()))

        // Wrong offsets
        assertEquals(LocalDate.EPOCH, CommandHandler.offsetDate(date, "yesterday"))
        assertEquals(LocalDate.EPOCH, CommandHandler.offsetDate(date, "666"))
        assertEquals(LocalDate.EPOCH, CommandHandler.offsetDate(date, "-1"))
        assertEquals(LocalDate.EPOCH, CommandHandler.offsetDate(date, "-1"))
        assertEquals(LocalDate.EPOCH, CommandHandler.offsetDate(date, (config.upperBound + 1).toString()))
    }
}