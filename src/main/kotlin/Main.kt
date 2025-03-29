package com.dsidak

import com.dsidak.bot.WeatherBot
import io.github.cdimascio.dotenv.dotenv
import io.github.oshai.kotlinlogging.KotlinLogging
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication
import java.time.Instant

val log = KotlinLogging.logger {}

fun main() {
    val token = dotenv["BOT_API_KEY"]
    val botUsername = dotenv["BOT_USERNAME"]
    log.debug { "$botUsername starts using token=$token" }
    val start = Instant.now()
    try {
        // Instantiate Telegram Bots API
        TelegramBotsLongPollingApplication().use { botsApplication ->
            // Create Bot
            val bot = WeatherBot(OkHttpTelegramClient(token), botUsername)
            // Enable abilities
            bot.onRegister()
            // Register our bot
            botsApplication.registerBot(token, bot)
            log.info { "${bot::class.java} successfully started in ${Instant.now().toEpochMilli() - start.toEpochMilli()}ms" }
            Thread.currentThread().join()
        }
    } catch (e: Exception) {
        log.error { e }
    }
}

val dotenv = dotenv {
    directory = "env"
    ignoreIfMissing = true
}