package com.turkcell.rencar.presentation.screen.auth.login

import com.turkcell.rencar.domain.auth.AuthError
import com.turkcell.rencar.domain.auth.AuthRepository
import com.turkcell.rencar.domain.auth.AuthResult
import com.turkcell.rencar.domain.auth.LoginChallenge
import com.turkcell.rencar.domain.auth.RegisterRequest
import com.turkcell.rencar.domain.auth.RegisteredUser
import com.turkcell.rencar.domain.auth.VerifiedSession
import com.turkcell.rencar.test.MainDispatcherRule
import androidx.compose.ui.text.AnnotatedString
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
class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeAuthRepository()

    @Test
    fun `phone input is formatted and successful request navigates to otp`() = runTest {
        repository.loginResult = AuthResult.Success(
            LoginChallenge(
                message = "Kod gönderildi.",
                phone = "+905320000000",
                expiresAt = "2026-07-03T12:05:00.000Z"
            )
        )
        val viewModel = LoginViewModel(repository)

        viewModel.onIntent(LoginIntent.PhoneNumberChanged("5320000000"))
        viewModel.onIntent(LoginIntent.SendCodeClicked)
        advanceUntilIdle()

        assertEquals("5320000000", viewModel.state.value.phoneNumber)
        assertEquals(listOf("+905320000000"), repository.loginRequests)
        assertFalse(viewModel.state.value.isLoading)
        assertEquals(
            LoginEffect.NavigateToOtp("+905320000000"),
            viewModel.effect.first()
        )
    }

    @Test
    fun `invalid phone does not call repository`() = runTest {
        val viewModel = LoginViewModel(repository)

        viewModel.onIntent(LoginIntent.PhoneNumberChanged("532"))
        viewModel.onIntent(LoginIntent.SendCodeClicked)

        assertTrue(repository.loginRequests.isEmpty())
        assertEquals(
            "Telefon numarası 10 haneli olmalıdır.",
            viewModel.state.value.errorMessage
        )
    }

    @Test
    fun `phone visual transformation keeps cursor after inserted spaces`() {
        val transformed = TurkishPhoneNumberVisualTransformation.filter(
            AnnotatedString("5496141234")
        )

        assertEquals("549 614 12 34", transformed.text.text)
        assertEquals(5, transformed.offsetMapping.originalToTransformed(4))
        assertEquals(4, transformed.offsetMapping.transformedToOriginal(5))
        assertEquals(13, transformed.offsetMapping.originalToTransformed(10))
        assertEquals(10, transformed.offsetMapping.transformedToOriginal(13))
    }

    @Test
    fun `duplicate send is ignored while first request is loading`() = runTest {
        repository.loginGate = CompletableDeferred()
        val viewModel = LoginViewModel(repository)
        viewModel.onIntent(LoginIntent.PhoneNumberChanged("5320000000"))

        viewModel.onIntent(LoginIntent.SendCodeClicked)
        viewModel.onIntent(LoginIntent.SendCodeClicked)
        advanceUntilIdle()

        assertEquals(1, repository.loginRequests.size)
        assertTrue(viewModel.state.value.isLoading)

        repository.loginGate?.complete(AuthResult.Failure(AuthError.Network))
        advanceUntilIdle()
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `user not found is exposed as state error`() = runTest {
        repository.loginResult = AuthResult.Failure(AuthError.UserNotFound)
        val viewModel = LoginViewModel(repository)
        viewModel.onIntent(LoginIntent.PhoneNumberChanged("5320000000"))

        viewModel.onIntent(LoginIntent.SendCodeClicked)
        advanceUntilIdle()

        assertEquals(
            "Bu telefon numarasına kayıtlı kullanıcı bulunamadı.",
            viewModel.state.value.errorMessage
        )
    }

    private class FakeAuthRepository : AuthRepository {
        var loginResult: AuthResult<LoginChallenge> =
            AuthResult.Failure(AuthError.Unexpected)
        var loginGate: CompletableDeferred<AuthResult<LoginChallenge>>? = null
        val loginRequests = mutableListOf<String>()

        override suspend fun register(
            request: RegisterRequest
        ): AuthResult<RegisteredUser> = AuthResult.Failure(AuthError.Unexpected)

        override suspend fun requestLogin(phone: String): AuthResult<LoginChallenge> {
            loginRequests += phone
            return loginGate?.await() ?: loginResult
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
