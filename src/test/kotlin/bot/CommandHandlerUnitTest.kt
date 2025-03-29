package bot

import com.dsidak.bot.CommandHandler
import com.dsidak.configuration.config
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

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
        assertNull(CommandHandler.offsetDate(date, "yesterday"))
        assertNull(CommandHandler.offsetDate(date, "666"))
        assertNull(CommandHandler.offsetDate(date, "-1"))
        assertNull(CommandHandler.offsetDate(date, "-1"))
        assertNull(CommandHandler.offsetDate(date, (config.upperBound + 1).toString()))
    }
}