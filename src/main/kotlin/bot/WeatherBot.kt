package com.dsidak.bot

import com.dsidak.configuration.config
import com.dsidak.dotenv
import com.dsidak.weather.Fetcher
import com.google.common.base.Predicates
import io.github.oshai.kotlinlogging.KotlinLogging
import org.telegram.telegrambots.abilitybots.api.bot.AbilityBot
import org.telegram.telegrambots.abilitybots.api.db.DBContext
import org.telegram.telegrambots.abilitybots.api.db.MapDBContext.onlineInstance
import org.telegram.telegrambots.abilitybots.api.objects.Ability
import org.telegram.telegrambots.abilitybots.api.objects.Locality
import org.telegram.telegrambots.abilitybots.api.objects.Privacy
import org.telegram.telegrambots.abilitybots.api.sender.SilentSender
import org.telegram.telegrambots.meta.generics.TelegramClient
import java.time.LocalDate
import java.util.*

class WeatherBot(telegramClient: TelegramClient, botUsername: String, db: DBContext = onlineInstance(botUsername)) :
    AbilityBot(telegramClient, botUsername, db) {
    private val log = KotlinLogging.logger {}

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
     * Creates an ability that responds with "Hello world!".
     *
     * @return the hello world ability
     */
    fun helloWorldCommand(): Ability {
        return Ability.builder()
            .name("hello")
            .info("Just says hello world")
            .privacy(Privacy.PUBLIC)
            .locality(Locality.ALL)
            .action { ctx -> silent.send("Hello world!", ctx.chatId()) }
            .build()
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
                val location = readLocation(ctx.user().id)
                if (location.isEmpty) {
                    silent.send("Please, provide your location first using /location <city>", ctx.chatId())
                    return@action
                }

                log.debug { "Location is $location" }

                val responseToUser = try {
                    Fetcher().fetchWeather(location.get(), dateWithOffset)
                } catch (e: IllegalArgumentException) {
                    "Wrong input data: ${e.message}"
                } catch (e: Exception) {
                    "Unexpected error: ${e.message}"
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
            .input(1)
            .action { ctx ->
                val inputArg = ctx.arguments()[0]
                log.debug { "Received location=$inputArg" }
                val previousLocation = updateLocation(ctx.user().id, inputArg)
                if (previousLocation.isEmpty) {
                    silent.send("Location set to $inputArg", ctx.chatId())
                } else {
                    silent.send("Location updated from ${previousLocation.get()} to $inputArg", ctx.chatId())
                }
            }
            .build()
    }

    /**
     * Reads the location for a given user ID.
     *
     * @param userId the user ID
     * @return an [Optional] containing the location if it exists, otherwise [Optional.empty]
     */
    private fun readLocation(userId: Long): Optional<String> {
        val locations = db.getMap<Long, String>("LOCATIONS")
        return Optional.ofNullable(locations[userId])
    }

    /**
     * Updates the location for a given user ID.
     *
     * @param userId the user ID
     * @param location the new location
     * @return an [Optional] containing the previous location if it existed, otherwise [Optional.empty]
     */
    private fun updateLocation(userId: Long, location: String): Optional<String> {
        val locations = db.getMap<Long, String>("LOCATIONS")
        return Optional.ofNullable(locations.put(userId, location))
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
                    |1. Set your location using `/location <city>`
                    |This command is also used to update location.
                    |Examples: `/location Sofia`, `/location Moscow`
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
            .info("Handles text messages")
            .flag(Predicates.alwaysTrue())
            .privacy(Privacy.ADMIN)
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