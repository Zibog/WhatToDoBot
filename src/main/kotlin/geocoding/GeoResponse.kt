package com.dsidak.geocoding

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CityInfo(
    val name: String,
    @SerialName("local_names")
    val localNames: Map<String, String> = emptyMap(),
    @SerialName("lat")
    val latitude: Double,
    @SerialName("lon")
    val longitude: Double,
    val country: String,
    val state: String = ""
) {
    companion object {
        val EMPTY = CityInfo("", emptyMap(), 0.0, 0.0, "")
    }
}