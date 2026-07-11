package com.turkcell.rencar.presentation.screen.cardetail

import androidx.lifecycle.SavedStateHandle
import com.turkcell.rencar.domain.vehicle.Vehicle
import com.turkcell.rencar.domain.vehicle.VehicleRepository
import com.turkcell.rencar.domain.vehicle.VehicleResult
import com.turkcell.rencar.domain.vehicle.VehicleType
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

        override suspend fun getVehicle(id: String): VehicleResult<Vehicle> =
            VehicleResult.Success(vehicle)
    }

    private companion object {
        const val VEHICLE_ID = "vehicle-1"
    }
}
