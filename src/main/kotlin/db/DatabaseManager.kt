package com.dsidak.db

import com.dsidak.configuration.config
import com.dsidak.db.schemas.Location
import com.dsidak.db.schemas.LocationService
import com.dsidak.db.schemas.LocationService.LocationsTable
import com.dsidak.db.schemas.User
import com.dsidak.db.schemas.UserService
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

object DatabaseManager {
    private val database = Database.connect(
        url = config.database.dbHost,
        user = config.database.dbUser,
        driver = config.database.dbDriver,
        password = config.database.dbPassword
    )
    val locationService = LocationService(database)
    val userService = UserService(database)

    suspend fun createOrReadLocation(location: Location): Long {
        return dbQuery {
            val locationInDb = LocationsTable.selectAll()
                .where { LocationsTable.city eq location.city }
                .limit(1)
                .map {
                    Location(
                        it[LocationsTable.city],
                        it[LocationsTable.country],
                        it[LocationsTable.latitude],
                        it[LocationsTable.longitude],
                        it[LocationsTable.id]
                    )
                }
                .singleOrNull()
            return@dbQuery if (locationInDb != null) {
                locationInDb.id!!
            } else {
                val newLocationId = LocationsTable.insert {
                    it[city] = location.city
                    it[country] = location.country
                    it[latitude] = location.latitude
                    it[longitude] = location.longitude
                }[LocationsTable.id]
                return@dbQuery newLocationId
            }
        }
    }

    suspend fun createOrReadUser(user: User) {
        dbQuery {
            val userInDb = UserService.UsersTable.selectAll()
                .where { UserService.UsersTable.id eq user.id }
                .limit(1)
                .map {
                    User(
                        it[UserService.UsersTable.id],
                        it[UserService.UsersTable.firstName],
                        it[UserService.UsersTable.lastName],
                        it[UserService.UsersTable.userName],
                        it[UserService.UsersTable.locationId]
                    )
                }
                .singleOrNull()
            if (userInDb == null) {
                UserService.UsersTable.insert {
                    it[id] = user.id
                    it[firstName] = user.firstName
                    it[lastName] = user.lastName
                    it[userName] = user.userName
                    it[locationId] = user.locationId
                }
            }
        }
    }
}

suspend fun <T> dbQuery(block: suspend () -> T): T =
    newSuspendedTransaction(Dispatchers.IO) { block() }