package com.dsidak.chatbot

import com.dsidak.configuration.config
import com.dsidak.dotenv
import com.dsidak.exception.RequestFailureException
import com.dsidak.http.RequestExecutor
import com.dsidak.weather.WeatherResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.LocalDate
import java.util.concurrent.TimeUnit

/**
 * Client for interacting with the Gemini API to generate content based on weather data.
 *
 * @property httpClient the HTTP client used for making requests
 */
class GeminiClient(
    httpClient: OkHttpClient = OkHttpClient().newBuilder().callTimeout(config.requestTimeout, TimeUnit.SECONDS).build()
) : RequestExecutor<GeminiFlashResponse>(httpClient) {
    private val log = KotlinLogging.logger {}
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Generates content based on the provided weather data and date.
     *
     * @param weather the weather data
     * @param date the date for which to generate content
     * @return the generated content as a string
     */
    @Throws(RequestFailureException::class)
    fun generateContent(weather: WeatherResponse, date: LocalDate): String {
        val content = """
            |Weather in ${weather.cityName}, ${weather.country}:
            |${weather.weather[0].description}
            |Temperature: ${weather.main.temperature}°C
            |Feels like: ${weather.main.feelsLike}°C
            |Humidity: ${weather.main.humidity}%
            |Wind speed: ${weather.wind.speed} m/s
            |What would you recommend to do in ${weather.cityName} at $date and that weather?
            |Advice something interesting and unique.
            |Start with words "I recommend you to" and then describe your recommendation.
        """.trimMargin()
        val requestContent = GeminiFlashRequest(
            contents = listOf(
                Content(
                    parts = listOf(ContentPart(text = content))
                )
            )
        )
        val jsonRequest = json.encodeToString(requestContent)

        val response = try {
            executeRequest(
                Request.Builder()
                    .url(toUrl(config.gemini.modelUrl, config.gemini.modelName, config.gemini.modelAction))
                    .post(jsonRequest.toRequestBody("application/json".toMediaType()))
                    .build()
            )
        } catch (e: RequestFailureException) {
            throw RequestFailureException("Failed to generate content, try again later")
        }

        return response.candidates[0].content.parts[0].text
    }

    @Throws(RequestFailureException::class)
    override fun parseResponse(body: String): GeminiFlashResponse {
        return try {
            log.info { "Parsing Gemini response" }
            json.decodeFromString<GeminiFlashResponse>(body)
        } catch (e: Exception) {
            val message = "Failed to parse Gemini response: ${e.message}"
            log.error { message }
            throw RequestFailureException(message, e)
        }
    }

    companion object {
        /**
         * Constructs the URL for the Gemini API request.
         *
         * @param url the base URL
         * @param model the model name
         * @param action the action name
         * @return the constructed URL as a string
         */
        internal fun toUrl(url: String, model: String, action: String): String {
            return "$url/$model:$action?key=${dotenv["GEMINI_API_KEY"]}"
        }
    }
}