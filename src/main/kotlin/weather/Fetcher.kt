package com.dsidak.weather

import arrow.core.Either
import com.dsidak.chatbot.Client
import com.dsidak.configuration.config
import com.dsidak.db.DatabaseManager
import com.dsidak.db.schemas.Location
import com.dsidak.dotenv
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.net.UnknownHostException
import java.nio.charset.StandardCharsets
import java.time.LocalDate

class Fetcher(private val httpClient: OkHttpClient = OkHttpClient().newBuilder().build()) {
    private val log = KotlinLogging.logger {}

    /**
     * Fetches the weather data for a given city and date.
     *
     * @param city the name of the city
     * @param date the date for which to fetch the weather
     * @return a string containing the weather data
     */
    fun fetchWeather(city: String, date: LocalDate): String {
        val location = runBlocking {
            DatabaseManager.locationService.readByCity(city)
        }
        val url = if (location != null) {
            log.info { "Using coordinates '${location.latitude}, ${location.longitude}' of ${location.city}" }
            toUrl(location.latitude, location.longitude, date)
        } else {
            log.info { "Location not found in DB, will fetch from API" }
            toUrl(city, date)
        }

        val request = Request.Builder()
            .url(url)
            .header("charset", StandardCharsets.UTF_8.name())
            .get()
            .build()

        val response = executeRequest(request).fold(
            { errorDescription: String -> return errorDescription },
            { response: WeatherResponse -> return@fold response }
        )

        if (location == null) {
            runBlocking {
                launch {
                    DatabaseManager.locationService.create(
                        Location(
                            city = response.cityName,
                            country = response.sys.country,
                            latitude = response.coordinates.latitude,
                            longitude = response.coordinates.longitude
                        )
                    )
                }.join()
            }
        }

        val geminiResponse = Client().generateContent(response)

        return "The weather in ${response.cityName} is ${response.weather[0].description} with a temperature of ${response.main.temperature}°C"
    }

    internal fun executeRequest(request: Request): Either<String, WeatherResponse> {
        runCatching {
            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    response.body?.use { body ->
                        val weatherResponse = Json.decodeFromString<WeatherResponse>(body.string())
                        return Either.Right(weatherResponse)
                    }
                } else {
                    return Either.Left("Request failed: ${response.code} ${response.message}")
                }
            }
        }.onFailure {
            val errorMessage = "Failed to execute request"
            log.error { it.printStackTrace() }
            return when (it) {
                is UnknownHostException -> Either.Left("$errorMessage: can't connect to remote service")
                else -> Either.Left("$errorMessage. ${it.message.orEmpty()}")
            }
        }

        // Everything should be handled earlier
        return Either.Left("Failed to execute request, try again later")
    }

    companion object {
        /**
         * Constructs the URL for the weather API request.
         *
         * @param city the name of the city
         * @param date the date for which to fetch the weather
         * @return the constructed URL as a string
         */
        internal fun toUrl(city: String, date: LocalDate): String {
            val offset = date.toEpochDay() - LocalDate.now().toEpochDay()
            val endpoint = if (offset == 0L) config.weather.weatherCurrent else config.weather.weatherForecast
            val url = "${config.weather.weatherApiUrl}/$endpoint" +
                    "?q=${getCityQuery(city)}" +
                    "&appid=${dotenv["WEATHER_API_KEY"]}" +
                    "&units=${config.weather.weatherUnits}"
            if (endpoint == config.weather.weatherForecast) {
                return url.plus("&cnt=$offset")
            }
            return url
        }

        internal fun toUrl(latitude: Double, longitude: Double, date: LocalDate): String {
            val offset = date.toEpochDay() - LocalDate.now().toEpochDay()
            val endpoint = if (offset == 0L) config.weather.weatherCurrent else config.weather.weatherForecast
            val url = "${config.weather.weatherApiUrl}/$endpoint" +
                    "?lat=$latitude" +
                    "&lon=$longitude" +
                    "&appid=${dotenv["WEATHER_API_KEY"]}" +
                    "&units=${config.weather.weatherUnits}"
            if (endpoint == config.weather.weatherForecast) {
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
}