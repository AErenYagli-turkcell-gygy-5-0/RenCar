package com.turkcell.rencar.domain.reservation

data class Reservation(
    val id: String,
    val vehicleId: String,
    val status: ReservationStatus,
    val expiresAt: String,
    val remainingSeconds: Int
)
