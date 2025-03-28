package base

import okhttp3.*
import okhttp3.internal.http.RealResponseBody
import okio.Buffer
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

abstract class HttpTestBase : ResourceTestBase {
    protected val httpClient: OkHttpClient = mock()
    protected val call: Call = mock()

    protected fun mockResponse(
        responseBody: String? = null,
        request: Request? = null,
        code: Int = 200,
        message: String = ""
    ) {
        val body = if (responseBody == null) {
            createEmptyBody()
        } else {
            RealResponseBody(
                "application/json",
                responseBody.length.toLong(),
                Buffer().readFrom(responseBody.byteInputStream())
            )
        }
        val response = Response.Builder()
            .request(request ?: DEFAULT_REQUEST)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .body(body)
            .message(message)
            .build()

        whenever(httpClient.newCall(any())).thenReturn(call)
        whenever(call.execute()).thenReturn(response)
    }

    protected fun mockResponse(
        responseBody: String? = null,
        request: Request? = null,
        code: Int = 200,
        message: String = "",
        httpClient: OkHttpClient
    ) {
        val body = if (responseBody == null) {
            createEmptyBody()
        } else {
            RealResponseBody(
                "application/json",
                responseBody.length.toLong(),
                Buffer().readFrom(responseBody.byteInputStream())
            )
        }
        val response = Response.Builder()
            .request(request ?: DEFAULT_REQUEST)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .body(body)
            .message(message)
            .build()

        val call: Call = mock()
        whenever(httpClient.newCall(any())).thenReturn(call)
        whenever(call.execute()).thenReturn(response)
    }

    private fun createEmptyBody(): ResponseBody {
        return RealResponseBody(
            "application/json",
            EMPTY_CONTENT.length.toLong(),
            Buffer().readFrom(EMPTY_CONTENT.byteInputStream())
        )
    }

    companion object {
        private const val EMPTY_CONTENT = "[]"
        val DEFAULT_REQUEST = Request.Builder().url("http://test.com").build()
    }
}