package com.turkcell.rencar.data.repository.rental

import com.turkcell.rencar.data.remote.rental.dto.ActiveRentalResponseDto
import com.turkcell.rencar.data.remote.rental.dto.RentalPhotoDto
import com.turkcell.rencar.data.remote.rental.dto.RentalPhotosStateResponseDto
import com.turkcell.rencar.data.remote.rental.dto.RentalResponseDto
import com.turkcell.rencar.data.remote.rental.dto.RentalSummaryResponseDto
import com.turkcell.rencar.domain.rental.RentalPhotoSide
import com.turkcell.rencar.domain.rental.RentalPlan
import com.turkcell.rencar.domain.rental.RentalStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiRentalRepositoryTest {

    @Test
    fun `rental response with values maps to domain`() {
        val rental = RentalResponseDto(
            id = RENTAL_ID,
            userId = "user-1",
            vehicleId = VEHICLE_ID,
            plan = RentalPlan.DAILY.name,
            startDate = "2026-07-10T10:00:00.000Z",
            endDate = "2026-07-11T10:00:00.000Z",
            totalPrice = 1450.0,
            status = "ACTIVE",
            createdAt = "2026-07-10T10:00:00.000Z"
        ).toDomain()

        assertEquals(RENTAL_ID, rental.id)
        assertEquals(VEHICLE_ID, rental.vehicleId)
        assertEquals(RentalPlan.DAILY, rental.plan)
        assertEquals(1450.0, rental.totalPrice ?: 0.0, 0.0)
    }

    @Test
    fun `rental response with null endDate and totalPrice maps to domain without crashing`() {
        val rental = RentalResponseDto(
            id = RENTAL_ID,
            userId = "user-1",
            vehicleId = VEHICLE_ID,
            plan = RentalPlan.PER_MINUTE.name,
            startDate = "2026-07-10T10:00:00.000Z",
            endDate = null,
            totalPrice = null,
            status = "PREPARING",
            createdAt = "2026-07-10T10:00:00.000Z"
        ).toDomain()

        assertNull(rental.endDate)
        assertNull(rental.totalPrice)
        assertEquals("PREPARING", rental.status)
        assertEquals(RentalPlan.PER_MINUTE, rental.plan)
    }

    @Test
    fun `rental summary with null vehicleId is dropped`() {
        val summary = RentalSummaryResponseDto(
            id = RENTAL_ID,
            vehicleId = null,
            status = "ACTIVE"
        ).toDomainOrNull()

        assertNull(summary)
    }

    @Test
    fun `rental summary with unknown status defaults to completed`() {
        val summary = RentalSummaryResponseDto(
            id = RENTAL_ID,
            vehicleId = VEHICLE_ID,
            status = "SOMETHING_UNKNOWN"
        ).toDomainOrNull()

        assertEquals(RentalStatus.COMPLETED, summary?.status)
    }

    @Test
    fun `photos state maps uploaded and remaining sides`() {
        val state = RentalPhotosStateResponseDto(
            rentalId = RENTAL_ID,
            photos = listOf(
                RentalPhotoDto(side = "FRONT", imageUrl = "front.jpg", createdAt = "2026-07-10T10:00:00.000Z"),
                RentalPhotoDto(side = "BACK", imageUrl = "back.jpg", createdAt = "2026-07-10T10:00:00.000Z")
            ),
            uploadedCount = 2,
            remainingSides = listOf("LEFT", "RIGHT"),
            photosComplete = false
        ).toDomain()

        assertEquals(setOf(RentalPhotoSide.FRONT, RentalPhotoSide.BACK), state.uploadedSides)
        assertEquals(setOf(RentalPhotoSide.LEFT, RentalPhotoSide.RIGHT), state.remainingSides)
        assertTrue(!state.photosComplete)
    }

    @Test
    fun `active rental maps elapsed seconds and current cost`() {
        val activeRental = ActiveRentalResponseDto(
            id = RENTAL_ID,
            vehicleId = VEHICLE_ID,
            status = "ACTIVE",
            plan = RentalPlan.PER_MINUTE.name,
            startFee = 15.0,
            startedAt = "2026-07-10T10:00:00.000Z",
            elapsedSeconds = 1264L,
            currentCost = 156.5,
            distanceKm = 12.4
        ).toDomain()

        assertEquals(RENTAL_ID, activeRental.id)
        assertEquals(RentalStatus.ACTIVE, activeRental.status)
        assertEquals(RentalPlan.PER_MINUTE, activeRental.plan)
        assertEquals(15.0, activeRental.startFee, 0.0)
        assertEquals("2026-07-10T10:00:00.000Z", activeRental.startedAt)
        assertEquals(1264L, activeRental.elapsedSeconds)
        assertEquals(156.5, activeRental.currentCost, 0.0)
        assertEquals(12.4, activeRental.distanceKm, 0.0)
    }

    private companion object {
        const val VEHICLE_ID = "vehicle-1"
        const val RENTAL_ID = "rental-1"
    }
}
