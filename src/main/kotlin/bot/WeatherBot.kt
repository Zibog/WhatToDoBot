package com.dsidak.bot

import com.dsidak.dotenv
import com.google.common.base.Predicates
import io.github.oshai.kotlinlogging.KotlinLogging
import org.telegram.telegrambots.abilitybots.api.bot.AbilityBot
import org.telegram.telegrambots.abilitybots.api.db.MapDBContext
import org.telegram.telegrambots.abilitybots.api.objects.Ability
import org.telegram.telegrambots.abilitybots.api.objects.Locality
import org.telegram.telegrambots.abilitybots.api.objects.Privacy
import org.telegram.telegrambots.abilitybots.api.sender.SilentSender
import org.telegram.telegrambots.meta.generics.TelegramClient

/**
 * Telegram bot for providing weather information and recommendations based on weather data.
 * Converts city names to coordinates and fetches weather data, then generates content using the Gemini API.
 */
class WeatherBot(
    telegramClient: TelegramClient,
    botUsername: String,
    private val commandHandler: CommandHandler = CommandHandler()
) : AbilityBot(
    telegramClient, botUsername, MapDBContext.offlineInstance("${botUsername}DB")
) {
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
     * Creates an ability that provides weather information for a specified date.
     * If no date is provided, it defaults to today.
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
                val responseToUser = commandHandler.handleWeatherCommand(ctx)
                log.debug { "user=${ctx.user().userName}, chatId=${ctx.chatId()}, response=$responseToUser" }
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
                val responseToUser = commandHandler.handleLocationCommand(ctx)
                log.debug { "user=${ctx.user().userName}, chatId=${ctx.chatId()}, response=$responseToUser" }
                silent.sendMd(responseToUser, ctx.chatId())
            }
            .build()
    }

    /**
     * Creates an ability that drops the user's location.
     *
     * @return the restart ability
     */
    @Suppress("unused")
    fun restartCommand(): Ability {
        return Ability.builder()
            .name("restart")
            .info("Drops your location")
            .privacy(Privacy.PUBLIC)
            .locality(Locality.ALL)
            .action { ctx ->
                val responseToUser = commandHandler.handleRestartCommand(ctx)
                log.debug { "user=${ctx.user().userName}, chatId=${ctx.chatId()}, response=$responseToUser" }
                silent.sendMd(responseToUser, ctx.chatId())
            }
            .build()
    }

    /**
     * Creates an ability that shows the list of available commands with usages.
     *
     * @return the help ability
     */
    @Suppress("unused")
    fun helpCommand(): Ability {
        return Ability.builder()
            .name("help")
            .info("Shows the list of available commands")
            .privacy(Privacy.PUBLIC)
            .locality(Locality.ALL)
            .action { ctx ->
                val responseToUser = commandHandler.handleHelpCommand()
                log.debug { "user=${ctx.user().userName}, chatId=${ctx.chatId()}, response=$responseToUser" }
                silent.sendMd(responseToUser, ctx.chatId())
            }
            .build()
    }

    /**
     * Creates an ability that handles non-command input.
     *
     * @return the default ability
     */
    @Suppress("unused")
    fun defaultCommand(): Ability {
        return Ability.builder()
            .name(DEFAULT)
            .flag(Predicates.alwaysTrue())
            .privacy(Privacy.PUBLIC)
            .locality(Locality.ALL)
            .action { ctx ->
                val responseToUser = commandHandler.handleDefault()
                log.debug { "user=${ctx.user().userName}, chatId=${ctx.chatId()}, response=$responseToUser" }
                silent.sendMd(responseToUser, ctx.chatId())
            }
            .build()
    }
}