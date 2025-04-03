package com.dsidak.http

import com.dsidak.exception.RequestFailureException
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
     * @return parsed response object
     */
    protected abstract fun parseResponse(body: String): T

    /**
     * Executes the given HTTP request.
     *
     * @param request the HTTP request to execute
     * @return response object of type [T]
     */
    @Throws(RequestFailureException::class)
    fun executeRequest(request: Request): T {
        runCatching {
            httpClient.newCall(request).execute().use { response ->
                log.debug { "Received response: $response" }
                if (response.isSuccessful) {
                    response.body?.use { body ->
                        return parseResponse(body.string())
                    }
                } else {
                    log.warn { "Request $request failed: ${response.code} ${response.message}" }
                    throw RequestFailureException("Request failed: ${response.code} ${response.message}")
                }
            }
        }.onFailure {
            val errorMessage = "Failed to execute request due to: ${it.message ?: "unknown error"}"
            log.error(it) { it.printStackTrace() }
            throw when (it) {
                is UnknownHostException -> RequestFailureException("$errorMessage: can't connect to remote service", it)
                is IOException -> RequestFailureException("$errorMessage: network error", it)
                is RequestFailureException -> it
                else -> RequestFailureException(errorMessage, it)
            }
        }

        // Everything should be handled earlier
        throw RequestFailureException("Failed to execute request, try again later")
    }
}