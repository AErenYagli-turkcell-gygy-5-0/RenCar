package com.turkcell.rencar.data.repository.location

import com.turkcell.rencar.data.remote.location.LocationSocketClient
import com.turkcell.rencar.domain.location.LocationRepository
import com.turkcell.rencar.domain.location.VehicleLocation
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ApiLocationRepository @Inject constructor(
    private val socketClient: LocationSocketClient
) : LocationRepository {

    override fun observeMyVehicleLocation(): Flow<VehicleLocation> =
        socketClient.observeMyVehicle()
}
