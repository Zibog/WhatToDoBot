plugins {
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.serialization") version "2.1.0"
}

group = "com.dsidak"
version = "1.0-SNAPSHOT"

val telegramBots: String by project
val kotlinLogging: String by project
val logback: String by project
val mockito: String by project
val kotlinX: String by project

val mockitoAgent = configurations.create("mockitoAgent")

repositories {
    mavenCentral()
}

dependencies {
    // https://mvnrepository.com/artifact/org.telegram/telegrambots-client
    implementation("org.telegram:telegrambots-client:$telegramBots")
    // https://mvnrepository.com/artifact/org.telegram/telegrambots-abilities
    implementation("org.telegram:telegrambots-abilities:$telegramBots")

    // https://mvnrepository.com/artifact/io.github.oshai/kotlin-logging-jvm
    implementation("io.github.oshai:kotlin-logging-jvm:$kotlinLogging")
    // https://mvnrepository.com/artifact/ch.qos.logback/logback-classic
    implementation("ch.qos.logback:logback-classic:$logback")

    // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-serialization-json
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinX")

    // https://mvnrepository.com/artifact/io.arrow-kt/arrow-core
    implementation("io.arrow-kt:arrow-core:2.0.1")

    testImplementation(kotlin("test"))
    // https://mvnrepository.com/artifact/org.mockito/mockito-core
    testImplementation("org.mockito:mockito-core:$mockito")
    mockitoAgent("org.mockito:mockito-core:$mockito") { isTransitive = false }
}

tasks.test {
    useJUnitPlatform()
    jvmArgs("-javaagent:${mockitoAgent.asPath}")
}