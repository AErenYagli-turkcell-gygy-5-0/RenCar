package com.turkcell.rencar.presentation.screen.cardetail

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
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
import com.turkcell.rencar.domain.reservation.ReservationPlanStore
import com.turkcell.rencar.domain.reservation.ReservationRepository
import com.turkcell.rencar.domain.reservation.ReservationResult
import com.turkcell.rencar.domain.reservation.ReservationStatus
import com.turkcell.rencar.domain.reservation.ReservationVehicleSummary
import com.turkcell.rencar.domain.vehicle.Transmission
import com.turkcell.rencar.domain.vehicle.Vehicle
import com.turkcell.rencar.domain.vehicle.VehicleError
import com.turkcell.rencar.domain.vehicle.VehicleQuote
import com.turkcell.rencar.domain.vehicle.VehicleRepository
import com.turkcell.rencar.domain.vehicle.VehicleResult
import com.turkcell.rencar.domain.vehicle.VehicleSegment
import com.turkcell.rencar.domain.vehicle.VehicleStatus
import com.turkcell.rencar.domain.vehicle.VehicleType
import com.turkcell.rencar.presentation.navigation.RenCarDestination
import com.turkcell.rencar.test.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CarDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `reserve click emits selected vehicle id after vehicle loads`() = runTest {
        val viewModel = createViewModel()

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
    fun `active reservation unlock creates minute rental and navigates to photo upload when preparing`() = runTest {
        val rentalRepository = FakeRentalRepository(createdStatus = "PREPARING")
        val reservationPlanStore = FakeReservationPlanStore(RentalPlan.HOURLY)
        val viewModel = createViewModel(
            vehicleRepository = FakeVehicleRepository(vehicleResult = VehicleResult.Failure(VehicleError.NotFound)),
            rentalRepository = rentalRepository,
            reservationRepository = FakeReservationRepository(),
            reservationPlanStore = reservationPlanStore
        )

        viewModel.onIntent(CarDetailIntent.ScreenStarted)
        advanceUntilIdle()
        viewModel.onIntent(CarDetailIntent.UnlockClicked)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isActiveReservationVehicle)
        assertTrue(viewModel.state.value.canUnlock)
        assertEquals(RentalPlan.HOURLY, rentalRepository.requestedPlan)
        assertEquals(VEHICLE_ID, rentalRepository.requestedVehicleId)
        assertTrue(reservationPlanStore.wasCleared)
        assertEquals(
            CarDetailEffect.NavigateToRentalPhotoUpload(RENTAL_ID, VEHICLE_ID),
            viewModel.effect.first()
        )
    }

    @Test
    fun `active reservation unlock navigates to active rental when rental starts active`() = runTest {
        val rentalRepository = FakeRentalRepository(createdStatus = "ACTIVE")
        val viewModel = createViewModel(
            vehicleRepository = FakeVehicleRepository(vehicleResult = VehicleResult.Failure(VehicleError.NotFound)),
            rentalRepository = rentalRepository,
            reservationRepository = FakeReservationRepository()
        )

        viewModel.onIntent(CarDetailIntent.ScreenStarted)
        advanceUntilIdle()
        viewModel.onIntent(CarDetailIntent.UnlockClicked)
        advanceUntilIdle()

        assertEquals(RentalPlan.PER_MINUTE, rentalRepository.requestedPlan)
        assertEquals(
            CarDetailEffect.NavigateToActiveRental(RENTAL_ID, VEHICLE_ID),
            viewModel.effect.first()
        )
    }

    @Test
    fun `reserved vehicle detail enables unlock for active reservation`() = runTest {
        val rentalRepository = FakeRentalRepository(createdStatus = "PREPARING")
        val viewModel = createViewModel(
            vehicleRepository = FakeVehicleRepository(
                vehicleResult = VehicleResult.Success(vehicle.copy(status = VehicleStatus.RESERVED))
            ),
            rentalRepository = rentalRepository,
            reservationRepository = FakeReservationRepository()
        )

        viewModel.onIntent(CarDetailIntent.ScreenStarted)
        advanceUntilIdle()
        viewModel.onIntent(CarDetailIntent.UnlockClicked)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isActiveReservationVehicle)
        assertTrue(viewModel.state.value.canUnlock)
        assertEquals(RentalPlan.PER_MINUTE, rentalRepository.requestedPlan)
        assertEquals(
            CarDetailEffect.NavigateToRentalPhotoUpload(RENTAL_ID, VEHICLE_ID),
            viewModel.effect.first()
        )
    }

    @Test
    fun `cancel reservation clicked opens confirm dialog without calling repository`() = runTest {
        val reservationRepository = FakeReservationRepository()
        val viewModel = createViewModel(
            vehicleRepository = FakeVehicleRepository(vehicleResult = VehicleResult.Failure(VehicleError.NotFound)),
            reservationRepository = reservationRepository
        )

        viewModel.onIntent(CarDetailIntent.ScreenStarted)
        advanceUntilIdle()
        viewModel.onIntent(CarDetailIntent.CancelReservationClicked)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.showCancelReservationConfirmDialog)
        assertNull(reservationRepository.cancelledReservationId)
    }

    @Test
    fun `cancel reservation dismissed closes confirm dialog without calling repository`() = runTest {
        val reservationRepository = FakeReservationRepository()
        val viewModel = createViewModel(
            vehicleRepository = FakeVehicleRepository(vehicleResult = VehicleResult.Failure(VehicleError.NotFound)),
            reservationRepository = reservationRepository
        )

        viewModel.onIntent(CarDetailIntent.ScreenStarted)
        advanceUntilIdle()
        viewModel.onIntent(CarDetailIntent.CancelReservationClicked)
        advanceUntilIdle()
        viewModel.onIntent(CarDetailIntent.CancelReservationDismissed)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.showCancelReservationConfirmDialog)
        assertNull(reservationRepository.cancelledReservationId)
    }

    @Test
    fun `active reservation cancel confirmed calls repository and clears reservation state`() = runTest {
        val reservationRepository = FakeReservationRepository()
        val reservationPlanStore = FakeReservationPlanStore(RentalPlan.HOURLY)
        val viewModel = createViewModel(
            vehicleRepository = FakeVehicleRepository(vehicleResult = VehicleResult.Failure(VehicleError.NotFound)),
            reservationRepository = reservationRepository,
            reservationPlanStore = reservationPlanStore
        )

        viewModel.onIntent(CarDetailIntent.ScreenStarted)
        advanceUntilIdle()
        viewModel.onIntent(CarDetailIntent.CancelReservationClicked)
        advanceUntilIdle()
        viewModel.onIntent(CarDetailIntent.CancelReservationConfirmed)
        advanceUntilIdle()

        assertEquals(RESERVATION_ID, reservationRepository.cancelledReservationId)
        assertTrue(reservationPlanStore.wasCleared)
        assertFalse(viewModel.state.value.isActiveReservationVehicle)
        assertFalse(viewModel.state.value.canUnlock)
        assertNull(viewModel.state.value.activeReservationId)
        assertFalse(viewModel.state.value.showCancelReservationConfirmDialog)
    }

    private fun createViewModel(
        vehicleRepository: FakeVehicleRepository = FakeVehicleRepository(),
        rentalRepository: FakeRentalRepository = FakeRentalRepository(),
        reservationRepository: FakeReservationRepository = FakeReservationRepository(),
        reservationPlanStore: FakeReservationPlanStore = FakeReservationPlanStore()
    ) = CarDetailViewModel(
        vehicleRepository = vehicleRepository,
        rentalRepository = rentalRepository,
        reservationRepository = reservationRepository,
        reservationPlanStore = reservationPlanStore,
        savedStateHandle = SavedStateHandle(
            mapOf(RenCarDestination.ARG_VEHICLE_ID to VEHICLE_ID)
        )
    )

    private class FakeRentalRepository(
        private val createdStatus: String = "PREPARING"
    ) : RentalRepository {
        var requestedVehicleId: String? = null
        var requestedPlan: RentalPlan? = null

        override suspend fun createRental(vehicleId: String, plan: RentalPlan, endDate: String?): RentalResult<Rental> {
            requestedVehicleId = vehicleId
            requestedPlan = plan
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
            discountCode: String?,
            iyzicoPaymentId: String?
        ): RentalResult<PaymentReceipt> =
            error("Not used by car detail")
    }

    private class FakeReservationRepository : ReservationRepository {
        var cancelledReservationId: String? = null

        override suspend fun createReservation(vehicleId: String): ReservationResult<Reservation> =
            error("Not used by this test")

        override suspend fun getActiveReservation(): ReservationResult<Reservation> =
            ReservationResult.Success(activeReservation)

        override suspend fun cancelReservation(id: String): ReservationResult<Unit> {
            cancelledReservationId = id
            return ReservationResult.Success(Unit)
        }
    }

    private class FakeReservationPlanStore(
        private val plan: RentalPlan? = null
    ) : ReservationPlanStore {
        var wasCleared = false

        override fun savePlan(vehicleId: String, plan: RentalPlan) = Unit

        override fun getPlan(vehicleId: String): RentalPlan? = plan

        override fun clearPlan(vehicleId: String) {
            wasCleared = true
        }
    }

    private class FakeVehicleRepository(
        private val vehicleResult: VehicleResult<Vehicle> = VehicleResult.Success(vehicle)
    ) : VehicleRepository {
        override suspend fun listAvailableVehicles(type: VehicleType?): VehicleResult<List<Vehicle>> =
            VehicleResult.Success(listOf(vehicle))

        override suspend fun getVehicle(id: String): VehicleResult<Vehicle> = vehicleResult

        override suspend fun getQuote(id: String, plan: String, minutes: Int): VehicleResult<VehicleQuote> =
            error("Quote is not used by car detail")
    }

    private companion object {
        const val VEHICLE_ID = "vehicle-1"
        const val RENTAL_ID = "rental-1"
        const val RESERVATION_ID = "reservation-1"

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
            id = RESERVATION_ID,
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
