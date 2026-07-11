package com.turkcell.rencar.domain.rental

interface RentalRepository {
    suspend fun createRental(vehicleId: String, endDate: String): RentalResult<Rental>
}
