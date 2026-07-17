package com.turkcell.rencar.presentation.screen.reservation.confirmation

import com.turkcell.rencar.domain.rental.RentalPlan
import com.turkcell.rencar.domain.reservation.Reservation
import com.turkcell.rencar.domain.reservation.ReservationPlanStore
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
        assertTrue(viewModel.state.value.hasLoaded)
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
    fun `accepted terms creates reservation only and saves selected plan`() = runTest {
        val reservationRepository = FakeReservationRepository()
        val reservationPlanStore = FakeReservationPlanStore()
        val viewModel = createViewModel(
            reservationRepository = reservationRepository,
            reservationPlanStore = reservationPlanStore
        )
        viewModel.onIntent(ReservationConfirmationIntent.ScreenStarted(VEHICLE_ID))
        advanceUntilIdle()
        viewModel.onIntent(ReservationConfirmationIntent.PlanSelected(RentalPlan.HOURLY))
        advanceUntilIdle()
        viewModel.onIntent(ReservationConfirmationIntent.TermsAcceptanceChanged(true))
        viewModel.onIntent(ReservationConfirmationIntent.CompleteReservationClicked)
        advanceUntilIdle()

        assertEquals(VEHICLE_ID, reservationRepository.createdVehicleId)
        assertEquals(RentalPlan.HOURLY, reservationPlanStore.getPlan(VEHICLE_ID))
        assertEquals(
            ReservationConfirmationEffect.ReservationCreated(VEHICLE_ID),
            viewModel.effect.first()
        )
    }

    private fun createViewModel(
        vehicleRepository: FakeVehicleRepository = FakeVehicleRepository(),
        reservationRepository: FakeReservationRepository = FakeReservationRepository(),
        reservationPlanStore: FakeReservationPlanStore = FakeReservationPlanStore()
    ) = ReservationConfirmationViewModel(vehicleRepository, reservationRepository, reservationPlanStore)

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

    private class FakeReservationRepository : ReservationRepository {
        var createdVehicleId: String? = null

        override suspend fun createReservation(vehicleId: String): ReservationResult<Reservation> {
            createdVehicleId = vehicleId
            return ReservationResult.Success(activeReservation(vehicleId))
        }

        override suspend fun getActiveReservation(): ReservationResult<Reservation> = error("Not used")
        override suspend fun cancelReservation(id: String): ReservationResult<Unit> = error("Not used")
    }

    private class FakeReservationPlanStore : ReservationPlanStore {
        private val plans = mutableMapOf<String, RentalPlan>()

        override fun savePlan(vehicleId: String, plan: RentalPlan) {
            plans[vehicleId] = plan
        }

        override fun getPlan(vehicleId: String): RentalPlan? = plans[vehicleId]

        override fun clearPlan(vehicleId: String) {
            plans.remove(vehicleId)
        }
    }

    private companion object {
        const val VEHICLE_ID = "vehicle-1"

        val vehicle = Vehicle(
            VEHICLE_ID, "34 RNC 022", "Renault", "Clio", VehicleType.HATCHBACK,
            1450.0, 4.5, 180.0, 72.0, 480.0, Transmission.MANUAL, 5,
            VehicleSegment.ECONOMY, VehicleStatus.AVAILABLE, 41.0, 29.0
        )

        fun activeReservation(vehicleId: String) = Reservation(
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
    }
}
