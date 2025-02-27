package com.dsidak.db.schemas

import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

@Serializable
data class UserLocation(
    val userId: Int,
    val locationId: Int
)

class UserLocationService(db: Database) {
    object UserLocations : Table() {
        val id = integer("id").autoIncrement()
        val userId = reference("user_id", UserService.Users.id)
        val locationId = reference("location_id", LocationService.Locations.id)

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(db) {
            SchemaUtils.create(UserLocations)
        }
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    suspend fun create(userLocation: UserLocation): Int = dbQuery {
        UserLocations.insert {
            it[userId] = userLocation.userId
            it[locationId] = userLocation.locationId
        }[UserLocations.id]
    }

    suspend fun read(id: Int): UserLocation? {
        return dbQuery {
            UserLocations.selectAll()
                .where { UserLocations.id eq id }
                .map { UserLocation(it[UserLocations.userId], it[UserLocations.locationId]) }
                .singleOrNull()
        }
    }

    suspend fun update(id: Int, userLocation: UserLocation) {
        dbQuery {
            UserLocations.update({ UserLocations.id eq id }) {
                it[userId] = userLocation.userId
                it[locationId] = userLocation.locationId
            }
        }
    }

    suspend fun delete(id: Int) {
        dbQuery {
            UserLocations.deleteWhere { UserLocations.id.eq(id) }
        }
    }
}