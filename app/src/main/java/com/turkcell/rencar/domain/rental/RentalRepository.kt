package com.turkcell.rencar.domain.rental

interface RentalRepository {
    suspend fun createRental(
        vehicleId: String,
        plan: RentalPlan,
        endDate: String? = null
    ): RentalResult<Rental>

    suspend fun getMyRentals(): RentalResult<List<RentalSummary>>
}
