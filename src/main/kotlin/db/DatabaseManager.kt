package com.dsidak.db

import com.dsidak.db.schemas.LocationService
import com.dsidak.db.schemas.UserLocationService
import com.dsidak.db.schemas.UserService
import org.jetbrains.exposed.sql.Database

object DatabaseManager {
    private val database = Database.connect(
        url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
        user = "root",
        driver = "org.h2.Driver",
        password = ""
    )
    val locationService = LocationService(database)
    val userService = UserService(database)
    val userLocationService = UserLocationService(database)
}