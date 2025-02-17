package com.dsidak.openmeteo

import java.time.MonthDay

data class RequestContext(var latitude: Double, var longitude: Double, var day: MonthDay, var url: String)
