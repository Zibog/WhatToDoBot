plugins {
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.serialization") version "2.1.0"
    id("io.ktor.plugin") version "3.1.1"
}

group = "com.dsidak"
version = "1.0-SNAPSHOT"

val telegramBots: String by project
val kotlinLogging: String by project
val logback: String by project
val mockito: String by project
val mockitoKotlin: String by project
val kotlinX: String by project
val dotenv: String by project
val ktor: String by project
val exposedVersion: String by project
val h2Version: String by project
val hoplite: String by project

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

    // https://mvnrepository.com/artifact/io.github.cdimascio/dotenv-kotlin
    implementation("io.github.cdimascio:dotenv-kotlin:$dotenv")

    // https://mvnrepository.com/artifact/io.ktor/ktor-server-core-jvm
    implementation("io.ktor:ktor-server-core-jvm:$ktor")
    // https://mvnrepository.com/artifact/org.jetbrains.exposed/exposed-core
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    // https://mvnrepository.com/artifact/org.jetbrains.exposed/exposed-jdbc
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    // https://mvnrepository.com/artifact/com.h2database/h2
    implementation("com.h2database:h2:$h2Version")

    // https://mvnrepository.com/artifact/com.sksamuel.hoplite/hoplite-core
    implementation("com.sksamuel.hoplite:hoplite-core:$hoplite")
    // https://mvnrepository.com/artifact/com.sksamuel.hoplite/hoplite-yaml
    implementation("com.sksamuel.hoplite:hoplite-yaml:$hoplite")

    testImplementation(kotlin("test"))
    // https://mvnrepository.com/artifact/org.mockito/mockito-core
    testImplementation("org.mockito:mockito-core:$mockito")
    testImplementation("org.mockito.kotlin:mockito-kotlin:$mockitoKotlin")
    mockitoAgent("org.mockito:mockito-core:$mockito") { isTransitive = false }
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs("-javaagent:${mockitoAgent.asPath}")
}

application {
    mainClass = "com.dsidak.MainKt"
}