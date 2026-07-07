package com.turkcell.rencar.data.repository.license

import com.turkcell.rencar.data.remote.license.dto.LicenseStatusResponseDto
import com.turkcell.rencar.domain.license.LicenseReviewStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ApiLicenseRepositoryTest {

    @Test
    fun `status response maps approved state`() {
        val status = LicenseStatusResponseDto(
            status = "APPROVED",
            frontImageUrl = "front.jpg",
            backImageUrl = "back.jpg",
            rejectReason = null,
            reviewedAt = "2026-07-04T10:00:00.000Z"
        )
            .toDomain()

        assertEquals(LicenseReviewStatus.APPROVED, status.reviewStatus)
        assertNull(status.rejectReason)
    }
}
