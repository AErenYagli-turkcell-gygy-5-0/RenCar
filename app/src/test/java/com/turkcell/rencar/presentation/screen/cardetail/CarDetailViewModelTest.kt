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
import com.turkcell.rencar.domain.reservation.*
import com.turkcell.rencar.presentation.navigation.RenCarDestination
import com.turkcell.rencar.test.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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

    @Test
    fun `reserve click does not navigate when another vehicle has active reservation`() = runTest {
        val viewModel = CarDetailViewModel(
            vehicleRepository = FakeVehicleRepository(),
            rentalRepository = FakeRentalRepository(),
            reservationRepository = FakeReservationRepository(activeVehicleId = RESERVED_VEHICLE_ID),
            savedStateHandle = SavedStateHandle(
                mapOf(RenCarDestination.ARG_VEHICLE_ID to VEHICLE_ID)
            )
        )

        viewModel.onIntent(CarDetailIntent.ScreenStarted)
        advanceUntilIdle()
        viewModel.onIntent(CarDetailIntent.ReserveClicked)
        advanceUntilIdle()

        assertEquals(RESERVED_VEHICLE_ID, viewModel.state.value.activeReservationVehicleId)
        assertNull(withTimeoutOrNull(100) { viewModel.effect.first() })
    }

    @Test
    fun `unlock click with active reservation creates rental and navigates to photo upload`() = runTest {
        val rentalRepository = FakeRentalRepository(createdStatus = RentalStatus.PREPARING.name)
        val viewModel = CarDetailViewModel(
            vehicleRepository = FakeVehicleRepository(vehicleStatus = VehicleStatus.RESERVED),
            rentalRepository = rentalRepository,
            reservationRepository = FakeReservationRepository(activeVehicleId = VEHICLE_ID),
            savedStateHandle = SavedStateHandle(
                mapOf(
                    RenCarDestination.ARG_VEHICLE_ID to VEHICLE_ID,
                    RenCarDestination.ARG_RENTAL_PLAN to RentalPlan.HOURLY.name
                )
            )
        )

        viewModel.onIntent(CarDetailIntent.ScreenStarted)
        advanceUntilIdle()
        viewModel.onIntent(CarDetailIntent.UnlockClicked)
        advanceUntilIdle()

        assertEquals(VEHICLE_ID, rentalRepository.requestedVehicleId)
        assertEquals(RentalPlan.HOURLY, rentalRepository.requestedPlan)
        assertNull(rentalRepository.requestedEndDate)
        assertEquals(
            CarDetailEffect.NavigateToRentalPhotoUpload(RENTAL_ID, VEHICLE_ID),
            viewModel.effect.first()
        )
    }

    private class FakeRentalRepository(
        private val createdStatus: String = RentalStatus.PREPARING.name
    ) : RentalRepository {
        var requestedVehicleId: String? = null
        var requestedPlan: RentalPlan? = null
        var requestedEndDate: String? = null

        override suspend fun createRental(vehicleId: String, plan: RentalPlan, endDate: String?): RentalResult<Rental> {
            requestedVehicleId = vehicleId
            requestedPlan = plan
            requestedEndDate = endDate
            return RentalResult.Success(
                Rental(
                    id = RENTAL_ID,
                    userId = "user-1",
                    vehicleId = vehicleId,
                    plan = plan,
                    startDate = "2026-07-14T10:00:00.000Z",
                    endDate = endDate,
                    totalPrice = null,
                    status = createdStatus,
                    createdAt = "2026-07-14T10:00:00.000Z"
                )
            )
        }

        override suspend fun getMyRentals(): RentalResult<List<RentalSummary>> =
            RentalResult.Success(emptyList())

        override suspend fun getRentalHistory(): RentalResult<List<RentalHistoryItem>> =
            RentalResult.Success(emptyList())

        override suspend fun getRentalStats(): RentalResult<RentalStats> =
            RentalResult.Success(RentalStats(0, 0.0, 0.0, 0.0))

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
    }

    private class FakeReservationRepository(
        private val activeVehicleId: String? = null
    ) : ReservationRepository {
        override suspend fun createReservation(vehicleId: String): ReservationResult<Reservation> =
            error("Not used by this test")

        override suspend fun getActiveReservation(): ReservationResult<Reservation> {
            val vehicleId = activeVehicleId ?: return ReservationResult.Failure(ReservationError.NotFound)
            return ReservationResult.Success(
                Reservation(
                    id = "reservation-1",
                    vehicleId = vehicleId,
                    vehicle = ReservationVehicleSummary(
                        id = vehicleId,
                        plate = "34 RNC 022",
                        brand = "Renault",
                        model = "Clio",
                        type = VehicleType.HATCHBACK,
                        latitude = 41.0,
                        longitude = 29.0,
                        pricePerMinute = 4.5
                    ),
                    status = ReservationStatus.ACTIVE,
                    expiresAt = "2026-07-14T10:15:00.000Z",
                    remainingSeconds = 900
                )
            )
        }

        override suspend fun cancelReservation(id: String): ReservationResult<Unit> =
            error("Not used by this test")
    }

    private class FakeVehicleRepository(
        private val vehicleStatus: VehicleStatus = VehicleStatus.AVAILABLE
    ) : VehicleRepository {
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
            status = vehicleStatus,
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
        const val RESERVED_VEHICLE_ID = "vehicle-2"
        const val RENTAL_ID = "rental-1"
    }
}
