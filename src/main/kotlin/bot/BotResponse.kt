package com.dsidak.bot

interface BotResponse {
    val status: Status

    override fun toString(): String

    enum class Status {
        SUCCESS,
        ERROR
    }

    data class Success(
        val message: String
    ) : BotResponse {
        override val status: Status = Status.SUCCESS

        override fun toString(): String {
            return message
        }
    }

    data class FullResponse(
        val forecast: String,
        val recommendation: String,
    ) : BotResponse {
        override val status: Status = Status.SUCCESS

        override fun toString(): String {
            return """
            |$forecast
            |
            |$recommendation
        """.trimMargin()
        }
    }

    data class Error(
        val message: String
    ) : BotResponse {
        override val status: Status = Status.ERROR

        override fun toString(): String {
            return message
        }
    }
}


