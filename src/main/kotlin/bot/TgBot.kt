package com.dsidak.bot

import com.github.kotlintelegrambot.Bot

interface TgBot: AutoCloseable {
    fun getBot(): Bot
}