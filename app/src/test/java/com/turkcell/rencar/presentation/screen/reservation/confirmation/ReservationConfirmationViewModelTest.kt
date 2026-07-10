package com.turkcell.rencar.presentation.screen.reservation.confirmation

import com.turkcell.rencar.domain.rental.Rental
import com.turkcell.rencar.domain.rental.RentalError
import com.turkcell.rencar.domain.rental.RentalRepository
import com.turkcell.rencar.domain.rental.RentalResult
import com.turkcell.rencar.domain.vehicle.Vehicle
import com.turkcell.rencar.domain.vehicle.VehicleRepository
import com.turkcell.rencar.domain.vehicle.VehicleResult
import com.turkcell.rencar.domain.vehicle.VehicleType
import com.turkcell.rencar.test.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReservationConfirmationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `screen started loads available vehicle details`() = runTest {
        val viewModel = ReservationConfirmationViewModel(
            vehicleRepository = FakeVehicleRepository(),
            rentalRepository = FakeRentalRepository()
        )

        viewModel.onIntent(ReservationConfirmationIntent.ScreenStarted(VEHICLE_ID))
        advanceUntilIdle()

        assertEquals("Renault Clio", viewModel.state.value.vehicleName)
        assertEquals("34 RNC 022", viewModel.state.value.plate)
        assertEquals(1450.0, viewModel.state.value.pricePerDay, 0.0)
        assertTrue(viewModel.state.value.hasLoaded)
        assertFalse(viewModel.state.value.canComplete)
    }

    @Test
    fun `accepted terms and complete click creates rental and emits id`() = runTest {
        val rentalRepository = FakeRentalRepository()
        val viewModel = ReservationConfirmationViewModel(
            vehicleRepository = FakeVehicleRepository(),
            rentalRepository = rentalRepository
        )

        viewModel.onIntent(ReservationConfirmationIntent.ScreenStarted(VEHICLE_ID))
        advanceUntilIdle()
        viewModel.onIntent(ReservationConfirmationIntent.TermsAcceptanceChanged(true))
        viewModel.onIntent(ReservationConfirmationIntent.CompleteReservationClicked)
        advanceUntilIdle()

        assertEquals(VEHICLE_ID, rentalRepository.requestedVehicleId)
        assertTrue(rentalRepository.requestedEndDate.orEmpty().endsWith("Z"))
        assertEquals(
            ReservationConfirmationEffect.ReservationCreated(RENTAL_ID),
            viewModel.effect.first()
        )
        assertFalse(viewModel.state.value.isSubmitting)
    }

    @Test
    fun `rental conflict is exposed and can be retried`() = runTest {
        val viewModel = ReservationConfirmationViewModel(
            vehicleRepository = FakeVehicleRepository(),
            rentalRepository = FakeRentalRepository(
                result = RentalResult.Failure(RentalError.Conflict)
            )
        )

        viewModel.onIntent(ReservationConfirmationIntent.ScreenStarted(VEHICLE_ID))
        advanceUntilIdle()
        viewModel.onIntent(ReservationConfirmationIntent.TermsAcceptanceChanged(true))
        viewModel.onIntent(ReservationConfirmationIntent.CompleteReservationClicked)
        advanceUntilIdle()

        assertEquals(ReservationConfirmationError.RESERVATION_CONFLICT, viewModel.state.value.error)
        assertTrue(viewModel.state.value.canComplete)
    }

    private class FakeVehicleRepository : VehicleRepository {
        private val vehicle = Vehicle(
            id = VEHICLE_ID,
            plate = "34 RNC 022",
            brand = "Renault",
            model = "Clio",
            type = VehicleType.HATCHBACK,
            pricePerDay = 1450.0,
            latitude = 41.0,
            longitude = 29.0
        )

        override suspend fun listAvailableVehicles(type: VehicleType?): VehicleResult<List<Vehicle>> =
            VehicleResult.Success(listOf(vehicle))

        override suspend fun getAvailableVehicle(vehicleId: String): VehicleResult<Vehicle> =
            VehicleResult.Success(vehicle)
    }

    private class FakeRentalRepository(
        private val result: RentalResult<Rental> = RentalResult.Success(
            Rental(
                id = RENTAL_ID,
                userId = "user-1",
                vehicleId = VEHICLE_ID,
                startDate = "2026-07-10T10:00:00.000Z",
                endDate = "2026-07-11T10:00:00.000Z",
                totalPrice = 1450.0,
                status = "ACTIVE",
                createdAt = "2026-07-10T10:00:00.000Z"
            )
        )
    ) : RentalRepository {
        var requestedVehicleId: String? = null
        var requestedEndDate: String? = null

        override suspend fun createRental(vehicleId: String, endDate: String): RentalResult<Rental> {
            requestedVehicleId = vehicleId
            requestedEndDate = endDate
            return result
        }
    }

    private companion object {
        const val VEHICLE_ID = "vehicle-1"
        const val RENTAL_ID = "rental-1"
    }
}
