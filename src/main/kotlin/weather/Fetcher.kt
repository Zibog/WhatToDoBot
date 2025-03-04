package com.dsidak.weather

import arrow.core.Either
import com.dsidak.chatbot.Client
import com.dsidak.configuration.config
import com.dsidak.db.DatabaseManager
import com.dsidak.db.schemas.Location
import com.dsidak.dotenv
import com.dsidak.log
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.UnknownHostException
import java.nio.charset.StandardCharsets
import java.time.LocalDate

class Fetcher(private val httpClient: OkHttpClient = OkHttpClient().newBuilder().build()) {
    private val log = KotlinLogging.logger {}
    private val client = Client()
    private val json = Json { ignoreUnknownKeys = true }

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
                            country = response.country,
                            latitude = response.coordinates.latitude,
                            longitude = response.coordinates.longitude
                        )
                    )
                }.join()
            }
        }

        val geminiResponse = client.generateContent(response, date)
        return geminiResponse
    }

    internal fun executeRequest(request: Request): Either<String, WeatherResponse> {
        runCatching {
            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    response.body?.use { body ->
                        val weatherResponse = if (request.url.queryParameter("cnt") == null) {
                            json.decodeFromString<CurrentWeatherResponse>(body.string())
                        } else {
                            json.decodeFromString<ForecastWeatherResponse>(body.string())
                        }
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
         * @return the constructed URL
         */
        internal fun toUrl(city: String, date: LocalDate): HttpUrl {
            return buildUrlBase(date)
                .addQueryParameter("q", city)
                .build()
        }

        internal fun toUrl(latitude: Double, longitude: Double, date: LocalDate): HttpUrl {
            return buildUrlBase(date)
                .addQueryParameter("lat", latitude.toString())
                .addQueryParameter("lon", longitude.toString())
                .build()
        }

        private fun buildUrlBase(date: LocalDate): HttpUrl.Builder {
            val offset = date.toEpochDay() - LocalDate.now().toEpochDay()
            val endpoint = when (offset) {
                !in config.lowerBound..config.upperBound -> {
                    log.error { "Offset=$offset due to date=$date, should be between ${config.lowerBound} and ${config.upperBound}" }
                    throw IllegalArgumentException("Wrong date offset: should be between ${config.lowerBound} and ${config.upperBound}, but was $offset")
                }

                0L -> {
                    config.weather.weatherCurrent
                }

                else -> {
                    config.weather.weatherForecast
                }
            }

            val builder = HttpUrl.Builder()
                .scheme("https")
                .host(config.weather.weatherHost)
                .addPathSegments(config.weather.weatherPath)
                .addPathSegment(endpoint)
                .addQueryParameter("appid", dotenv["WEATHER_API_KEY"])
                .addQueryParameter("units", config.weather.weatherUnits)
            if (endpoint == config.weather.weatherForecast) {
                builder.addQueryParameter("cnt", offset.toString())
            }

            return builder
        }
    }
}