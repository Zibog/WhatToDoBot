package com.dsidak

import com.dsidak.bot.loadProperties
import com.dsidak.bot.runEchoBot

fun main() {
    val properties = loadProperties()
    val bot = runEchoBot(properties)
    bot.startPolling()
}