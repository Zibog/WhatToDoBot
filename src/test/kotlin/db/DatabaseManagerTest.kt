package db

import com.dsidak.db.DatabaseManager
import com.dsidak.db.schemas.Location
import com.dsidak.db.schemas.User
import kotlinx.coroutines.runBlocking
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class DatabaseManagerTest {
    private val userToCreate = User(
        id = Random.nextLong(),
        firstName = "first",
        lastName = null,
        userName = "userName"
    )
    private val locationToCreate = Location(
        city = "New York",
        country = "US",
        latitude = 40.7128,
        longitude = -74.0060
    )

    @Test
    fun testDatabaseManager_createUser() {
        val userToCheck = runBlocking {
            val userId = DatabaseManager.userService.create(
                userToCreate
            )
            DatabaseManager.userService.read(userId)
        }

        assertEquals(userToCreate, userToCheck)
    }

    @Test
    fun testDatabaseManager_createLocation() {
        val locationToCheck = runBlocking {
            val locationId = DatabaseManager.locationService.create(
                locationToCreate
            )
            DatabaseManager.locationService.read(locationId)
        }

        assertEquals(locationToCreate, locationToCheck)
    }

    @Test
    fun testDatabaseManager_createUserWithLocation() {
        val userWithLocation = runBlocking {
            val locationId = DatabaseManager.locationService.create(
                locationToCreate
            )
            userToCreate.copy(locationId = locationId)
        }

        val userToCheck = runBlocking {
            val userId = DatabaseManager.userService.create(
                userWithLocation
            )
            DatabaseManager.userService.read(userId)
        }

        assertEquals(userWithLocation, userToCheck)
    }

    @Test
    fun testDatabaseManager_createOrReadLocation() {
        val locationId = runBlocking {
            DatabaseManager.createOrReadLocation(
                locationToCreate
            )
        }

        val locationToCheck = runBlocking {
            DatabaseManager.locationService.read(locationId)
        }

        assertEquals(locationToCreate, locationToCheck)

        val locationId2 = runBlocking {
            DatabaseManager.createOrReadLocation(
                locationToCreate
            )
        }

        assertEquals(locationId, locationId2)
    }

    @Test
    fun testDatabaseManager_createOrReadUser() {
        val userWithLocation = userToCreate.copy(
            locationId = runBlocking {
                DatabaseManager.createOrReadLocation(
                    locationToCreate
                )
            }
        )

        runBlocking {
            DatabaseManager.createOrReadUser(
                userWithLocation
            )
        }

        val userToCheck = runBlocking {
            DatabaseManager.userService.read(userWithLocation.id)
        }

        assertEquals(userWithLocation, userToCheck)

        runBlocking {
            DatabaseManager.createOrReadUser(
                userWithLocation
            )
        }

        val userToCheck2 = runBlocking {
            DatabaseManager.userService.read(userWithLocation.id)
        }

        assertEquals(userWithLocation, userToCheck2)
    }
}