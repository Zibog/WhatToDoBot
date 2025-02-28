package com.dsidak.config

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addResourceSource

data class AppConfig(
    val weather: Weather,

    val lowerBound: Long,
    val upperBound: Long,

    val database: Database
)

val appConfig by lazy {
    ConfigLoaderBuilder.default()
        .addResourceSource("/default.yaml")
        .build()
        .loadConfigOrThrow<AppConfig>()
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