package com.turkcell.rencar.domain.rental

import android.net.Uri

interface RentalRepository {
    suspend fun createRental(
        vehicleId: String,
        plan: RentalPlan,
        endDate: String? = null
    ): RentalResult<Rental>

    suspend fun getMyRentals(): RentalResult<List<RentalSummary>>

    suspend fun getRentalHistory(): RentalResult<List<RentalHistoryItem>>

    suspend fun getRentalStats(): RentalResult<RentalStats>

    suspend fun uploadRentalPhoto(
        rentalId: String,
        side: RentalPhotoSide,
        imageUri: Uri
    ): RentalResult<RentalPhotosState>

    suspend fun getRentalPhotos(rentalId: String): RentalResult<RentalPhotosState>

    suspend fun startRental(rentalId: String): RentalResult<Rental>

    suspend fun cancelRental(rentalId: String): RentalResult<Unit>

    suspend fun getActiveRental(): RentalResult<ActiveRental>

    suspend fun finishRental(rentalId: String): RentalResult<Rental>

    suspend fun getRentalDetail(rentalId: String): RentalResult<Rental>

    suspend fun payRental(
        rentalId: String,
        method: PaymentMethod,
        cardId: String? = null,
        discountCode: String? = null,
        iyzicoPaymentId: String? = null
    ): RentalResult<PaymentReceipt>
}
