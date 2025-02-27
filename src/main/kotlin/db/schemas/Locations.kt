package com.dsidak.db.schemas

import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

@Serializable
data class Location(
    val city: String,
    val country: String,
    val latitude: Double,
    val longitude: Double
)

class LocationService(db: Database) {
    object Locations : Table() {
        val id = integer("id").autoIncrement()
        val city = varchar("city", 50)
        val country = varchar("country", 50)
        val latitude = double("latitude")
        val longitude = double("longitude")

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(db) {
            SchemaUtils.create(Locations)
        }
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    suspend fun create(location: Location): Int = dbQuery {
        Locations.insert {
            it[city] = location.city
            it[country] = location.country
            it[latitude] = location.latitude
            it[longitude] = location.longitude
        }[Locations.id]
    }

    suspend fun read(id: Int): Location? {
        return dbQuery {
            Locations.selectAll()
                .where { Locations.id eq id }
                .map {
                    Location(
                        it[Locations.city],
                        it[Locations.country],
                        it[Locations.latitude],
                        it[Locations.longitude]
                    )
                }
                .singleOrNull()
        }
    }

    suspend fun update(id: Int, location: Location) {
        dbQuery {
            Locations.update({ Locations.id eq id }) {
                it[city] = location.city
                it[country] = location.country
                it[latitude] = location.latitude
                it[longitude] = location.longitude
            }
        }
    }

    suspend fun delete(id: Int) {
        dbQuery {
            Locations.deleteWhere { Locations.id.eq(id) }
        }
    }
}