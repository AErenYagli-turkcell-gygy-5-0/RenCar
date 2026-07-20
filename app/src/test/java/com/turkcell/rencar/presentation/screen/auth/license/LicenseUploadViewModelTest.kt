package com.turkcell.rencar.presentation.screen.auth.license

import com.turkcell.rencar.domain.auth.AuthError
import com.turkcell.rencar.domain.auth.AuthRepository
import com.turkcell.rencar.domain.auth.AuthResult
import com.turkcell.rencar.domain.auth.LoginChallenge
import com.turkcell.rencar.domain.auth.RegisterRequest
import com.turkcell.rencar.domain.auth.RegisteredUser
import com.turkcell.rencar.domain.auth.VerifiedSession
import com.turkcell.rencar.domain.license.LicenseError
import com.turkcell.rencar.domain.license.LicenseRepository
import com.turkcell.rencar.domain.license.LicenseResult
import com.turkcell.rencar.domain.license.LicenseReviewStatus
import com.turkcell.rencar.domain.license.LicenseStatus
import com.turkcell.rencar.domain.license.UploadedLicense
import com.turkcell.rencar.domain.profile.ProfilePhotoRepository
import com.turkcell.rencar.test.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LicenseUploadViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `under review user opens approval step`() = runTest {
        val licenseRepository = FakeLicenseRepository(LicenseReviewStatus.UNDER_REVIEW)
        val viewModel = LicenseUploadViewModel(
            licenseRepository,
            FakeAuthRepository(),
            FakeProfilePhotoRepository()
        )

        advanceUntilIdle()

        assertEquals(LicenseVerificationStep.APPROVAL, viewModel.state.value.currentStep)
        assertEquals(0, licenseRepository.uploadCount)
    }

    @Test
    fun `license upload is blocked until required images are selected`() = runTest {
        val licenseRepository = FakeLicenseRepository(LicenseReviewStatus.NOT_SUBMITTED)
        val viewModel = LicenseUploadViewModel(
            licenseRepository,
            FakeAuthRepository(),
            FakeProfilePhotoRepository()
        )
        advanceUntilIdle()

        viewModel.onIntent(LicenseUploadIntent.ContinueClicked)

        assertEquals(LicenseVerificationStep.LICENSE, viewModel.state.value.currentStep)
        assertEquals(0, licenseRepository.uploadCount)
        assertEquals(
            "Devam etmeden önce ön ve arka yüz fotoğraflarını seçmelisiniz.",
            viewModel.state.value.errorMessage
        )
    }

    @Test
    fun `approved status refreshes customer session and navigates home`() = runTest {
        val licenseRepository = FakeLicenseRepository(LicenseReviewStatus.APPROVED)
        val viewModel = LicenseUploadViewModel(
            licenseRepository,
            FakeAuthRepository(),
            FakeProfilePhotoRepository()
        )

        advanceUntilIdle()

        assertEquals(LicenseUploadEffect.NavigateHome, viewModel.effect.first())
    }

    @Test
    fun `selfie is stored as profile photo for current user`() = runTest {
        val profilePhotoRepository = FakeProfilePhotoRepository()
        val viewModel = LicenseUploadViewModel(
            FakeLicenseRepository(LicenseReviewStatus.NOT_SUBMITTED),
            FakeAuthRepository(),
            profilePhotoRepository
        )
        val selfiePreview = byteArrayOf(1, 2, 3)
        advanceUntilIdle()

        viewModel.saveSelfieAsProfilePhoto(selfiePreview)

        assertEquals("user-1", profilePhotoRepository.savedUserId)
        assertArrayEquals(selfiePreview, profilePhotoRepository.savedPhoto)
    }

    private class FakeLicenseRepository(
        initialStatus: LicenseReviewStatus
    ) : LicenseRepository {
        var uploadCount = 0
        var status = initialStatus

        override suspend fun uploadLicense(
            frontImageUri: android.net.Uri,
            backImageUri: android.net.Uri,
            selfieJpegBytes: ByteArray
        ): LicenseResult<UploadedLicense> {
            uploadCount++
            status = LicenseReviewStatus.UNDER_REVIEW
            return LicenseResult.Success(
                UploadedLicense("license-1", "UNDER_REVIEW", "front.jpg", "back.jpg")
            )
        }

        override suspend fun getLicenseStatus(): LicenseResult<LicenseStatus> =
            LicenseResult.Success(
                LicenseStatus(status, null, null, null, null)
            )
    }

    private class FakeAuthRepository : AuthRepository {
        override suspend fun register(
            request: RegisterRequest
        ): AuthResult<RegisteredUser> = AuthResult.Failure(AuthError.Unexpected)

        override suspend fun requestLogin(
            phone: String
        ): AuthResult<LoginChallenge> = AuthResult.Failure(AuthError.Unexpected)

        override suspend fun verifyOtp(
            phone: String,
            code: String
        ): AuthResult<VerifiedSession> = AuthResult.Failure(AuthError.Unexpected)

        override suspend fun refreshSession(): AuthResult<VerifiedSession> =
            AuthResult.Success(
                VerifiedSession(
                    user = RegisteredUser(
                        id = "user-1",
                        email = "user@example.com",
                        phone = "+905320000000",
                        fullName = "Test User",
                        role = "CUSTOMER",
                        referralCode = null,
                        createdAt = "2026-07-04T10:00:00.000Z",
                        updatedAt = "2026-07-04T10:00:00.000Z"
                    ),
                    accessToken = "access",
                    refreshToken = "refresh"
                )
            )

        override suspend fun getCurrentUser(): AuthResult<RegisteredUser> =
            AuthResult.Success(
                RegisteredUser(
                    id = "user-1",
                    email = "user@example.com",
                    phone = "+905320000000",
                    fullName = "Test User",
                    role = "PENDING",
                    referralCode = null,
                    createdAt = "2026-07-04T10:00:00.000Z",
                    updatedAt = "2026-07-04T10:00:00.000Z"
                )
            )

        override suspend fun logout(): AuthResult<Unit> =
            AuthResult.Failure(AuthError.Unexpected)
    }

    private class FakeProfilePhotoRepository : ProfilePhotoRepository {
        var savedUserId: String? = null
        var savedPhoto: ByteArray? = null

        override suspend fun saveProfilePhoto(userId: String, jpegBytes: ByteArray) {
            savedUserId = userId
            savedPhoto = jpegBytes
        }

        override suspend fun getProfilePhoto(userId: String): ByteArray? = null
    }
}
