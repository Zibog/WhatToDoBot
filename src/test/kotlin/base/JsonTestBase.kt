package base

import kotlinx.serialization.json.Json

interface JsonTestBase : ResourceTestBase {
    // Synchronize the configuration with the one in the [Client] class
    val json: Json
        get() = Json { ignoreUnknownKeys = true }
}