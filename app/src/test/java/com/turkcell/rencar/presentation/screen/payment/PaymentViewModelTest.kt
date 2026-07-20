package com.turkcell.rencar.presentation.screen.payment

import android.net.Uri
import com.turkcell.rencar.domain.cards.Card
import com.turkcell.rencar.domain.cards.CardBrand
import com.turkcell.rencar.domain.cards.CardRepository
import com.turkcell.rencar.domain.cards.CardResult
import com.turkcell.rencar.domain.iyzico.IyzicoCheckoutSession
import com.turkcell.rencar.domain.iyzico.IyzicoPaymentResult
import com.turkcell.rencar.domain.iyzico.IyzicoRepository
import com.turkcell.rencar.domain.iyzico.IyzicoResult
import com.turkcell.rencar.domain.rental.ActiveRental
import com.turkcell.rencar.domain.rental.PaymentMethod
import com.turkcell.rencar.domain.rental.PaymentReceipt
import com.turkcell.rencar.domain.rental.PaymentStatus
import com.turkcell.rencar.domain.rental.Rental
import com.turkcell.rencar.domain.rental.RentalHistoryItem
import com.turkcell.rencar.domain.rental.RentalPhotoSide
import com.turkcell.rencar.domain.rental.RentalPhotosState
import com.turkcell.rencar.domain.rental.RentalPlan
import com.turkcell.rencar.domain.rental.RentalRepository
import com.turkcell.rencar.domain.rental.RentalResult
import com.turkcell.rencar.domain.rental.RentalStats
import com.turkcell.rencar.domain.rental.RentalSummary
import com.turkcell.rencar.domain.wallet.Wallet
import com.turkcell.rencar.domain.wallet.WalletRepository
import com.turkcell.rencar.domain.wallet.WalletResult
import com.turkcell.rencar.test.MainDispatcherRule
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
class PaymentViewModelTest {

    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `iyzico checkout success verifies rental payment and navigates home`() = runTest {
        val rentalRepository = FakeRentalRepository()
        val iyzicoRepository = FakeIyzicoRepository(
            result = IyzicoResult.Success(
                IyzicoPaymentResult(
                    status = "success",
                    paymentId = "36677190",
                    paymentStatus = "SUCCESS",
                    paidPrice = 150.5,
                    currency = "TRY"
                )
            )
        )
        val viewModel = createViewModel(rentalRepository, iyzicoRepository)

        viewModel.onIntent(PaymentIntent.ScreenStarted(RENTAL_ID))
        advanceUntilIdle()
        viewModel.onIntent(PaymentIntent.MethodSelected(PaymentMethod.IYZICO))
        viewModel.onIntent(PaymentIntent.PayClicked)
        advanceUntilIdle()

        assertEquals(150.5, iyzicoRepository.initializedPrice ?: 0.0, 0.0)
        assertEquals("rental-$RENTAL_ID", iyzicoRepository.initializedBasketId)
        assertEquals(listOf(1), iyzicoRepository.initializedInstallments)
        assertTrue(viewModel.state.value.showIyzicoWebView)

        viewModel.onIntent(PaymentIntent.IyzicoPaymentCheckClicked)
        advanceUntilIdle()

        assertEquals(PaymentMethod.IYZICO, rentalRepository.paidMethod)
        assertEquals("36677190", rentalRepository.paidIyzicoPaymentId)
        assertEquals(PaymentEffect.NavigateHome, viewModel.effect.first())
    }

    @Test
    fun `iyzico pending result keeps rental unpaid`() = runTest {
        val rentalRepository = FakeRentalRepository()
        val iyzicoRepository = FakeIyzicoRepository(
            result = IyzicoResult.Success(
                IyzicoPaymentResult(
                    status = "failure",
                    paymentId = null,
                    paymentStatus = "FAILURE",
                    paidPrice = null,
                    currency = null
                )
            )
        )
        val viewModel = createViewModel(rentalRepository, iyzicoRepository)

        viewModel.onIntent(PaymentIntent.ScreenStarted(RENTAL_ID))
        advanceUntilIdle()
        viewModel.onIntent(PaymentIntent.MethodSelected(PaymentMethod.IYZICO))
        viewModel.onIntent(PaymentIntent.PayClicked)
        advanceUntilIdle()
        viewModel.onIntent(PaymentIntent.IyzicoPaymentCheckClicked)
        advanceUntilIdle()

        assertFalse(rentalRepository.payCalled)
        assertFalse(viewModel.state.value.isIyzicoResultChecking)
        assertTrue(viewModel.state.value.errorMessage?.contains("tamamlanmadı") == true)
    }

    private fun createViewModel(
        rentalRepository: FakeRentalRepository = FakeRentalRepository(),
        iyzicoRepository: FakeIyzicoRepository = FakeIyzicoRepository()
    ) = PaymentViewModel(
        rentalRepository = rentalRepository,
        walletRepository = FakeWalletRepository(),
        cardRepository = FakeCardRepository(),
        iyzicoRepository = iyzicoRepository
    )

