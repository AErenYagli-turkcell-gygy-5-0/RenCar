package com.turkcell.rencar.domain.vehicle

interface VehicleRepository {

    suspend fun listAvailableVehicles(type: VehicleType? = null): VehicleResult<List<Vehicle>>

    suspend fun getVehicle(id: String): VehicleResult<Vehicle>

    suspend fun getQuote(id: String, plan: String, minutes: Int): VehicleResult<VehicleQuote>
}
