package com.turkcell.rencar.presentation.screen.reservation.confirmation

import android.net.Uri
import com.turkcell.rencar.domain.rental.*
import com.turkcell.rencar.domain.reservation.*
import com.turkcell.rencar.domain.vehicle.*
import com.turkcell.rencar.test.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReservationConfirmationViewModelTest {

    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `screen start loads vehicle and minute quote`() = runTest {
        val viewModel = createViewModel()
        viewModel.onIntent(ReservationConfirmationIntent.ScreenStarted(VEHICLE_ID))
        advanceUntilIdle()

        assertEquals("Renault Clio", viewModel.state.value.vehicleName)
        assertEquals(72.0, viewModel.state.value.fuelPercent, 0.0)
        assertEquals(120.0, viewModel.state.value.usageFee, 0.0)
        assertEquals(15.0, viewModel.state.value.startFee, 0.0)
        assertEquals(0.0, viewModel.state.value.serviceFee, 0.0)
        assertEquals(135.0, viewModel.state.value.estimatedTotal, 0.0)
        assertTrue(viewModel.state.value.hasQuote)
    }

    @Test
    fun `plan selection refreshes quote`() = runTest {
        val vehicleRepository = FakeVehicleRepository()
        val viewModel = createViewModel(vehicleRepository = vehicleRepository)
        viewModel.onIntent(ReservationConfirmationIntent.ScreenStarted(VEHICLE_ID))
        advanceUntilIdle()
        viewModel.onIntent(ReservationConfirmationIntent.PlanSelected(RentalPlan.HOURLY))
        advanceUntilIdle()

        assertEquals(RentalPlan.HOURLY, viewModel.state.value.selectedPlan)
        assertEquals(RentalPlan.HOURLY.name, vehicleRepository.requestedPlan)
        assertEquals(30, vehicleRepository.requestedMinutes)
        assertEquals(120.0, viewModel.state.value.usageFee, 0.0)
        assertEquals(15.0, viewModel.state.value.startFee, 0.0)
        assertEquals(0.0, viewModel.state.value.serviceFee, 0.0)
        assertEquals(135.0, viewModel.state.value.estimatedTotal, 0.0)
    }

    @Test
    fun `accepted terms creates reservation then rental with selected plan`() = runTest {
        val rentalRepository = FakeRentalRepository()
        val viewModel = createViewModel(rentalRepository = rentalRepository)
        viewModel.onIntent(ReservationConfirmationIntent.ScreenStarted(VEHICLE_ID))
        advanceUntilIdle()
        viewModel.onIntent(ReservationConfirmationIntent.TermsAcceptanceChanged(true))
        viewModel.onIntent(ReservationConfirmationIntent.CompleteReservationClicked)
        advanceUntilIdle()

        assertEquals(RentalPlan.PER_MINUTE, rentalRepository.requestedPlan)
        assertNull(rentalRepository.requestedEndDate)
        assertEquals(
            ReservationConfirmationEffect.ReservationCreated(RENTAL_ID, VEHICLE_ID, isPreparing = true),
            viewModel.effect.first()
        )
    }

    private fun createViewModel(
        vehicleRepository: FakeVehicleRepository = FakeVehicleRepository(),
        rentalRepository: FakeRentalRepository = FakeRentalRepository()
    ) = ReservationConfirmationViewModel(vehicleRepository, rentalRepository, FakeReservationRepository())

    private class FakeVehicleRepository : VehicleRepository {
        var requestedPlan: String? = null
        var requestedMinutes: Int? = null
        override suspend fun listAvailableVehicles(type: VehicleType?) = VehicleResult.Success(listOf(vehicle))
        override suspend fun getVehicle(id: String) = VehicleResult.Success(vehicle)
        override suspend fun getQuote(id: String, plan: String, minutes: Int): VehicleResult<VehicleQuote> {
            requestedPlan = plan
            requestedMinutes = minutes
            return VehicleResult.Success(VehicleQuote(id, plan, minutes, 120.0, 15.0, 0.0, 135.0))
        }
    }

    private class FakeRentalRepository : RentalRepository {
        var requestedPlan: RentalPlan? = null
        var requestedEndDate: String? = null
        override suspend fun createRental(vehicleId: String, plan: RentalPlan, endDate: String?): RentalResult<Rental> {
            requestedPlan = plan; requestedEndDate = endDate
            return RentalResult.Success(Rental(RENTAL_ID, "user-1", vehicleId, plan, "2026-07-14T10:00:00.000Z", endDate, null, "PREPARING", "2026-07-14T10:00:00.000Z"))
        }
        override suspend fun getMyRentals(): RentalResult<List<RentalSummary>> = RentalResult.Success(emptyList())
        override suspend fun getRentalHistory(): RentalResult<List<RentalHistoryItem>> =
            RentalResult.Success(emptyList())
        override suspend fun getRentalStats(): RentalResult<RentalStats> =
            RentalResult.Success(RentalStats(0, 0.0, 0.0, 0.0))
        override suspend fun uploadRentalPhoto(rentalId: String, side: RentalPhotoSide, imageUri: Uri): RentalResult<RentalPhotosState> =
            error("Not used")
        override suspend fun getRentalPhotos(rentalId: String): RentalResult<RentalPhotosState> = error("Not used")
        override suspend fun startRental(rentalId: String): RentalResult<Rental> = error("Not used")
        override suspend fun cancelRental(rentalId: String): RentalResult<Unit> = error("Not used")
        override suspend fun getActiveRental(): RentalResult<ActiveRental> = error("Not used")
        override suspend fun finishRental(rentalId: String): RentalResult<Rental> = error("Not used")
    }

    private class FakeReservationRepository : ReservationRepository {
        override suspend fun createReservation(vehicleId: String) = ReservationResult.Success(
            Reservation(
                id = "reservation-1",
                vehicleId = vehicleId,
                vehicle = ReservationVehicleSummary(
                    id = vehicleId,
                    plate = vehicle.plate,
                    brand = vehicle.brand,
                    model = vehicle.model,
                    type = vehicle.type,
                    latitude = vehicle.latitude,
                    longitude = vehicle.longitude,
                    pricePerMinute = vehicle.pricePerMinute
                ),
                status = ReservationStatus.ACTIVE,
                expiresAt = "2026-07-14T10:15:00.000Z",
                remainingSeconds = 900
            )
        )
        override suspend fun getActiveReservation(): ReservationResult<Reservation> = error("Not used")
        override suspend fun cancelReservation(id: String): ReservationResult<Unit> = error("Not used")
    }

    private companion object {
        const val VEHICLE_ID = "vehicle-1"
        const val RENTAL_ID = "rental-1"
        val vehicle = Vehicle(
            VEHICLE_ID, "34 RNC 022", "Renault", "Clio", VehicleType.HATCHBACK,
            1450.0, 4.5, 180.0, 72.0, 480.0, Transmission.MANUAL, 5,
            VehicleSegment.ECONOMY, VehicleStatus.AVAILABLE, 41.0, 29.0
        )
    }
}
