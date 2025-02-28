package com.dsidak.db

import com.dsidak.configuration.config
import com.dsidak.db.schemas.LocationService
import com.dsidak.db.schemas.UserLocationService
import com.dsidak.db.schemas.UserService
import org.jetbrains.exposed.sql.Database

object DatabaseManager {
    private val database = Database.connect(
        url = config.database.dbHost,
        user = config.database.dbUser,
        driver = config.database.dbDriver,
        password = config.database.dbPassword
    )
    val locationService = LocationService(database)
    val userService = UserService(database)
    val userLocationService = UserLocationService(database)
}