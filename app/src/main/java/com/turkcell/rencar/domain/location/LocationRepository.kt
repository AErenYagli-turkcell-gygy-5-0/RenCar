package com.turkcell.rencar.domain.location

import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    fun observeMyVehicleLocation(): Flow<VehicleLocation>
}
