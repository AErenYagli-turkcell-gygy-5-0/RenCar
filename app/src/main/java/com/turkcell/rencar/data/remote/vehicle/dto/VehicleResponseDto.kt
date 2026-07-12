package com.turkcell.rencar.data.remote.vehicle.dto

data class VehicleResponseDto(
    val id: String,
    val plate: String,
    val brand: String,
    val model: String,
    val type: String,
    val pricePerDay: Double,
    // Backend'de bu alanlar tüm kayıtlarda garanti değil (bkz. ApiVehicleRepository.toDomain);
    // eksik/null gelirse tüm araç listesinin çökmemesi için nullable tutulur.
    val pricePerMinute: Double? = null,
    val pricePerHour: Double? = null,
    val fuelPercent: Double? = null,
    val rangeKm: Double? = null,
    val transmission: String? = null,
    val seats: Int? = null,
    val segment: String? = null,
    val status: String,
    val latitude: Double,
    val longitude: Double,
    val createdAt: String,
    val updatedAt: String
)
