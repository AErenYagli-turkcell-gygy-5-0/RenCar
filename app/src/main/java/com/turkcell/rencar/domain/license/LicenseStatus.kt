package com.turkcell.rencar.domain.license

data class LicenseStatus(
    val reviewStatus: LicenseReviewStatus,
    val frontImageUrl: String?,
    val backImageUrl: String?,
    val rejectReason: String?,
    val reviewedAt: String?
)

enum class LicenseReviewStatus {
    NOT_SUBMITTED,
    UNDER_REVIEW,
    APPROVED,
    REJECTED
}
