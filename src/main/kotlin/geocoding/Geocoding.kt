package com.dsidak.geocoding

import arrow.core.Either
import com.dsidak.configuration.config
import com.dsidak.dotenv
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.UnknownHostException
import java.nio.charset.StandardCharsets

class Geocoding(private val httpClient: OkHttpClient = OkHttpClient().newBuilder().build()) {
    private val log = KotlinLogging.logger {}
    private val json = Json { ignoreUnknownKeys = true }

    fun fetchCoordinates(city: String): CityInfo {
        val url = toUrl(city)

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

    internal fun executeRequest(request: Request): Either<String, List<CityInfo>> {
        runCatching {
            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    response.body?.use { body ->
                        val geoResponse = json.decodeFromString<List<CityInfo>>(body.string())
                        if (geoResponse.isEmpty()) {
                            return Either.Left("No results found for the given city")
                        }
                        return Either.Right(geoResponse)
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
        internal fun toUrl(city: String, limit: Int = 1): HttpUrl {
            val builder = HttpUrl.Builder()
                .scheme("https")
                .host(config.weather.weatherHost)
                .addPathSegments(config.weather.geoPath)
                .addQueryParameter("q", city)
                .addQueryParameter("limit", limit.toString())
                .addQueryParameter("appid", dotenv["WEATHER_API_KEY"])

            return builder.build()
        }
    }
}