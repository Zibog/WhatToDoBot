plugins {
    kotlin("jvm") version "2.1.0"
}

group = "com.dsidak"
version = "1.0-SNAPSHOT"

val telegramBots: String by project
val kotlinLogging: String by project
val logback: String by project
val mockito: String by project

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    // https://mvnrepository.com/artifact/org.telegram/telegrambots-client
    implementation("org.telegram:telegrambots-client:$telegramBots")
    // https://mvnrepository.com/artifact/org.telegram/telegrambots-abilities
    implementation("org.telegram:telegrambots-abilities:$telegramBots")

    // https://mvnrepository.com/artifact/io.github.oshai/kotlin-logging-jvm
    implementation("io.github.oshai:kotlin-logging-jvm:$kotlinLogging")
    // https://mvnrepository.com/artifact/ch.qos.logback/logback-classic
    implementation("ch.qos.logback:logback-classic:")

    testImplementation(kotlin("test"))
    // https://mvnrepository.com/artifact/org.mockito/mockito-core
    testImplementation("org.mockito:mockito-core:$mockito")
}

tasks.test {
    useJUnitPlatform()
}