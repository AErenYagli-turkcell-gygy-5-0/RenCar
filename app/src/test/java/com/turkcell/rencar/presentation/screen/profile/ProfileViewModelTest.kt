package com.turkcell.rencar.presentation.screen.profile

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
import com.turkcell.rencar.test.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `screen started loads user and approved license status`() = runTest {
        val viewModel = ProfileViewModel(
            authRepository = FakeAuthRepository(),
            licenseRepository = FakeLicenseRepository(LicenseReviewStatus.APPROVED)
        )

        viewModel.onIntent(ProfileIntent.ScreenStarted)
        advanceUntilIdle()

        assertEquals("Deniz Yilmaz", viewModel.state.value.fullName)
        assertEquals("+905320000000", viewModel.state.value.phone)
        assertEquals(LicenseReviewStatus.APPROVED, viewModel.state.value.licenseStatus)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `license status failure keeps user visible and exposes error`() = runTest {
        val viewModel = ProfileViewModel(
            authRepository = FakeAuthRepository(),
            licenseRepository = FakeLicenseRepository(error = LicenseError.Network)
        )

        viewModel.onIntent(ProfileIntent.ScreenStarted)
        advanceUntilIdle()

        assertEquals("Deniz Yilmaz", viewModel.state.value.fullName)
        assertEquals(null, viewModel.state.value.licenseStatus)
        assertEquals(
            "Internet baglantinizi kontrol edip tekrar deneyin.",
            viewModel.state.value.errorMessage
        )
    }

    @Test
    fun `unauthorized user load navigates to login`() = runTest {
        val viewModel = ProfileViewModel(
            authRepository = FakeAuthRepository(userResult = AuthResult.Failure(AuthError.Unauthorized)),
            licenseRepository = FakeLicenseRepository(LicenseReviewStatus.APPROVED)
        )

        viewModel.onIntent(ProfileIntent.ScreenStarted)
        advanceUntilIdle()

        assertEquals(ProfileEffect.NavigateToLogin, viewModel.effect.first())
    }

    @Test
    fun `logout click waits for confirmation and confirmed logout navigates to login`() = runTest {
        val authRepository = FakeAuthRepository()
        val viewModel = ProfileViewModel(
            authRepository = authRepository,
            licenseRepository = FakeLicenseRepository(LicenseReviewStatus.APPROVED)
        )

        viewModel.onIntent(ProfileIntent.LogoutClicked)
        advanceUntilIdle()

        assertEquals(0, authRepository.logoutCount)
        assertEquals(true, viewModel.state.value.showLogoutConfirmation)

        viewModel.onIntent(ProfileIntent.LogoutConfirmed)
        advanceUntilIdle()

        assertEquals(1, authRepository.logoutCount)
        assertEquals(ProfileEffect.NavigateToLogin, viewModel.effect.first())
    }

    @Test
    fun `logout confirmation can be dismissed without logout`() = runTest {
        val authRepository = FakeAuthRepository()
        val viewModel = ProfileViewModel(
            authRepository = authRepository,
            licenseRepository = FakeLicenseRepository(LicenseReviewStatus.APPROVED)
        )

        viewModel.onIntent(ProfileIntent.LogoutClicked)
        viewModel.onIntent(ProfileIntent.LogoutConfirmationDismissed)
        advanceUntilIdle()

        assertEquals(0, authRepository.logoutCount)
        assertEquals(false, viewModel.state.value.showLogoutConfirmation)
    }

    private class FakeAuthRepository(
        var userResult: AuthResult<RegisteredUser> = AuthResult.Success(
            RegisteredUser(
                id = "user-1",
                email = "deniz@example.com",
                phone = "+905320000000",
                fullName = "Deniz Yilmaz",
                role = "CUSTOMER",
                createdAt = "2026-07-07T10:00:00.000Z",
                updatedAt = "2026-07-07T10:00:00.000Z"
            )
        ),
        var logoutResult: AuthResult<Unit> = AuthResult.Success(Unit)
    ) : AuthRepository {
        var logoutCount = 0

        override suspend fun register(request: RegisterRequest): AuthResult<RegisteredUser> =
            AuthResult.Failure(AuthError.Unexpected)

        override suspend fun requestLogin(phone: String): AuthResult<LoginChallenge> =
            AuthResult.Failure(AuthError.Unexpected)

        override suspend fun verifyOtp(phone: String, code: String): AuthResult<VerifiedSession> =
            AuthResult.Failure(AuthError.Unexpected)

        override suspend fun refreshSession(): AuthResult<VerifiedSession> =
            AuthResult.Failure(AuthError.Unexpected)

        override suspend fun getCurrentUser(): AuthResult<RegisteredUser> = userResult

        override suspend fun logout(): AuthResult<Unit> {
            logoutCount++
            return logoutResult
        }
    }

    private class FakeLicenseRepository(
        private val status: LicenseReviewStatus? = null,
        private val error: LicenseError? = null
    ) : LicenseRepository {
        override suspend fun uploadLicense(
            frontImageUri: android.net.Uri,
            backImageUri: android.net.Uri
        ): LicenseResult<UploadedLicense> =
            LicenseResult.Failure(LicenseError.Unexpected)

        override suspend fun getLicenseStatus(): LicenseResult<LicenseStatus> {
            error?.let { return LicenseResult.Failure(it) }
            return LicenseResult.Success(
                LicenseStatus(
                    reviewStatus = status ?: LicenseReviewStatus.NOT_SUBMITTED,
                    frontImageUrl = null,
                    backImageUrl = null,
                    rejectReason = null,
                    reviewedAt = null
                )
            )
        }
    }
}
