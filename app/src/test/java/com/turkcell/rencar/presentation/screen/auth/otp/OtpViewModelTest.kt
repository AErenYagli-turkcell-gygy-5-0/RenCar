package com.turkcell.rencar.presentation.screen.auth.otp

import androidx.lifecycle.SavedStateHandle
import com.turkcell.rencar.domain.auth.AuthError
import com.turkcell.rencar.domain.auth.AuthRepository
import com.turkcell.rencar.domain.auth.AuthResult
import com.turkcell.rencar.domain.auth.LoginChallenge
import com.turkcell.rencar.domain.auth.RegisterRequest
import com.turkcell.rencar.domain.auth.RegisteredUser
import com.turkcell.rencar.domain.auth.VerifiedSession
import com.turkcell.rencar.presentation.navigation.RenCarDestination
import com.turkcell.rencar.test.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.TestScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OtpViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `pending user navigates to license verification`() = runTest {
        val viewModel = createViewModel(role = "PENDING")

        verify(viewModel)

        assertEquals(OtpEffect.NavigateToLicenseVerification, viewModel.effect.first())
    }

    @Test
    fun `customer user navigates to home`() = runTest {
        val viewModel = createViewModel(role = "CUSTOMER")

        verify(viewModel)

        assertEquals(OtpEffect.NavigateToHome, viewModel.effect.first())
    }

    @Test
    fun `admin user remains on otp with unsupported role error`() = runTest {
        val viewModel = createViewModel(role = "ADMIN")

        verify(viewModel)

        assertEquals(
            "Bu hesap müşteri uygulaması için uygun değil.",
            viewModel.state.value.errorMessage
        )
        assertFalse(viewModel.state.value.isVerifying)
    }

    private fun TestScope.verify(viewModel: OtpViewModel) {
        viewModel.onIntent(OtpIntent.DigitsChanged("123456"))
        viewModel.onIntent(OtpIntent.VerifyClicked)
        advanceUntilIdle()
    }

    private fun createViewModel(role: String): OtpViewModel {
        val repository = FakeAuthRepository(role)
        return OtpViewModel(
            authRepository = repository,
            savedStateHandle = SavedStateHandle(
                mapOf(RenCarDestination.ARG_PHONE_NUMBER to "+905320000000")
            )
        )
    }

    private class FakeAuthRepository(private val role: String) : AuthRepository {
        override suspend fun register(
            request: RegisterRequest
        ): AuthResult<RegisteredUser> = AuthResult.Failure(AuthError.Unexpected)

        override suspend fun requestLogin(
            phone: String
        ): AuthResult<LoginChallenge> = AuthResult.Failure(AuthError.Unexpected)

        override suspend fun verifyOtp(
            phone: String,
            code: String
        ): AuthResult<VerifiedSession> = AuthResult.Success(session(role))

        override suspend fun refreshSession(): AuthResult<VerifiedSession> =
            AuthResult.Failure(AuthError.Unexpected)
    }
}

private fun session(role: String) = VerifiedSession(
    user = RegisteredUser(
        id = "user-1",
        email = "user@example.com",
        phone = "+905320000000",
        fullName = "Test User",
        role = role,
        createdAt = "2026-07-04T10:00:00.000Z",
        updatedAt = "2026-07-04T10:00:00.000Z"
    ),
    accessToken = "access",
    refreshToken = "refresh"
)
