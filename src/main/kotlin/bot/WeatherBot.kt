package com.dsidak.bot

import arrow.core.Either
import com.dsidak.chatbot.GeminiClient
import com.dsidak.configuration.config
import com.dsidak.db.DatabaseManager
import com.dsidak.db.schemas.Location
import com.dsidak.db.schemas.User
import com.dsidak.dotenv
import com.dsidak.geocoding.CityInfo
import com.dsidak.geocoding.GeocodingFetcher
import com.dsidak.weather.WeatherFetcher
import com.google.common.base.Predicates
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import org.telegram.telegrambots.abilitybots.api.bot.AbilityBot
import org.telegram.telegrambots.abilitybots.api.db.MapDBContext
import org.telegram.telegrambots.abilitybots.api.objects.Ability
import org.telegram.telegrambots.abilitybots.api.objects.Locality
import org.telegram.telegrambots.abilitybots.api.objects.Privacy
import org.telegram.telegrambots.abilitybots.api.sender.SilentSender
import org.telegram.telegrambots.meta.generics.TelegramClient
import java.time.LocalDate
import java.util.*

class WeatherBot :
    AbilityBot {
    constructor(telegramClient: TelegramClient, botUsername: String) : super(
        telegramClient,
        botUsername,
        MapDBContext.offlineInstance("${botUsername}DB")
    ) {
        this.weatherFetcher = WeatherFetcher()
        this.geocodingFetcher = GeocodingFetcher()
        this.geminiClient = GeminiClient()
    }

    constructor(
        telegramClient: TelegramClient,
        botUsername: String,
        weatherFetcher: WeatherFetcher,
        geocodingFetcher: GeocodingFetcher,
        geminiClient: GeminiClient
    ) : super(
        telegramClient,
        botUsername,
        MapDBContext.offlineInstance("${botUsername}DB")
    ) {
        this.weatherFetcher = weatherFetcher
        this.geocodingFetcher = geocodingFetcher
        this.geminiClient = geminiClient
    }

    private val log = KotlinLogging.logger {}
    private val weatherFetcher: WeatherFetcher
    private val geocodingFetcher: GeocodingFetcher
    private val geminiClient: GeminiClient

    /**
     * Returns the creator ID of the bot.
     *
     * @return the creator ID
     */
    override fun creatorId(): Long {
        return dotenv["BOT_CREATOR_ID"].toLong()
    }

    /**
     * Sets the silent sender for the bot. Intended to be used only for testing purposes.
     *
     * @param silentSender the silent sender to be set
     */
    internal fun setSilentSender(silentSender: SilentSender) {
        this.silent = silentSender
    }

    /**
     * Creates an ability that provides weather information for a specified date.
     *
     * @return the weather ability
     */
    @Suppress("unused")
    fun weatherCommand(): Ability {
        return Ability.builder()
            .name("weather")
            .info("Request weather for the day")
            .privacy(Privacy.PUBLIC)
            .locality(Locality.ALL)
            .action { ctx ->
                val inputArg = ctx.arguments().getOrElse(0) { "today" }

                log.debug { "Going to parse arg=$inputArg" }
                val dateWithOffset = offsetDate(LocalDate.now(), inputArg)
                if (dateWithOffset == LocalDate.EPOCH) {
                    silent.send("Invalid argument '$inputArg'. Please, provide valid offset", ctx.chatId())
                    return@action
                }

                log.debug { "Check the weather for $dateWithOffset" }
                val location = runBlocking {
                    val tgUser = ctx.user()
                    DatabaseManager.createOrReadUser(
                        User(
                            tgUser.id,
                            tgUser.firstName,
                            tgUser.lastName,
                            tgUser.userName
                        )
                    )
                    readLocation(ctx.user().id)
                }
                if (location.isEmpty) {
                    silent.send("Please, provide your location first using /location <city>, [country]", ctx.chatId())
                    return@action
                }

                log.debug { "Location is $location" }

                val weatherResponse = try {
                    weatherFetcher.fetchWeather(location.get().city, dateWithOffset)
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
                log.debug { "Response to user: $responseToUser" }
                silent.sendMd(responseToUser, ctx.chatId())
            }
            .build()
    }

    /**
     * Creates an ability that sets the location for weather requests.
     *
     * @return the location ability
     */
    @Suppress("unused")
    fun locationCommand(): Ability {
        return Ability.builder()
            .name("location")
            .info("Set location for the request")
            .privacy(Privacy.PUBLIC)
            .locality(Locality.ALL)
            .action { ctx ->
                val args = extractCityAndCountry(ctx.arguments())
                if (args == null) {
                    silent.send("Sorry, this feature requires 1 or 2 additional inputs.", ctx.chatId())
                    return@action
                }
                val city = args.first
                val country = args.second
                log.debug { "Received location=$city, $country" }
                // TODO: check if the location is already in DB
                val cityInfo = geocodingFetcher.fetchCoordinates(city, country)
                if (cityInfo == CityInfo.EMPTY) {
                    silent.send("No results found for the city $city. Try to specify the country", ctx.chatId())
                    return@action
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
                val helperMessage =
                    "Location is set to ${cityInfo.name}, ${cityInfo.country}. If location is wrong, please state city and two-letter-length country code separated by comma."
                if (previousLocation.isEmpty) {
                    silent.send(helperMessage, ctx.chatId())
                } else {
                    silent.send("Location updated from ${previousLocation.get().city} to $city", ctx.chatId())
                    silent.send(helperMessage, ctx.chatId())
                }
            }
            .build()
    }

    private fun extractCityAndCountry(args: Array<String>): Pair<String, String>? {
        return when (args.size) {
            1 -> Pair(args[0].removeSuffix(","), "")
            2 -> Pair(args[0].removeSuffix(","), args[1])
            else -> null
        }
    }

    /**
     * Reads the location for a given user ID.
     *
     * @param userId the user ID
     * @return an [Optional] containing the location if it exists, otherwise [Optional.empty]
     */
    private suspend fun readLocation(userId: Long): Optional<Location> {
        val user = DatabaseManager.userService.read(userId)!!
        return if (user.locationId != null) {
            Optional.of(DatabaseManager.locationService.read(user.locationId)!!)
        } else {
            Optional.empty()
        }
    }

    /**
     * Updates the location for a given user ID.
     *
     * @param userId the user ID
     * @param location the new location
     * @return an [Optional] containing the previous location if it existed, otherwise [Optional.empty]
     */
    private suspend fun updateLocation(userId: Long, location: CityInfo?): Optional<Location> {
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
        return Optional.ofNullable(oldLocation)
    }

    @Suppress("unused")
    fun restartCommand(): Ability {
        return Ability.builder()
            .name("restart")
            .info("Drops your location")
            .privacy(Privacy.PUBLIC)
            .locality(Locality.ALL)
            .action { ctx ->
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
                if (previousLocation.isEmpty) {
                    silent.send("No location was set", ctx.chatId())
                } else {
                    silent.send("Location dropped from ${previousLocation.get().city}", ctx.chatId())
                }
            }
            .build()
    }

    @Suppress("unused")
    fun helpCommand(): Ability {
        return Ability.builder()
            .name("help")
            .info("Shows the list of available commands")
            .privacy(Privacy.PUBLIC)
            .locality(Locality.ALL)
            .action { ctx ->
                val message = """
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
                silent.sendMd(message, ctx.chatId())
            }
            .build()
    }

    @Suppress("unused")
    fun defaultCommand(): Ability {
        return Ability.builder()
            .name(DEFAULT)
            .flag(Predicates.alwaysTrue())
            .privacy(Privacy.PUBLIC)
            .locality(Locality.ALL)
            .action { ctx ->
                val message = """This bot works only with commands. To check them, use /commands or /help"""
                silent.sendMd(message, ctx.chatId())
            }
            .build()
    }

    companion object {
        /**
         * Parses the argument and returns the offset date based on the argument.
         *
         * @param date the base date
         * @param arg the argument specifying the offset
         * @return the offset date or [LocalDate.EPOCH] if the argument is invalid
         */
        internal fun offsetDate(date: LocalDate, arg: String): LocalDate {
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

            return LocalDate.EPOCH
        }
    }
}