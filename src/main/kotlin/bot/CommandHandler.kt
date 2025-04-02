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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.telegram.telegrambots.abilitybots.api.objects.MessageContext
import java.time.LocalDate

class CommandHandler(
    private val weatherFetcher: WeatherFetcher = WeatherFetcher(),
    private val geocodingFetcher: GeocodingFetcher = GeocodingFetcher(),
    private val geminiClient: GeminiClient = GeminiClient()
) {
    private val log = KotlinLogging.logger {}

    internal suspend fun handleWeatherCommand(ctx: MessageContext): String = coroutineScope {
        val inputArg = ctx.arguments().getOrElse(0) { "today" }

        log.debug { "Going to parse offset arg=$inputArg" }
        val dateWithOffset = offsetDate(LocalDate.now(), inputArg)
        if (!isValidWeatherArg(inputArg) || dateWithOffset == null) {
            return@coroutineScope "Invalid argument '$inputArg'. Please, provide a valid offset"
        }

        log.debug { "Check the weather for date=$dateWithOffset" }
        val location = readLocation(ctx.user().id)
            ?: return@coroutineScope "Please, provide your location first using /location <city>, [country]"

        log.debug { "Location is $location" }

        val weatherResponse = try {
            async { weatherFetcher.fetchWeather(location.city, dateWithOffset) }.await()
        } catch (e: IllegalArgumentException) {
            Either.Left("Wrong input data: ${e.message}")
        } catch (e: Exception) {
            Either.Left("Unexpected error: ${e.message}")
        }

        val responseToUser = if (weatherResponse.isLeft()) {
            weatherResponse.leftOrNull()!!
        } else {
            async { geminiClient.generateContent(weatherResponse.getOrNull()!!, dateWithOffset) }.await()
        }
        return@coroutineScope responseToUser
    }

    /**
     * Reads the location for a given user ID.
     *
     * @param userId the user ID
     * @return a [Location] containing the location of the user if it exists in DB, otherwise null
     */
    private suspend fun readLocation(userId: Long): Location? = withContext(Dispatchers.IO) {
        val user = DatabaseManager.userService.read(userId) ?: return@withContext null
        return@withContext if (user.locationId != null) {
            DatabaseManager.locationService.read(user.locationId)
        } else {
            null
        }
    }

    internal suspend fun handleLocationCommand(ctx: MessageContext): String = coroutineScope {
        async { DatabaseManager.createOrReadUser(ctx.user().toUser()) }.await()
        if (ctx.arguments().isEmpty()) {
            val location = readLocation(ctx.user().id) ?: return@coroutineScope "No location was set"
            return@coroutineScope "Your current location is ${location.city}, ${location.country}"
        }
        val args = extractCityAndCountry(ctx.arguments())
        if (args == null || !isValidLocationArgs(args)) {
            return@coroutineScope "Sorry, this feature requires 1 or 2 additional inputs"
        }
        val city = args.first
        val country = args.second
        log.debug { "Received location=$city, $country" }
        // TODO: check if the location is already in DB
        val cityInfo = async { geocodingFetcher.fetchCoordinates(city, country) }.await()
        if (cityInfo == CityInfo.EMPTY) {
            return@coroutineScope "No results found for the city $city. Try to specify the country"
        }
        log.info { "Checked geolocation of $city: $cityInfo" }
        val previousLocation = async { updateLocation(ctx.user().id, cityInfo) }.await()
        val action = if (previousLocation == null) {
            "set"
        } else {
            "updated from ${previousLocation.city}, ${previousLocation.country}"
        }
        return@coroutineScope "Location $action to ${cityInfo.name}, ${cityInfo.country}. If location is wrong, set it using `/location <city>, <country>`"
    }

    /**
     * Updates the location for a given user ID.
     *
     * @param userId the user ID
     * @param location the new location
     * @return a [Location] containing the previous location if it existed in DB, otherwise null
     */
    private suspend fun updateLocation(userId: Long, location: CityInfo?): Location? = withContext(Dispatchers.IO) {
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
        return@withContext oldLocation
    }

    internal suspend fun handleRestartCommand(ctx: MessageContext): String = coroutineScope {
        val previousLocation = async {
            DatabaseManager.createOrReadUser(ctx.user().toUser())
            updateLocation(ctx.user().id, null)
        }.await()
        return@coroutineScope if (previousLocation == null) {
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
            |You can use this command without arguments to check your current location.
            |Example: `/location`
            |2. Request weather for the day using `/weather [offset]`
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

        private fun isValidWeatherArg(arg: String): Boolean {
            return arg.equals("today", ignoreCase = true) || arg.equals(
                "tomorrow",
                ignoreCase = true
            ) || arg.toLongOrNull() != null
        }

        private fun isValidLocationArgs(args: Pair<String, String>): Boolean {
            return args.first.isNotBlank() && (args.second.isBlank() || args.second.length == 2)
        }

        private fun org.telegram.telegrambots.meta.api.objects.User.toUser(): User {
            return User(
                id = this.id,
                firstName = this.firstName,
                lastName = this.lastName,
                userName = this.userName
            )
        }
    }
}