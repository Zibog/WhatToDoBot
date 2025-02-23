package com.dsidak.bot

object BotProperties {
    const val WEATHER_API_URL = "api.openweathermap.org/data/2.5"
    const val WEATHER_FORECAST = "forecast/daily"

    // Accept requests from today (0) up to 7 days upwards
    // LOWER_BOUND is intended to ALWAYS be zero
    const val LOWER_BOUND = 0L
    const val UPPER_BOUND = 7L
}