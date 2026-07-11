package com.turkcell.rencar.domain.vehicle

interface VehicleRepository {

    suspend fun listAvailableVehicles(type: VehicleType? = null): VehicleResult<List<Vehicle>>

    suspend fun getVehicle(id: String): VehicleResult<Vehicle>
}
