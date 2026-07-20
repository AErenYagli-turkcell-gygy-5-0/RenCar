package com.turkcell.rencar.presentation.screen.referral

import com.turkcell.rencar.domain.auth.AuthError
import com.turkcell.rencar.domain.auth.AuthRepository
import com.turkcell.rencar.domain.auth.AuthResult
import com.turkcell.rencar.domain.auth.LoginChallenge
import com.turkcell.rencar.domain.auth.RegisterRequest
import com.turkcell.rencar.domain.auth.RegisteredUser
import com.turkcell.rencar.domain.auth.VerifiedSession
import com.turkcell.rencar.domain.wallet.Wallet
import com.turkcell.rencar.domain.wallet.WalletError
import com.turkcell.rencar.domain.wallet.WalletRepository
import com.turkcell.rencar.domain.wallet.WalletResult
import com.turkcell.rencar.domain.wallet.WalletTransaction
import com.turkcell.rencar.domain.wallet.WalletTransactionType
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
class ReferralViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `screen started loads referral code and filters bonus transactions`() = runTest {
        val viewModel = ReferralViewModel(
            authRepository = FakeAuthRepository(),
            walletRepository = FakeWalletRepository()
        )

        viewModel.onIntent(ReferralIntent.ScreenStarted)
        advanceUntilIdle()

        assertEquals("REN-K7M2XQ", viewModel.state.value.referralCode)
        assertEquals(1, viewModel.state.value.earnedTransactions.size)
        assertEquals(
            WalletTransactionType.REFERRAL_BONUS,
            viewModel.state.value.earnedTransactions.first().type
        )
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `share clicked sends referral code effect`() = runTest {
        val viewModel = ReferralViewModel(
            authRepository = FakeAuthRepository(),
            walletRepository = FakeWalletRepository()
        )

        viewModel.onIntent(ReferralIntent.ScreenStarted)
        advanceUntilIdle()
        viewModel.onIntent(ReferralIntent.ShareClicked)

        assertEquals(ReferralEffect.ShareReferralCode("REN-K7M2XQ"), viewModel.effect.first())
    }

    private class FakeAuthRepository(
        var userResult: AuthResult<RegisteredUser> = AuthResult.Success(
            RegisteredUser(
                id = "user-1",
                email = "deniz@example.com",
                phone = "+905320000000",
                fullName = "Deniz Yilmaz",
                role = "CUSTOMER",
                referralCode = "REN-K7M2XQ",
                createdAt = "2026-07-07T10:00:00.000Z",
                updatedAt = "2026-07-07T10:00:00.000Z"
            )
        )
    ) : AuthRepository {
        override suspend fun register(request: RegisterRequest): AuthResult<RegisteredUser> =
            AuthResult.Failure(AuthError.Unexpected)

        override suspend fun requestLogin(phone: String): AuthResult<LoginChallenge> =
            AuthResult.Failure(AuthError.Unexpected)

        override suspend fun verifyOtp(phone: String, code: String): AuthResult<VerifiedSession> =
            AuthResult.Failure(AuthError.Unexpected)

        override suspend fun refreshSession(): AuthResult<VerifiedSession> =
            AuthResult.Failure(AuthError.Unexpected)

        override suspend fun getCurrentUser(): AuthResult<RegisteredUser> = userResult

        override suspend fun logout(): AuthResult<Unit> = AuthResult.Success(Unit)
    }

    private class FakeWalletRepository(
        var walletResult: WalletResult<Wallet> = WalletResult.Success(
            Wallet(
                id = "wallet-1",
                balance = 150.0,
                transactions = listOf(
                    WalletTransaction(
                        id = "tx-1",
                        type = WalletTransactionType.REFERRAL_BONUS,
                        amount = 50.0,
                        rentalId = null,
                        description = "Davet bonusu",
                        createdAt = "2026-07-10T10:00:00.000Z"
                    ),
                    WalletTransaction(
                        id = "tx-2",
                        type = WalletTransactionType.TOPUP,
                        amount = 100.0,
                        rentalId = null,
                        description = "Bakiye yükleme",
                        createdAt = "2026-07-05T10:00:00.000Z"
                    )
                )
            )
        )
    ) : WalletRepository {
        override suspend fun getWallet(): WalletResult<Wallet> = walletResult

        override suspend fun topUp(amount: Double): WalletResult<Wallet> =
            WalletResult.Failure(WalletError.Unexpected)
    }
}
