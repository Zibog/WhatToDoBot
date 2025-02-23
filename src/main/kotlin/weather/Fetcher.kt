package com.dsidak.weather

import com.dsidak.Secrets
import com.dsidak.bot.BotProperties
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDate

object Fetcher {
    private val httpClient = OkHttpClient().newBuilder().build()

    /**
     * Fetches the weather data for a given city and date.
     *
     * @param city the name of the city
     * @param date the date for which to fetch the weather
     * @return a string containing the weather data
     */
    fun fetchWeather(city: String, date: LocalDate): String {
        // TODO: check if we have saved data for (city, date) in DB
        val url = toUrl(city, date)
        val request = Request.Builder()
            .url(url)
            .header("charset", StandardCharsets.UTF_8.name())
            .get()
            .build()
        // TODO: save fetched data to DB
        return "Let's do some code!"
    }

    /**
     * Constructs the URL for the weather API request.
     *
     * @param city the name of the city
     * @param date the date for which to fetch the weather
     * @return the constructed URL as a string
     */
    internal fun toUrl(city: String, date: LocalDate): String {
        val offset = date.toEpochDay() - LocalDate.now().toEpochDay()
        val endpoint = if (offset == 0L) BotProperties.WEATHER_CURRENT else BotProperties.WEATHER_FORECAST
        val url = "${BotProperties.WEATHER_API_URL}/$endpoint?q=${getCityQuery(city)}&appid=${Secrets.WEATHER_API_KEY}"
        if (endpoint == BotProperties.WEATHER_FORECAST) {
            return url.plus("&cnt=$offset")
        }
        return url
    }

    /**
     * Encodes the city name for use in the URL query.
     *
     * @param city the name of the city
     * @return the encoded city name
     */
    private fun getCityQuery(city: String): String {
        return URLEncoder.encode(city, StandardCharsets.UTF_8)
    }
}