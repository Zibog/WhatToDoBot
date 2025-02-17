package com.dsidak.bot

import com.dsidak.openmeteo.Fetcher
import com.dsidak.openmeteo.RequestContext
import com.dsidak.openmeteo.RequestType
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.handlers.CommandHandlerEnvironment
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.github.kotlintelegrambot.logging.LogLevel
import java.time.MonthDay
import java.util.*

class WeatherBot(private val properties: Properties): TgBot {
    private val bot = bot {
        this.token = properties.getProperty(BotProperties.BOT_API_KEY)
        this.logLevel = LogLevel.Network.Body

        this.dispatch {
            command("start") {
                handleStart()
            }

            command("weather") {
                handleWeather()
            }

            command("help") {
                handleHelp()
            }

            command("inlineButtons") {
                val inlineKeyboardMarkup = InlineKeyboardMarkup.create(
                    listOf(InlineKeyboardButton.CallbackData(text = "Test Inline Button", callbackData = "testButton")),
                    listOf(InlineKeyboardButton.CallbackData(text = "Show alert", callbackData = "showAlert")),
                )
                bot.sendMessage(
                    chatId = ChatId.fromId(message.chat.id),
                    text = "Hello, inline buttons!",
                    replyMarkup = inlineKeyboardMarkup,
                )
            }

            callbackQuery("testButton") {
                val chatId = callbackQuery.message?.chat?.id ?: return@callbackQuery
                bot.sendMessage(ChatId.fromId(chatId), callbackQuery.data)
            }

            callbackQuery(
                callbackData = "showAlert",
                callbackAnswerText = "HelloText",
                callbackAnswerShowAlert = true,
            ) {
                val chatId = callbackQuery.message?.chat?.id ?: return@callbackQuery
                bot.sendMessage(ChatId.fromId(chatId), callbackQuery.data)
            }
        }
    }

    private fun CommandHandlerEnvironment.handleStart() {
        val result = bot.sendMessage(chatId = ChatId.fromId(update.message!!.chat.id), text = "Bot started")

        result.fold(
            {
                // do something here with the response
            },
            {
                // do something with the error
            },
        )
    }

    private fun CommandHandlerEnvironment.handleWeather() {
        if (args.size != 1) {
            bot.sendMessage(chatId = ChatId.fromId(update.message!!.chat.id), text = "Please, enter one day marker")
            return
        }

        val day = args[0]
        val result = bot.sendMessage(chatId = ChatId.fromId(update.message!!.chat.id), text = "Oh yeah, $day")

        // TODO: properly handle the 'day' argument. Define allowed values
        val ctx = RequestContext(
            52.52,
            13.41,
            MonthDay.parse(day),
            properties.getProperty(BotProperties.WEATHER_API_URL)
        )
        val fetcher = Fetcher(RequestType.FORECAST, ctx)
        // TODO: coroutine? Potentially long operation
        val answer = fetcher.fetch()

        result.fold(
            {
                bot.sendMessage(chatId = ChatId.fromId(update.message!!.chat.id), text = answer)
            },
            {
                // do something with the error
            },
        )
    }

    private fun CommandHandlerEnvironment.handleHelp() {
        bot.sendMessage(chatId = ChatId.fromId(update.message!!.chat.id),
            text = """Usage:
                | /weather <day marker> - ask an advice on what to do on a specific day
                | e.g. /weather tomorrow, /weather +2, /weather 26, /weather Monday
            """.trimMargin())
    }

    override fun getBot(): Bot {
        bot.startPolling()
        return bot
    }

    override fun close() {
        bot.stopPolling()
        bot.close()
    }
}