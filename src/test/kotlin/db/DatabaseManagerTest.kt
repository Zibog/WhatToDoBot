package db

import com.dsidak.db.DatabaseManager
import com.dsidak.db.schemas.Location
import com.dsidak.db.schemas.User
import com.dsidak.db.schemas.UserLocation
import kotlinx.coroutines.runBlocking
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class DatabaseManagerTest {
    @Test
    fun testDatabaseManager_createUser() {
        val userToCreate = User(
            id = Random.nextInt(),
            firstName = "first",
            lastName = null,
            userName = "userName"
        )

        val userId = runBlocking {
            DatabaseManager.userService.create(
                userToCreate
            )
        }

        val userToCheck = runBlocking {
            DatabaseManager.userService.read(userId)
        }

        assertEquals(userToCreate, userToCheck)
    }

    @Test
    fun testDatabaseManager_createLocation() {
        val locationToCreate = Location(
            city = "New York",
            country = "USA",
            latitude = 40.7128,
            longitude = -74.0060
        )

        val locationId = runBlocking {
            DatabaseManager.locationService.create(
                locationToCreate
            )
        }

        val locationToCheck = runBlocking {
            DatabaseManager.locationService.read(locationId)
        }

        assertEquals(locationToCreate, locationToCheck)
    }

    @Test
    fun testDatabaseManager_createUserLocation() {
        val userToCreate = User(
            id = Random.nextInt(),
            firstName = "first",
            lastName = "last",
            userName = "userName"
        )

        val userId = runBlocking {
            DatabaseManager.userService.create(
                userToCreate
            )
        }

        val locationToCreate = Location(
            city = "New York",
            country = "USA",
            latitude = 40.7128,
            longitude = -74.0060
        )

        val locationId = runBlocking {
            DatabaseManager.locationService.create(
                locationToCreate
            )
        }

        val userLocationToCreate = UserLocation(
            userId = userId,
            locationId = locationId
        )

        val userLocationId = runBlocking {
            DatabaseManager.userLocationService.create(
                userLocationToCreate
            )
        }

        val userLocationToCheck = runBlocking {
            DatabaseManager.userLocationService.read(userLocationId)
        }

        assertEquals(userLocationToCreate, userLocationToCheck)
        assertEquals(userToCreate, runBlocking { DatabaseManager.userService.read(userLocationToCheck!!.userId) })
        assertEquals(
            locationToCreate,
            runBlocking { DatabaseManager.locationService.read(userLocationToCheck!!.locationId) })
    }
}