package com.dsidak.db.schemas

import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

@Serializable
data class UserLocation(
    val userId: Long,
    @Contextual
    val locationId: EntityID<Long>
)

class UserLocationService(db: Database) {
    object UserLocationsTable : LongIdTable() {
        val userId = reference("user_id", UserService.UsersTable.id)
        val locationId = reference("location_id", LocationService.LocationsTable.id)
    }

    init {
        transaction(db) {
            SchemaUtils.create(UserLocationsTable)
        }
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    suspend fun create(userLocation: UserLocation): EntityID<Long> = dbQuery {
        UserLocationsTable.insert {
            it[userId] = userLocation.userId
            it[locationId] = userLocation.locationId
        }[UserLocationsTable.id]
    }

    suspend fun read(id: EntityID<Long>): UserLocation? {
        return dbQuery {
            UserLocationsTable.selectAll()
                .where { UserLocationsTable.id eq id }
                .map { UserLocation(it[UserLocationsTable.userId], it[UserLocationsTable.locationId]) }
                .singleOrNull()
        }
    }

    suspend fun update(id: EntityID<Long>, userLocation: UserLocation) {
        dbQuery {
            UserLocationsTable.update({ UserLocationsTable.id eq id }) {
                it[userId] = userLocation.userId
                it[locationId] = userLocation.locationId
            }
        }
    }

    suspend fun delete(id: EntityID<Long>) {
        dbQuery {
            UserLocationsTable.deleteWhere { UserLocationsTable.id.eq(id) }
        }
    }
}