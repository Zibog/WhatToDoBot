package com.dsidak.bot

import org.telegram.telegrambots.abilitybots.api.bot.AbilityBot
import org.telegram.telegrambots.abilitybots.api.objects.Ability
import org.telegram.telegrambots.abilitybots.api.objects.Locality
import org.telegram.telegrambots.abilitybots.api.objects.Privacy
import org.telegram.telegrambots.abilitybots.api.sender.SilentSender
import org.telegram.telegrambots.meta.generics.TelegramClient

class WeatherBot(telegramClient: TelegramClient, botUsername: String) : AbilityBot(telegramClient, botUsername) {
    override fun creatorId(): Long {
        TODO("Not yet implemented")
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
}