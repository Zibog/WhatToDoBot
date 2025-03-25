package com.dsidak.db.schemas

import com.dsidak.db.dbQuery
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

@Serializable
data class User(
    val id: Long,
    val firstName: String,
    val lastName: String?,
    val userName: String,
    val locationId: Long? = null
)

class UserService(db: Database) {
    object UsersTable : Table() {
        // Telegram user id
        val id = long("id")
        val firstName = varchar("first_name", 50)
        val lastName = varchar("last_name", 50).nullable()
        val userName = varchar("user_name", 50)
        val locationId = reference("location_id", LocationService.LocationsTable.id).nullable()

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(db) {
            SchemaUtils.create(UsersTable)
        }
    }

    suspend fun create(user: User): Long = dbQuery {
        UsersTable.insert {
            it[id] = user.id
            it[firstName] = user.firstName
            it[lastName] = user.lastName
            it[userName] = user.userName
            it[locationId] = user.locationId
        }[UsersTable.id]
    }

    suspend fun read(id: Long): User? {
        return dbQuery {
            UsersTable.selectAll()
                .where { UsersTable.id eq id }
                .map {
                    User(
                        it[UsersTable.id],
                        it[UsersTable.firstName],
                        it[UsersTable.lastName],
                        it[UsersTable.userName],
                        it[UsersTable.locationId]
                    )
                }
                .singleOrNull()
        }
    }

    suspend fun update(id: Long, user: User) {
        dbQuery {
            UsersTable.update({ UsersTable.id eq id }) {
                it[firstName] = user.firstName
                it[lastName] = user.lastName
                it[userName] = user.userName
                it[locationId] = user.locationId
            }
        }
    }

    suspend fun delete(id: Long) {
        dbQuery {
            UsersTable.deleteWhere { UsersTable.id.eq(id) }
        }
    }
}