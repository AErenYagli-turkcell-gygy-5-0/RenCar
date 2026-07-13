package com.turkcell.rencar.domain.reservation

interface ReservationRepository {

    suspend fun createReservation(vehicleId: String): ReservationResult<Reservation>

    suspend fun getActiveReservation(): ReservationResult<Reservation>

    suspend fun cancelReservation(id: String): ReservationResult<Unit>
}
