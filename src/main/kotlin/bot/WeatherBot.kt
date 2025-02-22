package com.dsidak.bot

import org.telegram.telegrambots.abilitybots.api.bot.AbilityBot
import org.telegram.telegrambots.abilitybots.api.objects.Ability
import org.telegram.telegrambots.abilitybots.api.objects.Locality
import org.telegram.telegrambots.abilitybots.api.objects.Privacy
import org.telegram.telegrambots.abilitybots.api.sender.SilentSender
import org.telegram.telegrambots.meta.generics.TelegramClient
import java.time.Instant
import java.time.ZoneId

class WeatherBot(telegramClient: TelegramClient, botUsername: String) : AbilityBot(telegramClient, botUsername) {
    override fun creatorId(): Long {
        return BotProperties.BOT_CREATOR_ID
    }

    internal fun setSilentSender(silentSender: SilentSender) {
        this.silent = silentSender
    }

    // For fun and testing
    fun sayHelloWorld(): Ability {
        return Ability.builder()
            .name("hello")
            .info("Just says hello world")
            .privacy(Privacy.PUBLIC)
            .locality(Locality.ALL)
            .action { ctx -> silent.send("Hello world!", ctx.chatId()) }
            .build()
    }

    fun checkWeather(): Ability {
        val builder = Ability.builder()
        builder
            .name("weather")
            .info("Request weather for the day")
            .privacy(Privacy.PUBLIC)
            .locality(Locality.ALL)
            .input(1)
            .action { ctx ->
                val inputArg = ctx.arguments()[0]
                silent.send(inputArg, ctx.chatId())
                val offset = argToDayOffset(inputArg)
                silent.send(Instant.now().atZone(ZoneId.systemDefault()).plusDays(offset).toString(), ctx.chatId())
            }
        return builder.build()
    }

    private fun argToDayOffset(arg: String): Long {
        if (arg.equals("today", ignoreCase = true)) {
            return 0
        }
        if (arg.equals("tomorrow", ignoreCase = true)) {
            return 1
        }

        val long = arg.toLongOrNull()
        if (long != null) {
            return long
        }

        return -1
    }
}