package com.dsidak.bot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.logging.LogLevel
import java.util.Properties

fun runEchoBot(properties: Properties): Bot {
    val bot = bot {
        token = properties.getProperty("BOT_API_KEY")
        logLevel = LogLevel.Network.Body

        dispatch {
            text {
                bot.sendMessage(
                    chatId = ChatId.fromId(message.chat.id),
                    messageThreadId = message.messageThreadId,
                    text = text,
                    protectContent = true,
                    disableNotification = false,
                )
            }
        }
    }

    return bot
}