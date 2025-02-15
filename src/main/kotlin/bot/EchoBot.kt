package com.dsidak.bot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.logging.LogLevel
import java.util.*

class EchoBot(properties: Properties): TgBot {
    private val bot = bot {
        token = properties.getProperty(BOT_API_KEY)
        logLevel = LogLevel.Network.Body

        dispatch {
            text {
                bot.sendMessage(
                    chatId = com.github.kotlintelegrambot.entities.ChatId.Companion.fromId(message.chat.id),
                    messageThreadId = message.messageThreadId,
                    text = text,
                    protectContent = true,
                    disableNotification = false,
                )
            }
        }
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