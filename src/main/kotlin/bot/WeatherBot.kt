package com.dsidak.bot

import com.dsidak.bot.BotProperties.LOWER_BOUND
import com.dsidak.bot.BotProperties.UPPER_BOUND
import io.github.oshai.kotlinlogging.KotlinLogging
import org.telegram.telegrambots.abilitybots.api.bot.AbilityBot
import org.telegram.telegrambots.abilitybots.api.objects.Ability
import org.telegram.telegrambots.abilitybots.api.objects.Locality
import org.telegram.telegrambots.abilitybots.api.objects.Privacy
import org.telegram.telegrambots.abilitybots.api.sender.SilentSender
import org.telegram.telegrambots.meta.generics.TelegramClient
import java.time.LocalDate

class WeatherBot(telegramClient: TelegramClient, botUsername: String) : AbilityBot(telegramClient, botUsername) {
    private val log = KotlinLogging.logger {}

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
                log.debug { "Going to parse arg=$inputArg" }
                val dateWithOffset = offsetDate(LocalDate.now(), inputArg)
                silent.send(dateWithOffset.toString(), ctx.chatId())
                log.debug { "Check the weather for $dateWithOffset" }
            }
        return builder.build()
    }

    companion object {
        internal fun offsetDate(date: LocalDate, arg: String): LocalDate {
            if (arg.equals("today", ignoreCase = true)) {
                return date
            }
            if (arg.equals("tomorrow", ignoreCase = true)) {
                return date.plusDays(1)
            }

            val long = arg.toLongOrNull()
            if (long != null && long in LOWER_BOUND..UPPER_BOUND) {
                return date.plusDays(long)
            }

            return LocalDate.EPOCH
        }
    }
}