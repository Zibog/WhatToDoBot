package com.dsidak.db.schemas

import com.dsidak.db.dbQuery
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

@Serializable
data class Location(
    val city: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val id: Long? = null
)

class LocationService(db: Database) {
    object LocationsTable : Table() {
        val id = long("id").autoIncrement()
        val city = varchar("city", 50)
        val country = varchar("country", 2)
        val latitude = double("latitude")
        val longitude = double("longitude")

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(db) {
            SchemaUtils.create(LocationsTable)
        }
    }

    suspend fun create(location: Location): Long = dbQuery {
        LocationsTable.insert {
            it[city] = location.city
            it[country] = location.country
            it[latitude] = location.latitude
            it[longitude] = location.longitude
        }[LocationsTable.id]
    }

    suspend fun read(id: Long): Location? {
        return dbQuery {
            LocationsTable.selectAll()
                .where { LocationsTable.id eq id }
                .map {
                    Location(
                        it[LocationsTable.city],
                        it[LocationsTable.country],
                        it[LocationsTable.latitude],
                        it[LocationsTable.longitude]
                    )
                }
                .singleOrNull()
        }
    }

    suspend fun readByCity(city: String): Location? {
        return dbQuery {
            LocationsTable.selectAll()
                .where { LocationsTable.city eq city }
                .limit(1)
                .map {
                    Location(
                        it[LocationsTable.city],
                        it[LocationsTable.country],
                        it[LocationsTable.latitude],
                        it[LocationsTable.longitude]
                    )
                }
                .singleOrNull()
        }
    }

    suspend fun update(id: Long, location: Location) {
        dbQuery {
            LocationsTable.update({ LocationsTable.id eq id }) {
                it[city] = location.city
                it[country] = location.country
                it[latitude] = location.latitude
                it[longitude] = location.longitude
            }
        }
    }

    suspend fun delete(id: Long) {
        dbQuery {
            LocationsTable.deleteWhere { LocationsTable.id.eq(id) }
        }
    }
}