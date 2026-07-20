package com.turkcell.rencar.presentation.screen.home

import android.net.Uri
import com.turkcell.rencar.domain.rental.ActiveRental
import com.turkcell.rencar.domain.rental.PaymentMethod
import com.turkcell.rencar.domain.rental.PaymentReceipt
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
import com.turkcell.rencar.domain.reservation.ReservationError
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `active reservation fills home card state without automatic navigation`() = runTest {
        val viewModel = createViewModel()

        viewModel.onIntent(HomeIntent.ScreenStarted)
        advanceUntilIdle()

        assertEquals(VEHICLE_ID, viewModel.state.value.activeReservationVehicleId)
        assertEquals("Renault Clio", viewModel.state.value.activeReservationVehicleName)
        assertEquals("34 RNC 022", viewModel.state.value.activeReservationPlate)
        assertEquals(900, viewModel.state.value.activeReservationRemainingSeconds)
        assertEquals(4.5, viewModel.state.value.activeReservationPricePerMinute, 0.0)
        assertNull(withTimeoutOrNull(100) { viewModel.effect.first() })
    }

    @Test
    fun `active reservation card click navigates to car detail`() = runTest {
        val viewModel = createViewModel()

        viewModel.onIntent(HomeIntent.ScreenStarted)
        advanceUntilIdle()
        viewModel.onIntent(HomeIntent.ActiveReservationCardClicked)

        assertEquals(
            HomeEffect.NavigateToActiveReservationCarDetail(VEHICLE_ID),
            viewModel.effect.first()
        )
    }

    @Test
    fun `screen resume refreshes active reservation after initial empty state`() = runTest {
        val reservationRepository = FakeReservationRepository(
            mutableListOf(
                ReservationResult.Failure(ReservationError.NotFound),
                ReservationResult.Success(activeReservation)
            )
        )
        val viewModel = createViewModel(reservationRepository = reservationRepository)

        viewModel.onIntent(HomeIntent.ScreenStarted)
        advanceUntilIdle()
        assertNull(viewModel.state.value.activeReservationVehicleId)

        viewModel.onIntent(HomeIntent.ScreenResumed)
        advanceUntilIdle()

        assertEquals(VEHICLE_ID, viewModel.state.value.activeReservationVehicleId)
        assertEquals("Renault Clio", viewModel.state.value.activeReservationVehicleName)
    }

    private fun createViewModel(
        vehicleRepository: FakeVehicleRepository = FakeVehicleRepository(),
        reservationRepository: FakeReservationRepository = FakeReservationRepository(),
        rentalRepository: FakeRentalRepository = FakeRentalRepository()
    ) = HomeViewModel(vehicleRepository, reservationRepository, rentalRepository)

    private class FakeVehicleRepository : VehicleRepository {
        override suspend fun listAvailableVehicles(type: VehicleType?): VehicleResult<List<Vehicle>> =
            VehicleResult.Success(emptyList())

        override suspend fun getVehicle(id: String): VehicleResult<Vehicle> = VehicleResult.Success(vehicle)

        override suspend fun getQuote(id: String, plan: String, minutes: Int): VehicleResult<VehicleQuote> =
            error("Quote is not used by home")
    }

    private class FakeReservationRepository(
        private val results: MutableList<ReservationResult<Reservation>> =
            mutableListOf(ReservationResult.Success(activeReservation))
    ) : ReservationRepository {
        override suspend fun createReservation(vehicleId: String): ReservationResult<Reservation> =
            error("Not used by home")

        override suspend fun getActiveReservation(): ReservationResult<Reservation> =
            if (results.size > 1) results.removeAt(0) else results.first()

        override suspend fun cancelReservation(id: String): ReservationResult<Unit> =
            error("Not used by home")
    }

    private class FakeRentalRepository : RentalRepository {
        override suspend fun createRental(vehicleId: String, plan: RentalPlan, endDate: String?): RentalResult<Rental> =
            error("Not used by home")

        override suspend fun getMyRentals(): RentalResult<List<RentalSummary>> =
            RentalResult.Success(emptyList())

        override suspend fun getRentalHistory(): RentalResult<List<RentalHistoryItem>> =
            error("Not used by home")

        override suspend fun getRentalStats(): RentalResult<RentalStats> =
            error("Not used by home")

        override suspend fun uploadRentalPhoto(rentalId: String, side: RentalPhotoSide, imageUri: Uri): RentalResult<RentalPhotosState> =
            error("Not used by home")

        override suspend fun getRentalPhotos(rentalId: String): RentalResult<RentalPhotosState> =
            error("Not used by home")

        override suspend fun startRental(rentalId: String): RentalResult<Rental> =
            error("Not used by home")

        override suspend fun cancelRental(rentalId: String): RentalResult<Unit> =
            error("Not used by home")

        override suspend fun getActiveRental(): RentalResult<ActiveRental> =
            error("Not used by home")

        override suspend fun finishRental(rentalId: String): RentalResult<Rental> =
            error("Not used by home")

        override suspend fun getRentalDetail(rentalId: String): RentalResult<Rental> =
            error("Not used by home")

        override suspend fun payRental(
            rentalId: String,
            method: PaymentMethod,
            cardId: String?,
            discountCode: String?,
            iyzicoPaymentId: String?
        ): RentalResult<PaymentReceipt> =
            error("Not used by home")
    }

    private companion object {
        const val VEHICLE_ID = "vehicle-1"

        val vehicle = Vehicle(
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

        val activeReservation = Reservation(
            id = "reservation-1",
            vehicleId = VEHICLE_ID,
            vehicle = ReservationVehicleSummary(
                id = VEHICLE_ID,
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
    }
}
