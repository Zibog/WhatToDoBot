package com.dsidak

import com.dsidak.bot.loadProperties
import com.dsidak.bot.runEchoBot
import java.io.File
import java.util.*

fun main() {
    val properties = loadProperties()
    val bot = runEchoBot(properties)
    bot.startPolling()
}