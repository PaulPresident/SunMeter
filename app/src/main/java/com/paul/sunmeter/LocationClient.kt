package com.paul.sunmeter

import android.location.Location
import kotlinx.coroutines.flow.Flow

interface LocationClient {
    fun getLocationUpdates(interval: Double): Flow<Location>

    class LocationException(message: String): Exception()
}