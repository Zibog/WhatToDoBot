plugins {
    kotlin("jvm") version "2.1.0"
}

group = "com.dsidak"
version = "1.0-SNAPSHOT"

val telegramBots: String by project
val mockito: String by project

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("org.telegram:telegrambots-client:$telegramBots")
    implementation("org.telegram:telegrambots-abilities:$telegramBots")

    testImplementation(kotlin("test"))
    testImplementation("org.mockito:mockito-core:$mockito")
}

tasks.test {
    useJUnitPlatform()
}