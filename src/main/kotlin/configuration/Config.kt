package com.dsidak.configuration

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addResourceSource

data class Config(
    val weather: Weather,

    val lowerBound: Long,
    val upperBound: Long,

    val database: Database,

    val gemini: Gemini,

    val requestTimeout: Long
)

val config by lazy {
    ConfigLoaderBuilder.default()
        .addResourceSource("/default.yaml")
        .build()
        .loadConfigOrThrow<Config>()
}

data class Weather(
    val weatherHost: String,
    val weatherPath: String,
    val weatherForecast: String,
    val weatherCurrent: String,
    val weatherUnits: String,
    val geoPath: String
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