package com.dsidak.chatbot

import kotlinx.serialization.Serializable

@Serializable
data class GeminiFlashRequest(
    val contents: List<Content>
)

@Serializable
data class Content(
    // "user" or "model" (optional, usually "user" for requests)
    val role: String? = null,
    val parts: List<ContentPart>
)

@Serializable
data class ContentPart(
    val text: String
)