package com.turkcell.rencar.domain.vehicle

interface VehicleRepository {

    suspend fun listAvailableVehicles(type: VehicleType? = null): VehicleResult<List<Vehicle>>

    suspend fun getAvailableVehicle(vehicleId: String): VehicleResult<Vehicle>
}
