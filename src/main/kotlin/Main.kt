package com.dsidak

import com.dsidak.bot.WeatherBot
import io.github.oshai.kotlinlogging.KotlinLogging
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication

val log = KotlinLogging.logger {}

fun main() {
    val token = Secrets.BOT_API_KEY
    val botUsername = Secrets.BOT_USERNAME
    log.debug { "$botUsername starts using token=$token" }
    try {
        // Instantiate Telegram Bots API
        TelegramBotsLongPollingApplication().use { botsApplication ->
            // Create Bot
            val bot = WeatherBot(OkHttpTelegramClient(token), botUsername)
            // Enable abilities
            bot.onRegister()
            // Register our bot
            botsApplication.registerBot(token, bot)
            log.info { "${bot::class.java} successfully started" }
            Thread.currentThread().join()
        }
    } catch (e: Exception) {
        log.error { e }
    }
}