package com.dsidak.openmeteo

class Fetcher(private val type: RequestType, private val ctx: RequestContext) {
    fun fetch(): String {
        // TODO: make request to API, then to chatbot, then return result
        return "Let's do some code!"
    }

    private fun toUrl(): String {
        return "${ctx.url}/${type.name}?${constructParams()}"
    }

    private fun constructParams(): String {
        return when (type) {
            RequestType.FORECAST -> "latitude=${ctx.latitude}&longitude=${ctx.longitude}"
        }
    }
}