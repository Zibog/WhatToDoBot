package com.dsidak.weather

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class WeatherResponse {
    abstract val cityName: String
    abstract val country: String
    abstract val coordinates: Coordinates
    abstract val weather: List<Weather>
    abstract val main: Main
    abstract val wind: Wind
    abstract val clouds: Clouds
    abstract val dt: Long
    abstract val timezone: Int
}

@Serializable
data class CurrentWeatherResponse(
    @SerialName("coord")
    override val coordinates: Coordinates,
    override val weather: List<Weather>,
    override val main: Main,
    // Visibility, meter. The maximum value of the visibility is 10 km
    val visibility: Int,
    override val wind: Wind,
    override val clouds: Clouds,
    // Time of data calculation, unix, UTC
    override val dt: Long,
    val sys: Sys,
    // Shift in seconds from UTC
    override val timezone: Int,
    // City name
    @SerialName("name")
    override val cityName: String
) : WeatherResponse() {
    init {
        require(cityName.isNotEmpty()) { "City name must not be empty" }
    }

    override val country: String
        get() = sys.country
}

@Serializable
data class ForecastWeatherResponse(
    @SerialName("list")
    val forecast: List<Forecast>,
    val city: City
) : WeatherResponse() {
    override val cityName: String
        get() = city.name
    override val country: String
        get() = city.country
    override val coordinates: Coordinates
        get() = city.coord
    override val weather: List<Weather>
        get() = forecast.last().weather
    override val main: Main
        get() = forecast.last().main
    override val wind: Wind
        get() = forecast.last().wind
    override val clouds: Clouds
        get() = forecast.last().clouds
    override val dt: Long
        get() = forecast.last().dt
    override val timezone: Int
        get() = city.timezone
}

@Serializable
data class Forecast(
    // Time of data forecasted, unix, UTC
    val dt: Long,
    val main: Main,
    val weather: List<Weather>,
    val clouds: Clouds,
    val wind: Wind
)

@Serializable
data class City(
    // City ID
    val id: Int,
    // City name
    val name: String,
    val coord: Coordinates,
    val country: String,
    // Shift in seconds from UTC
    val timezone: Int
)

@Serializable
data class Coordinates(
    // Longitude of the location
    @SerialName("lon")
    val longitude: Double,
    // Latitude of the location
    @SerialName("lat")
    val latitude: Double
)

@Serializable
data class Weather(
    // Weather condition id. https://openweathermap.org/weather-conditions#Weather-Condition-Codes-2
    val id: Int,
    // Group of weather parameters (Rain, Snow, Clouds etc.)
    val main: String,
    // Weather condition within the group
    val description: String,
    // Weather icon id
    val icon: String
)

@Serializable
data class Main(
    // Temperature
    @SerialName("temp")
    val temperature: Double,
    // Temperature. This temperature parameter accounts for the human perception of weather
    @SerialName("feels_like")
    val feelsLike: Double,
    // Humidity, %
    val humidity: Int
)

@Serializable
data class Wind(
    // Wind speed, m/s
    val speed: Double,
    // Wind direction, degrees (meteorological)
    @SerialName("deg")
    val degree: Int,
    // Wind gust, m/s
    val gust: Double? = null
)

@Serializable
data class Clouds(
    // Cloudiness, %
    val all: Int
)

@Serializable
data class Sys(
    // Country code (GB, JP etc.)
    val country: String,
    // Sunrise time, unix, UTC
    val sunrise: Long,
    // Sunset time, unix, UTC
    val sunset: Long
)