package com.dsidak

import com.dsidak.bot.BotProperties
import com.dsidak.bot.WeatherBot
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication

fun main() {
    val token = BotProperties.BOT_API_KEY
    try {
        // Instantiate Telegram Bots API
        TelegramBotsLongPollingApplication().use { botsApplication ->
            // Create Bot
            val bot = WeatherBot(OkHttpTelegramClient(token), BotProperties.BOT_USERNAME)
            // Enable abilities
            bot.onRegister()
            // Register our bot
            botsApplication.registerBot(token, bot)
            println("${bot.javaClass.simpleName} successfully started")
            Thread.currentThread().join()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}