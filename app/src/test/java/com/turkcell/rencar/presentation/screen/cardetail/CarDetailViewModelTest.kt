package com.turkcell.rencar.presentation.screen.cardetail

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import com.turkcell.rencar.domain.vehicle.Vehicle
import com.turkcell.rencar.domain.vehicle.VehicleRepository
import com.turkcell.rencar.domain.vehicle.VehicleResult
import com.turkcell.rencar.domain.vehicle.VehicleType
import com.turkcell.rencar.domain.vehicle.VehicleQuote
import com.turkcell.rencar.domain.vehicle.VehicleSegment
import com.turkcell.rencar.domain.vehicle.VehicleStatus
import com.turkcell.rencar.domain.vehicle.Transmission
import com.turkcell.rencar.domain.rental.*
import com.turkcell.rencar.domain.reservation.Reservation
import com.turkcell.rencar.domain.reservation.ReservationRepository
import com.turkcell.rencar.domain.reservation.ReservationResult
import com.turkcell.rencar.presentation.navigation.RenCarDestination
import com.turkcell.rencar.test.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CarDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `reserve click emits selected vehicle id after vehicle loads`() = runTest {
        val viewModel = CarDetailViewModel(
            vehicleRepository = FakeVehicleRepository(),
            rentalRepository = FakeRentalRepository(),
            reservationRepository = FakeReservationRepository(),
            savedStateHandle = SavedStateHandle(
                mapOf(RenCarDestination.ARG_VEHICLE_ID to VEHICLE_ID)
            )
        )

        viewModel.onIntent(CarDetailIntent.ScreenStarted)
        advanceUntilIdle()
        viewModel.onIntent(CarDetailIntent.ReserveClicked)
        advanceUntilIdle()

        assertEquals(
            CarDetailEffect.NavigateToReservationConfirmation(VEHICLE_ID),
            viewModel.effect.first()
        )
    }

    private class FakeRentalRepository : RentalRepository {
        override suspend fun createRental(vehicleId: String, plan: RentalPlan, endDate: String?): RentalResult<Rental> =
            error("Create rental is not used by car detail")

        override suspend fun getMyRentals(): RentalResult<List<RentalSummary>> =
            RentalResult.Success(emptyList())

        override suspend fun getRentalHistory(): RentalResult<List<RentalHistoryItem>> =
            error("Not used by car detail")

        override suspend fun getRentalStats(): RentalResult<RentalStats> =
            error("Not used by car detail")

        override suspend fun uploadRentalPhoto(rentalId: String, side: RentalPhotoSide, imageUri: Uri): RentalResult<RentalPhotosState> =
            error("Not used by car detail")

        override suspend fun getRentalPhotos(rentalId: String): RentalResult<RentalPhotosState> =
            error("Not used by car detail")

        override suspend fun startRental(rentalId: String): RentalResult<Rental> =
            error("Not used by car detail")

        override suspend fun cancelRental(rentalId: String): RentalResult<Unit> =
            error("Not used by car detail")

        override suspend fun getActiveRental(): RentalResult<ActiveRental> =
            error("Not used by car detail")

        override suspend fun finishRental(rentalId: String): RentalResult<Rental> =
            error("Not used by car detail")

        override suspend fun getRentalDetail(rentalId: String): RentalResult<Rental> =
            error("Not used by car detail")

        override suspend fun payRental(
            rentalId: String,
            method: PaymentMethod,
            cardId: String?,
            discountCode: String?
        ): RentalResult<PaymentReceipt> =
            error("Not used by car detail")
    }

    private class FakeReservationRepository : ReservationRepository {
        override suspend fun createReservation(vehicleId: String): ReservationResult<Reservation> =
            error("Not used by this test")

        override suspend fun getActiveReservation(): ReservationResult<Reservation> =
            error("Not used by this test")

        override suspend fun cancelReservation(id: String): ReservationResult<Unit> =
            error("Not used by this test")
    }

    private class FakeVehicleRepository : VehicleRepository {
        private val vehicle = Vehicle(
            id = VEHICLE_ID,
            plate = "34 RNC 022",
            brand = "Renault",
            model = "Clio",
            type = VehicleType.HATCHBACK,
            pricePerDay = 1450.0,
            pricePerMinute = 4.5,
            pricePerHour = 180.0,
            fuelPercent = 72.0,
            rangeKm = 480.0,
            transmission = Transmission.MANUAL,
            seats = 5,
            segment = VehicleSegment.ECONOMY,
            status = VehicleStatus.AVAILABLE,
            latitude = 41.0,
            longitude = 29.0
        )

        override suspend fun listAvailableVehicles(type: VehicleType?): VehicleResult<List<Vehicle>> =
            VehicleResult.Success(listOf(vehicle))

        override suspend fun getVehicle(id: String): VehicleResult<Vehicle> =
            VehicleResult.Success(vehicle)

        override suspend fun getQuote(id: String, plan: String, minutes: Int): VehicleResult<VehicleQuote> =
            error("Quote is not used by car detail")
    }

    private companion object {
        const val VEHICLE_ID = "vehicle-1"
    }
}
