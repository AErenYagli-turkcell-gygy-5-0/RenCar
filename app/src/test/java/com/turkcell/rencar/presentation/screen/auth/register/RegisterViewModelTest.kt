package com.turkcell.rencar.presentation.screen.auth.register

import com.turkcell.rencar.domain.auth.AuthError
import com.turkcell.rencar.domain.auth.AuthRepository
import com.turkcell.rencar.domain.auth.AuthResult
import com.turkcell.rencar.domain.auth.LoginChallenge
import com.turkcell.rencar.domain.auth.RegisterRequest
import com.turkcell.rencar.domain.auth.RegisteredUser
import com.turkcell.rencar.domain.auth.VerifiedSession
import com.turkcell.rencar.test.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
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
class RegisterViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeAuthRepository()

    @Test
    fun `successful register starts otp request and navigates to otp`() = runTest {
        val viewModel = RegisterViewModel(repository)

        viewModel.onIntent(RegisterIntent.FullNameChanged("Ahmet Yılmaz"))
        viewModel.onIntent(RegisterIntent.EmailChanged("ahmet@example.com"))
        viewModel.onIntent(RegisterIntent.PasswordChanged("Sifre123!"))
        viewModel.onIntent(RegisterIntent.PhoneNumberChanged("5320000000"))
        viewModel.onIntent(RegisterIntent.CreateAccountClicked)
        advanceUntilIdle()

        assertEquals(
            listOf(
                RegisterRequest(
                    email = "ahmet@example.com",
                    password = "Sifre123!",
                    fullName = "Ahmet Yılmaz",
                    phone = "+905320000000"
                )
            ),
            repository.registerRequests
        )
        assertEquals(listOf("+905320000000"), repository.loginRequests)
        assertFalse(viewModel.state.value.isLoading)
        assertEquals(
            RegisterEffect.NavigateToOtp("+905320000000"),
            viewModel.effect.first()
        )
    }

    @Test
    fun `invalid fields do not call repository`() = runTest {
        val viewModel = RegisterViewModel(repository)

        viewModel.onIntent(RegisterIntent.FullNameChanged("Ahmet Yılmaz"))
        viewModel.onIntent(RegisterIntent.EmailChanged("ahmet@example.com"))
        viewModel.onIntent(RegisterIntent.PasswordChanged("123"))
        viewModel.onIntent(RegisterIntent.PhoneNumberChanged("5320000000"))
        viewModel.onIntent(RegisterIntent.CreateAccountClicked)

        assertTrue(repository.registerRequests.isEmpty())
        assertEquals(
            "Şifre en az 6 karakter olmalıdır.",
            viewModel.state.value.errorMessage
        )
    }

    @Test
    fun `duplicate create account is ignored while first request is loading`() = runTest {
        repository.registerGate = CompletableDeferred()
        val viewModel = RegisterViewModel(repository)

        viewModel.onIntent(RegisterIntent.FullNameChanged("Ahmet Yılmaz"))
        viewModel.onIntent(RegisterIntent.EmailChanged("ahmet@example.com"))
        viewModel.onIntent(RegisterIntent.PasswordChanged("Sifre123!"))
        viewModel.onIntent(RegisterIntent.PhoneNumberChanged("5320000000"))
        viewModel.onIntent(RegisterIntent.CreateAccountClicked)
        viewModel.onIntent(RegisterIntent.CreateAccountClicked)
        advanceUntilIdle()

        assertEquals(1, repository.registerRequests.size)
        assertTrue(viewModel.state.value.isLoading)

        repository.registerGate?.complete(AuthResult.Failure(AuthError.Network))
        advanceUntilIdle()
        assertFalse(viewModel.state.value.isLoading)
    }

    private class FakeAuthRepository : AuthRepository {
        var registerResult: AuthResult<RegisteredUser> = AuthResult.Success(
            RegisteredUser(
                id = "user-1",
                email = "ahmet@example.com",
                phone = "+905320000000",
                fullName = "Ahmet Yılmaz",
                role = "PENDING",
                createdAt = "2026-07-04T10:00:00.000Z",
                updatedAt = "2026-07-04T10:00:00.000Z"
            )
        )
        var loginResult: AuthResult<LoginChallenge> = AuthResult.Success(
            LoginChallenge(
                message = "Kod gönderildi.",
                phone = "+905320000000",
                expiresAt = "2026-07-04T10:05:00.000Z"
            )
        )
        var registerGate: CompletableDeferred<AuthResult<RegisteredUser>>? = null
        val registerRequests = mutableListOf<RegisterRequest>()
        val loginRequests = mutableListOf<String>()

        override suspend fun register(
            request: RegisterRequest
        ): AuthResult<RegisteredUser> {
            registerRequests += request
            return registerGate?.await() ?: registerResult
        }

        override suspend fun requestLogin(phone: String): AuthResult<LoginChallenge> {
            loginRequests += phone
            return loginResult
        }

        override suspend fun verifyOtp(
            phone: String,
            code: String
        ): AuthResult<VerifiedSession> = AuthResult.Failure(AuthError.Unexpected)

        override suspend fun refreshSession(): AuthResult<VerifiedSession> =
            AuthResult.Failure(AuthError.Unexpected)

        override suspend fun getCurrentUser(): AuthResult<RegisteredUser> =
            AuthResult.Failure(AuthError.Unexpected)

        override suspend fun logout(): AuthResult<Unit> =
            AuthResult.Failure(AuthError.Unexpected)
    }
}
