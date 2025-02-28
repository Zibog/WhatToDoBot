package com.dsidak.configuration

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addResourceSource

data class Config(
    val weather: Weather,

    val lowerBound: Long,
    val upperBound: Long,

    val database: Database,

    val gemini: Gemini
)

val config by lazy {
    ConfigLoaderBuilder.default()
        .addResourceSource("/default.yaml")
        .build()
        .loadConfigOrThrow<Config>()
}

data class Weather(
    val weatherApiUrl: String,
    val weatherForecast: String,
    val weatherCurrent: String,
    val weatherUnits: String
)

data class Database(
    val dbDriver: String,
    val dbHost: String,
    val dbUser: String,
    val dbPassword: String
)

data class Gemini(
    val modelUrl: String,
    val modelName: String,
    val modelAction: String
)