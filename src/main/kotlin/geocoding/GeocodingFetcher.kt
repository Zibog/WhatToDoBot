package com.dsidak.geocoding

import arrow.core.Either
import com.dsidak.configuration.config
import com.dsidak.dotenv
import com.dsidak.http.RequestExecutor
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.nio.charset.StandardCharsets

/**
 * Class responsible for fetching geocoding data (city to coordinates conversion).
 *
 * @property httpClient the HTTP client used for making requests
 */
class GeocodingFetcher(httpClient: OkHttpClient = OkHttpClient().newBuilder().build()) :
    RequestExecutor<List<CityInfo>>(httpClient) {
    private val log = KotlinLogging.logger {}
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Fetches the coordinates for a given city and country.
     *
     * @param city the name of the city
     * @param country the name of the country, optional
     * @return the [CityInfo] containing the coordinates
     */
    fun fetchCoordinates(city: String, country: String): CityInfo {
        val url = toUrl(city, country)

        val request = Request.Builder()
            .url(url)
            .header("charset", StandardCharsets.UTF_8.name())
            .get()
            .build()

        val response = executeRequest(request).fold(
            { errorDescription: String ->
                // TODO: no real reason to just log this message
                log.error { errorDescription }
                return CityInfo.EMPTY
            },
            { response: List<CityInfo> -> return@fold response }
        )

        // TODO: suggest to user to choose city if there are multiple results
        return response[0]
    }

    override fun parseResponse(body: String): Either<String, List<CityInfo>> {
        return try {
            log.info { "Parsing geocoding response: $body" }
            val cityInfos = json.decodeFromString<List<CityInfo>>(body)
            if (cityInfos.isEmpty()) {
                val message = "No results found for the given city"
                log.info { message }
                return Either.Left(message)
            }
            Either.Right(cityInfos)
        } catch (e: Exception) {
            val message = "Failed to parse geocoding response: ${e.message}"
            log.error { message }
            return Either.Left(message)
        }
    }

    companion object {
        /**
         * Constructs the URL for the geocoding API request.
         *
         * @param city the name of the city to search for
         * @param country the name of the country to search for, optional
         * @param limit the maximum number of results to return
         * @return the constructed URL
         */
        internal fun toUrl(city: String, country: String = "", limit: Int = 1): HttpUrl {
            val location = if (country.isNotEmpty()) {
                "$city,$country"
            } else {
                city
            }
            val builder = HttpUrl.Builder()
                .scheme("https")
                .host(config.weather.weatherHost)
                .addPathSegments(config.weather.geoPath)
                .addQueryParameter("q", location)
                .addQueryParameter("limit", limit.toString())
                .addQueryParameter("appid", dotenv["WEATHER_API_KEY"])

            return builder.build()
        }
    }
}