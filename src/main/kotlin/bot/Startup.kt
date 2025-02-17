package com.dsidak.bot

import java.io.File
import java.util.Properties

fun loadProperties(): Properties {
    val properties = Properties()
    // TODO: make path configurable (e.g. for test properties) as input parameter
    val path = "./src/main/resources/project.properties"
    properties.load(File(path).inputStream())
    return properties
}