    private class FakeIyzicoRepository(
        private val session: IyzicoResult<IyzicoCheckoutSession> = IyzicoResult.Success(
            IyzicoCheckoutSession(
                token = "checkout-token",
                paymentPageUrl = "https://sandbox.iyzipay.com/pay/token",
                tokenExpireTime = 1800
            )
        ),
        private val result: IyzicoResult<IyzicoPaymentResult> = IyzicoResult.Success(
            IyzicoPaymentResult(
                status = "success",
                paymentId = "36677190",
                paymentStatus = "SUCCESS",
                paidPrice = 150.5,
                currency = "TRY"
            )
        )
    ) : IyzicoRepository {
        var initializedPrice: Double? = null
        var initializedBasketId: String? = null
        var initializedInstallments: List<Int>? = null

        override suspend fun initializeCheckoutForm(
            price: Double,
            description: String,
            basketId: String,
            enabledInstallments: List<Int>
        ): IyzicoResult<IyzicoCheckoutSession> {
            initializedPrice = price
            initializedBasketId = basketId
            initializedInstallments = enabledInstallments
            return session
        }

        override suspend fun getCheckoutFormResult(token: String): IyzicoResult<IyzicoPaymentResult> =
            result
    }

    private class FakeRentalRepository : RentalRepository {
        var payCalled = false
        var paidMethod: PaymentMethod? = null
        var paidIyzicoPaymentId: String? = null

        override suspend fun createRental(vehicleId: String, plan: RentalPlan, endDate: String?): RentalResult<Rental> =
            error("Not used")

        override suspend fun getMyRentals(): RentalResult<List<RentalSummary>> =
            error("Not used")

        override suspend fun getRentalHistory(): RentalResult<List<RentalHistoryItem>> =
            error("Not used")

        override suspend fun getRentalStats(): RentalResult<RentalStats> =
            error("Not used")

        override suspend fun uploadRentalPhoto(
            rentalId: String,
            side: RentalPhotoSide,
            imageUri: Uri
        ): RentalResult<RentalPhotosState> = error("Not used")

        override suspend fun getRentalPhotos(rentalId: String): RentalResult<RentalPhotosState> =
            error("Not used")

        override suspend fun startRental(rentalId: String): RentalResult<Rental> =
            error("Not used")

        override suspend fun cancelRental(rentalId: String): RentalResult<Unit> =
            error("Not used")

        override suspend fun getActiveRental(): RentalResult<ActiveRental> =
            error("Not used")

        override suspend fun finishRental(rentalId: String): RentalResult<Rental> =
            error("Not used")

        override suspend fun getRentalDetail(rentalId: String): RentalResult<Rental> =
            RentalResult.Success(rental)

        override suspend fun payRental(
            rentalId: String,
            method: PaymentMethod,
            cardId: String?,
            discountCode: String?,
            iyzicoPaymentId: String?
        ): RentalResult<PaymentReceipt> {
            payCalled = true
            paidMethod = method
            paidIyzicoPaymentId = iyzicoPaymentId
            return RentalResult.Success(
                PaymentReceipt(
                    rentalId = rentalId,
                    paymentStatus = PaymentStatus.PAID,
                    method = method,
                    totalPrice = 150.5,
                    discountAmount = 0.0,
                    paidAmount = 150.5,
                    walletBalance = null,
                    cardBrand = null,
                    cardLast4 = null
                )
            )
        }
    }

    private class FakeWalletRepository : WalletRepository {
        override suspend fun getWallet(): WalletResult<Wallet> =
            WalletResult.Success(Wallet(id = "wallet-1", balance = 0.0, transactions = emptyList()))

        override suspend fun topUp(amount: Double): WalletResult<Wallet> =
            error("Not used")
    }

    private class FakeCardRepository : CardRepository {
        override suspend fun getCards(): CardResult<List<Card>> =
            CardResult.Success(emptyList())

        override suspend fun addCard(
            brand: CardBrand,
            last4: String,
            expMonth: Int,
            expYear: Int
        ): CardResult<Card> = error("Not used")

        override suspend fun setDefaultCard(cardId: String): CardResult<Card> =
            error("Not used")

        override suspend fun deleteCard(cardId: String): CardResult<Unit> =
            error("Not used")
    }

    private companion object {
        const val RENTAL_ID = "rental-1"

        val rental = Rental(
            id = RENTAL_ID,
            userId = "user-1",
            vehicleId = "vehicle-1",
            vehicleBrand = "Renault",
            vehicleModel = "Clio",
            vehiclePlate = "34 RNC 022",
            plan = RentalPlan.PER_MINUTE,
            startDate = "2026-07-19T10:00:00.000Z",
            endDate = null,
            totalPrice = 150.5,
            startFee = 15.0,
            serviceFee = 5.0,
            distanceKm = 12.0,
            durationMinutes = 40.0,
            status = "COMPLETED",
            paymentStatus = PaymentStatus.UNPAID,
            createdAt = "2026-07-19T10:00:00.000Z"
        )
    }
}
