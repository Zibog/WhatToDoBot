package com.dsidak.bot

object BotProperties {
    const val BOT_API_KEY = ""
    const val BOT_USERNAME = ""
    const val BOT_CREATOR_ID = 0L
    const val WEATHER_API_URL = "https://api.open-meteo.com/v1"

    // Accept requests from today (0) up to 7 days upwards
    // LOWER_BOUND is intended to ALWAYS be zero
    const val LOWER_BOUND = 0L
    const val UPPER_BOUND = 7L
}