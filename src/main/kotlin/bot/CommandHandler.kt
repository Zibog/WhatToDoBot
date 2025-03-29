package com.dsidak.bot

import arrow.core.Either
import com.dsidak.chatbot.GeminiClient
import com.dsidak.configuration.config
import com.dsidak.db.DatabaseManager
import com.dsidak.db.schemas.Location
import com.dsidak.db.schemas.User
import com.dsidak.geocoding.CityInfo
import com.dsidak.geocoding.GeocodingFetcher
import com.dsidak.weather.WeatherFetcher
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import org.telegram.telegrambots.abilitybots.api.objects.MessageContext
import java.time.LocalDate

class CommandHandler(
    private val weatherFetcher: WeatherFetcher = WeatherFetcher(),
    private val geocodingFetcher: GeocodingFetcher = GeocodingFetcher(),
    private val geminiClient: GeminiClient = GeminiClient()
) {
    private val log = KotlinLogging.logger {}

    internal fun handleWeatherCommand(ctx: MessageContext): String {
        val inputArg = ctx.arguments().getOrElse(0) { "today" }

        log.debug { "Going to parse offset arg=$inputArg" }
        val dateWithOffset = offsetDate(LocalDate.now(), inputArg)
            ?: return "Invalid argument '$inputArg'. Please, provide valid offset"

        log.debug { "Check the weather for date=$dateWithOffset" }
        val location = runBlocking {
            readLocation(ctx.user().id)
        }
        if (location == null) {
            return "Please, provide your location first using /location <city>, [country]"
        }

        log.debug { "Location is $location" }

        val weatherResponse = try {
            weatherFetcher.fetchWeather(location.city, dateWithOffset)
        } catch (e: IllegalArgumentException) {
            Either.Left("Wrong input data: ${e.message}")
        } catch (e: Exception) {
            Either.Left("Unexpected error: ${e.message}")
        }

        val responseToUser = if (weatherResponse.isLeft()) {
            weatherResponse.leftOrNull()!!
        } else {
            geminiClient.generateContent(weatherResponse.getOrNull()!!, dateWithOffset)
        }
        return responseToUser
    }

    /**
     * Reads the location for a given user ID.
     *
     * @param userId the user ID
     * @return a [Location] containing the location of the user if it exists in DB, otherwise null
     */
    private suspend fun readLocation(userId: Long): Location? {
        val user = DatabaseManager.userService.read(userId) ?: return null
        return if (user.locationId != null) {
            DatabaseManager.locationService.read(user.locationId)
        } else {
            null
        }
    }

    internal fun handleLocationCommand(ctx: MessageContext): String {
        val args =
            extractCityAndCountry(ctx.arguments()) ?: return "Sorry, this feature requires 1 or 2 additional inputs."
        val city = args.first
        val country = args.second
        log.debug { "Received location=$city, $country" }
        // TODO: check if the location is already in DB
        val cityInfo = geocodingFetcher.fetchCoordinates(city, country)
        if (cityInfo == CityInfo.EMPTY) {
            return "No results found for the city $city. Try to specify the country"
        }
        log.info { "Checked geolocation of $city: $cityInfo" }
        val previousLocation = runBlocking {
            val tgUser = ctx.user()
            DatabaseManager.createOrReadUser(
                User(
                    tgUser.id,
                    tgUser.firstName,
                    tgUser.lastName,
                    tgUser.userName
                )
            )
            updateLocation(ctx.user().id, cityInfo)
        }
        val action = if (previousLocation == null) {
            "set"
        } else {
            "updated from ${previousLocation.city}, ${previousLocation.country}"
        }
        return "Location $action to ${cityInfo.name}, ${cityInfo.country}. If location is wrong, set it using /location <city>, <country>."
    }

    /**
     * Extracts the city and country from the arguments.
     *
     * @param args the arguments array
     * @return [Pair] containing the city and country, or null if the arguments are invalid
     */
    private fun extractCityAndCountry(args: Array<String>): Pair<String, String>? {
        return when (args.size) {
            1 -> Pair(args[0].removeSuffix(","), "")
            2 -> Pair(args[0].removeSuffix(","), args[1])
            else -> null
        }
    }

    /**
     * Updates the location for a given user ID.
     *
     * @param userId the user ID
     * @param location the new location
     * @return a [Location] containing the previous location if it existed in DB, otherwise null
     */
    private suspend fun updateLocation(userId: Long, location: CityInfo?): Location? {
        val user = DatabaseManager.userService.read(userId)!!
        val oldLocation = if (user.locationId != null) {
            DatabaseManager.locationService.read(user.locationId)
        } else {
            null
        }

        if (location != null) {
            val newLocationId = DatabaseManager.createOrReadLocation(
                Location(
                    city = location.name,
                    country = location.country,
                    latitude = location.latitude,
                    longitude = location.longitude
                )
            )
            DatabaseManager.userService.update(userId, user.copy(locationId = newLocationId))
        }
        return oldLocation
    }

    internal fun handleRestartCommand(ctx: MessageContext): String {
        val previousLocation = runBlocking {
            val tgUser = ctx.user()
            DatabaseManager.createOrReadUser(
                User(
                    tgUser.id,
                    tgUser.firstName,
                    tgUser.lastName,
                    tgUser.userName
                )
            )
            updateLocation(ctx.user().id, null)
        }
        return if (previousLocation == null) {
            "No location was set"
        } else {
            "Location dropped from ${previousLocation.city}"
        }
    }

    internal fun handleHelpCommand(): String {
        return """
            |How to use this bot?
            |
            |1. Set your location using `/location <city>, [country]`
            |This command is also used to update location.
            |The country is optional and should be a two-letter-length code.
            |Examples: `/location Sofia`, `/location Moscow`, `/location London, GB`
            |2. Request weather for the day using `/weather <offset>`
            |The offset can be a number from 0 to 5, *today* or *tomorrow*, where *today* is the default value.
            |The offset is the number of days from today, where 0 is today, 1 is tomorrow, etc.
            |Examples: `/weather 0`, `/weather today`, `/weather tomorrow`, `/weather 3`
            |
            |Optional commands (no arguments needed):
            |/restart drops your location
            |/help shows this instruction :)
            |/commands lists you with supported commands with short descriptions
            """.trimMargin()
    }

    internal fun handleDefault(): String {
        return """This bot works only with commands. To check them, use /commands or /help"""
    }

    companion object {
        /**
         * Parses the argument and returns the offset date based on the argument.
         *
         * @param date the base date
         * @param arg the argument specifying the offset
         * @return the offset date or null if the argument is invalid
         */
        internal fun offsetDate(date: LocalDate, arg: String): LocalDate? {
            if (arg.equals("today", ignoreCase = true)) {
                return date
            }
            if (arg.equals("tomorrow", ignoreCase = true)) {
                return date.plusDays(1)
            }

            val long = arg.toLongOrNull()
            if (long != null && long in config.lowerBound..config.upperBound) {
                return date.plusDays(long)
            }

            return null
        }
    }
}