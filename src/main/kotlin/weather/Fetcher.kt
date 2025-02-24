package com.dsidak.weather

import arrow.core.Either
import com.dsidak.Secrets
import com.dsidak.bot.BotProperties
import kotlinx.serialization.json.Json
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

        val response = executeRequest(request).fold(
            { errorDescription: String -> return errorDescription },
            { response: WeatherResponse -> return@fold response }
        )
        // TODO: Handle response
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
        val url = "${BotProperties.WEATHER_API_URL}/$endpoint" +
                "?q=${getCityQuery(city)}" +
                "&appid=${Secrets.WEATHER_API_KEY}" +
                "&units=${BotProperties.WEATHER_UNITS}"
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

    internal fun executeRequest(request: Request): Either<String, WeatherResponse> {
        httpClient.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                response.body?.use { body ->
                    val weatherResponse = Json.decodeFromString<WeatherResponse>(body.string())
                    return Either.Right(weatherResponse)
                }
            } else {
                return Either.Left("Response failed with code ${response.code} ${response.message}")
            }
        }

        return Either.Left("Failed to execute request, try again later")
    }
}