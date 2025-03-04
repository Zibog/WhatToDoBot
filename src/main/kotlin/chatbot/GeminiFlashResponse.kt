package com.dsidak.chatbot

import kotlinx.serialization.Serializable

@Serializable
data class GeminiFlashResponse(
    val candidates: List<Candidate>
)

@Serializable
data class Candidate(
    val content: Content,
    val finishReason: FinishReason
)

@Serializable
enum class FinishReason {
    FINISH_REASON_UNSPECIFIED,
    STOP, // Natural stop
    MAX_TOKENS, // Truncated due to max tokens
    SAFETY, // Blocked by safety
    RECITATION, // Blocked for reciting training data
    OTHER // Another unspecified reason.
}