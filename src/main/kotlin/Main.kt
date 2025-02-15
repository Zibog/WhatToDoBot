package com.dsidak

import com.dsidak.bot.WeatherBot
import com.dsidak.bot.loadProperties

fun main() {
    val bot = WeatherBot(loadProperties()).getBot()
}