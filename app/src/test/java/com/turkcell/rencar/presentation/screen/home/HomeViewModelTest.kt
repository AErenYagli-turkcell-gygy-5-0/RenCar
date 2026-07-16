package com.turkcell.rencar.presentation.screen.home

import android.net.Uri
import com.turkcell.rencar.domain.rental.ActiveRental
import com.turkcell.rencar.domain.rental.Rental
import com.turkcell.rencar.domain.rental.RentalHistoryItem
import com.turkcell.rencar.domain.rental.RentalPhotoSide
import com.turkcell.rencar.domain.rental.RentalPhotosState
import com.turkcell.rencar.domain.rental.RentalPlan
import com.turkcell.rencar.domain.rental.RentalRepository
import com.turkcell.rencar.domain.rental.RentalResult
import com.turkcell.rencar.domain.rental.RentalStats
import com.turkcell.rencar.domain.rental.RentalSummary
import com.turkcell.rencar.domain.reservation.Reservation
import com.turkcell.rencar.domain.reservation.ReservationRepository
import com.turkcell.rencar.domain.reservation.ReservationResult
import com.turkcell.rencar.domain.reservation.ReservationStatus
import com.turkcell.rencar.domain.reservation.ReservationVehicleSummary
import com.turkcell.rencar.domain.vehicle.Transmission
import com.turkcell.rencar.domain.vehicle.Vehicle
import com.turkcell.rencar.domain.vehicle.VehicleQuote
import com.turkcell.rencar.domain.vehicle.VehicleRepository
import com.turkcell.rencar.domain.vehicle.VehicleResult
import com.turkcell.rencar.domain.vehicle.VehicleSegment
import com.turkcell.rencar.domain.vehicle.VehicleStatus
import com.turkcell.rencar.domain.vehicle.VehicleType
import com.turkcell.rencar.test.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `active reservation remains visible on home vehicle markers`() = runTest {
        val viewModel = HomeViewModel(
            vehicleRepository = FakeVehicleRepository(),
            reservationRepository = FakeReservationRepository(activeReservation()),
            rentalRepository = FakeRentalRepository()
        )

        viewModel.onIntent(HomeIntent.ScreenStarted)
        advanceUntilIdle()

        val reservedMarker = viewModel.state.value.vehicles.first { it.id == RESERVED_VEHICLE_ID }
        assertEquals(RESERVED_VEHICLE_ID, viewModel.state.value.activeReservationVehicleId)
        assertEquals("Renault Clio", viewModel.state.value.activeReservationVehicleName)
        assertTrue(reservedMarker.isReservedByMe)
        assertEquals(2, viewModel.state.value.vehicles.size)
    }

    private class FakeVehicleRepository : VehicleRepository {
        override suspend fun listAvailableVehicles(type: VehicleType?): VehicleResult<List<Vehicle>> =
            VehicleResult.Success(listOf(availableVehicle))

        override suspend fun getVehicle(id: String): VehicleResult<Vehicle> =
            VehicleResult.Success(availableVehicle)

        override suspend fun getQuote(id: String, plan: String, minutes: Int): VehicleResult<VehicleQuote> =
            error("Quote is not used by home")
    }

    private class FakeReservationRepository(
        private val reservation: Reservation
    ) : ReservationRepository {
        override suspend fun createReservation(vehicleId: String): ReservationResult<Reservation> =
            error("Create reservation is not used by home")

        override suspend fun getActiveReservation(): ReservationResult<Reservation> =
            ReservationResult.Success(reservation)

        override suspend fun cancelReservation(id: String): ReservationResult<Unit> =
            error("Cancel reservation is not used by home")
    }

    private class FakeRentalRepository : RentalRepository {
        override suspend fun createRental(vehicleId: String, plan: RentalPlan, endDate: String?): RentalResult<Rental> =
            error("Create rental is not used by home")

        override suspend fun getMyRentals(): RentalResult<List<RentalSummary>> =
            RentalResult.Success(emptyList())

        override suspend fun getRentalHistory(): RentalResult<List<RentalHistoryItem>> =
            RentalResult.Success(emptyList())

        override suspend fun getRentalStats(): RentalResult<RentalStats> =
            RentalResult.Success(RentalStats(0, 0.0, 0.0, 0.0))

        override suspend fun uploadRentalPhoto(
            rentalId: String,
            side: RentalPhotoSide,
            imageUri: Uri
        ): RentalResult<RentalPhotosState> = error("Upload photo is not used by home")

        override suspend fun getRentalPhotos(rentalId: String): RentalResult<RentalPhotosState> =
            error("Get photos is not used by home")

        override suspend fun startRental(rentalId: String): RentalResult<Rental> =
            error("Start rental is not used by home")

        override suspend fun cancelRental(rentalId: String): RentalResult<Unit> =
            error("Cancel rental is not used by home")

        override suspend fun getActiveRental(): RentalResult<ActiveRental> =
            error("Active rental is not used by home")

        override suspend fun finishRental(rentalId: String): RentalResult<Rental> =
            error("Finish rental is not used by home")
    }

    private companion object {
        const val AVAILABLE_VEHICLE_ID = "vehicle-1"
        const val RESERVED_VEHICLE_ID = "vehicle-2"

        val availableVehicle = Vehicle(
            id = AVAILABLE_VEHICLE_ID,
            plate = "34 RNC 001",
            brand = "Fiat",
            model = "Egea",
            type = VehicleType.SEDAN,
            pricePerDay = 1200.0,
            pricePerMinute = 4.0,
            pricePerHour = 150.0,
            fuelPercent = 80.0,
            rangeKm = 500.0,
            transmission = Transmission.MANUAL,
            seats = 5,
            segment = VehicleSegment.ECONOMY,
            status = VehicleStatus.AVAILABLE,
            latitude = 41.0,
            longitude = 29.0
        )

        fun activeReservation() = Reservation(
            id = "reservation-1",
            vehicleId = RESERVED_VEHICLE_ID,
            vehicle = ReservationVehicleSummary(
                id = RESERVED_VEHICLE_ID,
                plate = "34 RNC 022",
                brand = "Renault",
                model = "Clio",
                type = VehicleType.HATCHBACK,
                latitude = 41.1,
                longitude = 29.1,
                pricePerMinute = 4.5
            ),
            status = ReservationStatus.ACTIVE,
            expiresAt = "2026-07-14T10:15:00.000Z",
            remainingSeconds = 900
        )
    }
}
