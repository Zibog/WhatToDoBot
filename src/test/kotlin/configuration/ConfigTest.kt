package configuration

import base.ResourceTestBase
import com.dsidak.configuration.Config
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addFileSource
import java.io.File
import kotlin.test.Test
import kotlin.test.assertNotNull

class ConfigTest : ResourceTestBase {
    @Test
    fun testConfig_defaultConfigIsValid() {
        val configFile = File("src/main/resources/default.yaml")

        assert(configFile.exists())

        val config = ConfigLoaderBuilder.default()
            .addFileSource(configFile)
            .build()
            .loadConfigOrThrow<Config>()

        assertNotNull(config, "Could not load app config")
    }

    @Test
    fun testConfig_testConfigIsValid() {
        val configFile = File("$resources/default.yaml")

        assert(configFile.exists())

        val config = ConfigLoaderBuilder.default()
            .addFileSource(configFile)
            .build()
            .loadConfigOrThrow<Config>()

        assertNotNull(config, "Could not load test config")
    }
}