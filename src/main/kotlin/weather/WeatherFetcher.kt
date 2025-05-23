package com.dsidak.weather

import com.dsidak.configuration.config
import com.dsidak.db.DatabaseManager
import com.dsidak.db.schemas.Location
import com.dsidak.dotenv
import com.dsidak.exception.RequestFailureException
import com.dsidak.http.RequestExecutor
import com.dsidak.log
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.nio.charset.StandardCharsets
import java.time.LocalDate

/**
 * Class responsible for fetching weather data.
 *
 * @property httpClient the HTTP client used for making requests
 */
class WeatherFetcher(
    httpClient: OkHttpClient = OkHttpClient().newBuilder().build()
) : RequestExecutor<WeatherResponse>(httpClient) {
    private val log = KotlinLogging.logger {}
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Fetches the weather data for a given city and date.
     *
     * @param city the name of the city
     * @param date the date for which to fetch the weather
     * @return [WeatherResponse] containing the weather data
     */
    @Throws(RequestFailureException::class)
    suspend fun fetchWeather(city: String, date: LocalDate): WeatherResponse {
        val location = runBlocking {
            DatabaseManager.locationService.readByCity(city)
        }
        val url = if (location != null) {
            log.info { "Using coordinates '${location.latitude}, ${location.longitude}' of ${location.city}" }
            toUrl(location.latitude, location.longitude, date)
        } else {
            log.info { "Location for $city not found in DB, will fetch from API" }
            toUrl(city, date)
        }

        val request = Request.Builder()
            .url(url)
            .header("charset", StandardCharsets.UTF_8.name())
            .get()
            .build()

        val response = executeRequest(request)

        if (location == null) {
            withContext(Dispatchers.IO) {
                DatabaseManager.locationService.create(
                    Location(
                        city = response.cityName,
                        country = response.country,
                        latitude = response.coordinates.latitude,
                        longitude = response.coordinates.longitude
                    )
                )
            }
        }

        return response
    }

    @Throws(RequestFailureException::class)
    override fun parseResponse(body: String): WeatherResponse {
        return try {
            val jsonObject = json.parseToJsonElement(body).jsonObject

            // Check for the presence of a field that indicates a forecast response
            val weatherResponse = if ("list" in jsonObject) {
                log.info { "Parsing forecast weather response" }
                json.decodeFromJsonElement<ForecastWeatherResponse>(jsonObject)
            } else {
                log.info { "Parsing current weather response" }
                json.decodeFromJsonElement<CurrentWeatherResponse>(jsonObject)
            }

            weatherResponse
        } catch (e: Exception) {
            val message = "Failed to parse weather response: ${e.message}"
            log.error { message }
            throw RequestFailureException(message, e)
        }
    }

    companion object {
        /**
         * Constructs the URL for the weather API request using city name.
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

        /**
         * Constructs the URL for the weather API request using coordinates.
         *
         * @param latitude the latitude of the location
         * @param longitude the longitude of the location
         * @param date the date for which to fetch the weather
         * @return the constructed URL
         */
        internal fun toUrl(latitude: Double, longitude: Double, date: LocalDate): HttpUrl {
            return buildUrlBase(date)
                .addQueryParameter("lat", latitude.toString())
                .addQueryParameter("lon", longitude.toString())
                .build()
        }

        /**
         * Builds the base URL for the weather API request.
         *
         * @param date the date for which to fetch the weather
         * @return the base URL builder
         */
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