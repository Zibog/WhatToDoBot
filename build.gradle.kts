plugins {
    kotlin("jvm") version "2.1.0"
}

group = "com.dsidak"
version = "1.0-SNAPSHOT"

val telegramBotsVersion: String by project

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("io.github.kotlin-telegram-bot.kotlin-telegram-bot:telegram:6.2.0")
    implementation("org.telegram:telegrambots-abilities:$telegramBotsVersion")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}