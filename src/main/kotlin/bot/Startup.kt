package com.dsidak.bot

import java.io.File
import java.util.Properties

fun loadProperties(): Properties {
    val properties = Properties()
    properties.load(File("./src/main/resources/project.properties").inputStream())
    return properties
}