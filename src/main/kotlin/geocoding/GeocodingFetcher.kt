package com.dsidak.geocoding

import com.dsidak.configuration.config
import com.dsidak.dotenv
import com.dsidak.exception.RequestFailureException
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
    fun fetchCoordinates(city: String, country: String): List<CityInfo> {
        val url = toUrl(city, country)

        val request = Request.Builder()
            .url(url)
            .header("charset", StandardCharsets.UTF_8.name())
            .get()
            .build()

        val response = try {
            executeRequest(request)
        } catch (e: RequestFailureException) {
            return emptyList()
        }

        return response
    }

    @Throws(RequestFailureException::class)
    override fun parseResponse(body: String): List<CityInfo> {
        return try {
            log.info { "Parsing geocoding response: $body" }
            json.decodeFromString<List<CityInfo>>(body)
        } catch (e: Exception) {
            val message = "Failed to parse geocoding response: ${e.message}"
            log.error { message }
            throw RequestFailureException(message, e)
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