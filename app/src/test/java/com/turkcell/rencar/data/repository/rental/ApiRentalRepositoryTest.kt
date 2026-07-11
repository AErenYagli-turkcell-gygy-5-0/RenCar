package com.turkcell.rencar.data.repository.rental

import com.turkcell.rencar.data.remote.rental.RentalApiService
import com.turkcell.rencar.data.remote.rental.dto.CreateRentalRequestDto
import com.turkcell.rencar.data.remote.rental.dto.RentalResponseDto
import com.turkcell.rencar.domain.rental.RentalResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiRentalRepositoryTest {

    @Test
    fun `create rental sends contract fields and maps response`() = runTest {
        val apiService = FakeRentalApiService()
        val repository = ApiRentalRepository(apiService)

        val result = repository.createRental(
            vehicleId = VEHICLE_ID,
            endDate = END_DATE
        )

        assertEquals(CreateRentalRequestDto(VEHICLE_ID, END_DATE), apiService.receivedRequest)
        assertTrue(result is RentalResult.Success)
        val rental = (result as RentalResult.Success).data
        assertEquals(RENTAL_ID, rental.id)
        assertEquals(VEHICLE_ID, rental.vehicleId)
        assertEquals(1450.0, rental.totalPrice, 0.0)
    }

    private class FakeRentalApiService : RentalApiService {
        var receivedRequest: CreateRentalRequestDto? = null

        override suspend fun create(request: CreateRentalRequestDto): RentalResponseDto {
            receivedRequest = request
            return RentalResponseDto(
                id = RENTAL_ID,
                userId = "user-1",
                vehicleId = request.vehicleId,
                startDate = "2026-07-10T10:00:00.000Z",
                endDate = request.endDate,
                totalPrice = 1450.0,
                status = "ACTIVE",
                createdAt = "2026-07-10T10:00:00.000Z"
            )
        }
    }

    private companion object {
        const val VEHICLE_ID = "vehicle-1"
        const val RENTAL_ID = "rental-1"
        const val END_DATE = "2026-07-11T10:00:00.000Z"
    }
}
