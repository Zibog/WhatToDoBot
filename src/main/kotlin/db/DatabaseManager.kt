package com.dsidak.db

import com.dsidak.config.appConfig
import com.dsidak.db.schemas.LocationService
import com.dsidak.db.schemas.UserLocationService
import com.dsidak.db.schemas.UserService
import org.jetbrains.exposed.sql.Database

object DatabaseManager {
    private val database = Database.connect(
        url = appConfig.database.dbHost,
        user = appConfig.database.dbUser,
        driver = appConfig.database.dbDriver,
        password = appConfig.database.dbPassword
    )
    val locationService = LocationService(database)
    val userService = UserService(database)
    val userLocationService = UserLocationService(database)
}