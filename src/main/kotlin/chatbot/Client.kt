package com.dsidak.chatbot

import arrow.core.Either
import com.dsidak.configuration.config
import com.dsidak.dotenv
import com.dsidak.weather.WeatherResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.UnknownHostException
import java.time.LocalDate

class Client(private val httpClient: OkHttpClient = OkHttpClient().newBuilder().build()) {
    private val log = KotlinLogging.logger {}
    private val json = Json { ignoreUnknownKeys = true }

    fun generateContent(weather: WeatherResponse, date: LocalDate): Either<String, WeatherResponse> {
        val content = """
            |Weather in ${weather.cityName}, ${weather.sys.country}:
            |${weather.weather[0].description}
            |Temperature: ${weather.main.temperature}°C
            |Feels like: ${weather.main.feelsLike}°C
            |Humidity: ${weather.main.humidity}%
            |Wind speed: ${weather.wind.speed} m/s
            |What would you recommend to do in ${weather.cityName} at $date and that weather?
        """.trimMargin()
        val requestContent = GeminiFlashRequest(
            contents = listOf(
                Content(
                    parts = listOf(ContentPart(text = content))
                )
            )
        )
        val jsonRequest = json.encodeToString(requestContent)

        val responseEither = executeRequest(
            Request.Builder()
                .url(toUrl(config.gemini.modelUrl, config.gemini.modelName, config.gemini.modelAction))
                .post(jsonRequest.toRequestBody("application/json".toMediaType()))
                .build()
        )

        // TODO: return actual response
        return responseEither
    }

    private fun executeRequest(request: Request): Either<String, WeatherResponse> {
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
        fun toUrl(url: String, model: String, action: String): String {
            return "$url/$model:$action?key=${dotenv["GEMINI_API_KEY"]}"
        }
    }
}