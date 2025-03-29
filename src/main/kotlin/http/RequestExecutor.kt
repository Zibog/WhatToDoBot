package com.dsidak.http

import arrow.core.Either
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.io.IOException
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.UnknownHostException

/**
 * Abstract class for executing HTTP requests.
 *
 * @param T the type of the response object
 * @property httpClient the HTTP client used for making requests
 */
abstract class RequestExecutor<T>(private val httpClient: OkHttpClient) {
    private val log = KotlinLogging.logger {}

    /**
     * Parses the response body to an object of type [T].
     *
     * @param body the response body as a string
     * @return either an error description or the parsed response object
     */
    protected abstract fun parseResponse(body: String): Either<String, T>

    /**
     * Executes the given HTTP request.
     *
     * @param request the HTTP request to execute
     * @return either an error description or the response object
     */
    fun executeRequest(request: Request): Either<String, T> {
        runCatching {
            httpClient.newCall(request).execute().use { response ->
                log.debug { "Received response: $response" }
                if (response.isSuccessful) {
                    response.body?.use { body ->
                        return parseResponse(body.string())
                    }
                } else {
                    log.warn { "Request $request failed: ${response.code} ${response.message}" }
                    return Either.Left("Request failed: ${response.code} ${response.message}")
                }
            }
        }.onFailure {
            val errorMessage = "Failed to execute request due to: ${it.message ?: "unknown error"}"
            log.error { it.printStackTrace() }
            return when (it) {
                is UnknownHostException -> Either.Left("$errorMessage: can't connect to remote service")
                is IOException -> Either.Left("$errorMessage: network error")
                else -> Either.Left(errorMessage)
            }
        }

        // Everything should be handled earlier
        return Either.Left("Failed to execute request, try again later")
    }
}