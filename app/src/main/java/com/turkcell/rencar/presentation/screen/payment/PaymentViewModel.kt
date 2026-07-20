package com.turkcell.rencar.presentation.screen.payment

import androidx.lifecycle.viewModelScope
import com.turkcell.rencar.domain.cards.CardBrand
import com.turkcell.rencar.domain.cards.CardError
import com.turkcell.rencar.domain.cards.CardRepository
import com.turkcell.rencar.domain.cards.CardResult
import com.turkcell.rencar.domain.iyzico.IyzicoError
import com.turkcell.rencar.domain.iyzico.IyzicoRepository
import com.turkcell.rencar.domain.iyzico.IyzicoResult
import com.turkcell.rencar.domain.rental.PaymentMethod
import com.turkcell.rencar.domain.rental.PaymentStatus
import com.turkcell.rencar.domain.rental.RentalError
import com.turkcell.rencar.domain.rental.RentalRepository
import com.turkcell.rencar.domain.rental.RentalResult
import com.turkcell.rencar.domain.wallet.WalletRepository
import com.turkcell.rencar.domain.wallet.WalletResult
import com.turkcell.rencar.presentation.core.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val rentalRepository: RentalRepository,
    private val walletRepository: WalletRepository,
    private val cardRepository: CardRepository,
    private val iyzicoRepository: IyzicoRepository
) : MviViewModel<PaymentState, PaymentIntent, PaymentEffect>(PaymentState()) {

    override fun onIntent(intent: PaymentIntent) {
        when (intent) {
            is PaymentIntent.ScreenStarted -> start(intent.rentalId)
            is PaymentIntent.MethodSelected -> setState {
                copy(
                    selectedMethod = intent.method,
                    errorMessage = null,
                    showIyzicoWebView = false,
                    iyzicoCheckoutToken = null,
                    iyzicoPaymentPageUrl = null,
                    isIyzicoResultChecking = false
                )
            }
            PaymentIntent.ChangeCardClicked -> setState { copy(showCardPicker = true) }
            is PaymentIntent.CardSelected -> setState {
                copy(selectedCardId = intent.cardId, showCardPicker = false)
            }

            PaymentIntent.CardPickerDismissed -> setState { copy(showCardPicker = false) }
            PaymentIntent.AddCardClicked -> setState {
                copy(
                    showCardPicker = false,
                    showAddCardDialog = true,
                    addCardBrand = CardBrand.VISA,
                    addCardLast4Input = "",
                    addCardExpMonthInput = "",
                    addCardExpYearInput = "",
                    addCardErrorMessage = null
                )
            }

            is PaymentIntent.AddCardBrandChanged -> setState { copy(addCardBrand = intent.brand) }
            is PaymentIntent.AddCardLast4Changed -> setState { copy(addCardLast4Input = intent.value) }
            is PaymentIntent.AddCardExpMonthChanged -> setState { copy(addCardExpMonthInput = intent.value) }
            is PaymentIntent.AddCardExpYearChanged -> setState { copy(addCardExpYearInput = intent.value) }
            PaymentIntent.AddCardConfirmClicked -> handleAddCardConfirmClicked()
            PaymentIntent.AddCardDismissed -> setState { copy(showAddCardDialog = false) }
            PaymentIntent.PayClicked -> handlePayClicked()
            PaymentIntent.IyzicoWebViewDismissed -> setState { copy(showIyzicoWebView = false) }
            PaymentIntent.IyzicoPaymentCheckClicked -> handleIyzicoPaymentCheckClicked()
            PaymentIntent.TopUpConfirmed -> {
                setState { copy(showInsufficientBalanceDialog = false) }
                sendEffect { PaymentEffect.NavigateToWallet }
            }

            PaymentIntent.TopUpDismissed -> setState { copy(showInsufficientBalanceDialog = false) }
        }
    }

    private fun start(rentalId: String) {
        if (state.value.rentalId == rentalId && state.value.vehicleName.isNotBlank()) return

        setState { copy(rentalId = rentalId, isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val rentalResult = rentalRepository.getRentalDetail(rentalId)) {
                is RentalResult.Success -> {
                    val rental = rentalResult.data
                    val usageFee = ((rental.totalPrice ?: 0.0) - rental.startFee - (rental.serviceFee ?: 0.0))
                        .coerceAtLeast(0.0)
                    setState {
                        copy(
                            vehicleName = "${rental.vehicleBrand} ${rental.vehicleModel}".trim(),
                            vehiclePlate = rental.vehiclePlate,
                            durationMinutes = rental.durationMinutes,
                            distanceKm = rental.distanceKm,
                            usageFee = usageFee,
                            startFee = rental.startFee,
                            serviceFee = rental.serviceFee ?: 0.0,
                            discountAmount = rental.discountAmount,
                            totalPrice = rental.totalPrice ?: 0.0,
                            isPaid = rental.paymentStatus == PaymentStatus.PAID
                        )
                    }
                    loadWalletAndCards()
                }

                is RentalResult.Failure -> setState {
                    copy(isLoading = false, errorMessage = rentalResult.error.toMessage())
                }
            }
        }
    }

    private suspend fun loadWalletAndCards() {
        when (val walletResult = walletRepository.getWallet()) {
            is WalletResult.Success -> setState { copy(walletBalance = walletResult.data.balance) }
            is WalletResult.Failure -> Unit
        }

        when (val cardResult = cardRepository.getCards()) {
            is CardResult.Success -> {
                val defaultCard = cardResult.data.firstOrNull { it.isDefault } ?: cardResult.data.firstOrNull()
                setState {
                    val hasSufficientBalance = walletBalance >= netAmount
                    copy(
                        cards = cardResult.data,
                        selectedCardId = defaultCard?.id,
                        selectedMethod = if (hasSufficientBalance || defaultCard == null) {
                            PaymentMethod.WALLET
                        } else {
                            PaymentMethod.CARD
                        }
                    )
                }
            }

            is CardResult.Failure -> Unit
        }

        setState { copy(isLoading = false) }
    }

    private fun handlePayClicked() {
        val currentState = state.value
        if (currentState.isPaying || currentState.isPaid) return

        if (currentState.selectedMethod == PaymentMethod.IYZICO) {
            handleIyzicoPayClicked(currentState)
            return
        }

        if (currentState.selectedMethod == PaymentMethod.WALLET &&
            currentState.walletBalance < currentState.netAmount
        ) {
            setState { copy(showInsufficientBalanceDialog = true) }
            return
        }

        if (currentState.selectedMethod == PaymentMethod.CARD && currentState.selectedCardId == null) {
            return
        }

        setState { copy(isPaying = true, errorMessage = null) }
        viewModelScope.launch {
            when (
                val result = rentalRepository.payRental(
                    rentalId = currentState.rentalId,
                    method = currentState.selectedMethod,
                    cardId = if (currentState.selectedMethod == PaymentMethod.CARD) {
                        currentState.selectedCardId
                    } else {
                        null
                    }
                )
            ) {
                is RentalResult.Success -> {
                    setState { copy(isPaying = false, isPaid = true) }
                    sendEffect { PaymentEffect.NavigateHome }
                }

                is RentalResult.Failure -> setState {
                    copy(isPaying = false, errorMessage = result.error.toMessage())
                }
            }
        }
    }

    private fun handleIyzicoPayClicked(currentState: PaymentState) {
        if (currentState.netAmount < IYZICO_MIN_PRICE) {
            setState { copy(errorMessage = INVALID_REQUEST_MESSAGE) }
            return
        }

        setState { copy(isPaying = true, errorMessage = null) }
        viewModelScope.launch {
            when (
                val result = iyzicoRepository.initializeCheckoutForm(
                    price = currentState.netAmount,
                    description = IYZICO_PAYMENT_DESCRIPTION,
                    basketId = "rental-${currentState.rentalId}",
                    enabledInstallments = listOf(DEFAULT_INSTALLMENT)
                )
            ) {
                is IyzicoResult.Success -> {
                    val session = result.data
                    if (session.paymentPageUrl.isBlank()) {
                        setState { copy(isPaying = false, errorMessage = UNEXPECTED_ERROR_MESSAGE) }
                    } else {
                        setState {
                            copy(
                                isPaying = false,
                                iyzicoCheckoutToken = session.token,
                                iyzicoPaymentPageUrl = session.paymentPageUrl,
                                showIyzicoWebView = true
                            )
                        }
                    }
                }

                is IyzicoResult.Failure -> setState {
                    copy(isPaying = false, errorMessage = result.error.toMessage())
                }
            }
        }
    }

    private fun handleIyzicoPaymentCheckClicked() {
        val currentState = state.value
        val token = currentState.iyzicoCheckoutToken ?: return
        if (currentState.isIyzicoResultChecking || currentState.isPaid) return

        setState { copy(isIyzicoResultChecking = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = iyzicoRepository.getCheckoutFormResult(token)) {
                is IyzicoResult.Success -> handleIyzicoPaymentResult(currentState, result.data.paymentId, result.data.isSuccessful)
                is IyzicoResult.Failure -> setState {
                    copy(isIyzicoResultChecking = false, errorMessage = result.error.toMessage())
                }
            }
        }
    }

    private suspend fun handleIyzicoPaymentResult(
        currentState: PaymentState,
        paymentId: String?,
        isSuccessful: Boolean
    ) {
        if (!isSuccessful || paymentId.isNullOrBlank()) {
            setState {
                copy(
                    isIyzicoResultChecking = false,
                    errorMessage = IYZICO_PENDING_MESSAGE
                )
            }
            return
        }

        when (
            val rentalResult = rentalRepository.payRental(
                rentalId = currentState.rentalId,
                method = PaymentMethod.IYZICO,
                iyzicoPaymentId = paymentId
            )
        ) {
            is RentalResult.Success -> {
                setState {
                    copy(
                        isIyzicoResultChecking = false,
                        showIyzicoWebView = false,
                        isPaid = true
                    )
                }
                sendEffect { PaymentEffect.NavigateHome }
            }

            is RentalResult.Failure -> setState {
                copy(
                    isIyzicoResultChecking = false,
                    errorMessage = rentalResult.error.toMessage()
                )
            }
        }
    }

    private fun handleAddCardConfirmClicked() {
        val currentState = state.value
        val last4 = currentState.addCardLast4Input
        val expMonth = currentState.addCardExpMonthInput.toIntOrNull()
        val expYear = currentState.addCardExpYearInput.toIntOrNull()

        if (last4.length != CARD_LAST4_LENGTH || last4.any { !it.isDigit() }) {
            setState { copy(addCardErrorMessage = CARD_LAST4_MESSAGE) }
            return
        }
        if (expMonth == null || expMonth !in CARD_MIN_MONTH..CARD_MAX_MONTH) {
            setState { copy(addCardErrorMessage = CARD_EXP_MONTH_MESSAGE) }
            return
        }
        if (expYear == null || expYear < CARD_MIN_YEAR) {
            setState { copy(addCardErrorMessage = CARD_EXP_YEAR_MESSAGE) }
            return
        }

        setState { copy(isAddCardSubmitting = true, addCardErrorMessage = null) }
        viewModelScope.launch {
            when (
                val result = cardRepository.addCard(
                    brand = currentState.addCardBrand,
                    last4 = last4,
                    expMonth = expMonth,
                    expYear = expYear
                )
            ) {
                is CardResult.Success -> setState {
                    copy(
                        isAddCardSubmitting = false,
                        showAddCardDialog = false,
                        cards = cards + result.data,
                        selectedCardId = result.data.id
                    )
                }

                is CardResult.Failure -> setState {
                    copy(isAddCardSubmitting = false, addCardErrorMessage = result.error.toMessage())
                }
            }
        }
    }

    private fun CardError.toMessage(): String = when (this) {
        CardError.InvalidRequest -> INVALID_REQUEST_MESSAGE
        CardError.Unauthorized -> UNAUTHORIZED_MESSAGE
        CardError.Forbidden -> FORBIDDEN_MESSAGE
        CardError.NotFound -> NOT_FOUND_MESSAGE
        CardError.Conflict -> CONFLICT_MESSAGE
        CardError.Network -> NETWORK_ERROR_MESSAGE
        CardError.Unexpected -> UNEXPECTED_ERROR_MESSAGE
    }

    private fun RentalError.toMessage(): String = when (this) {
        RentalError.InvalidRequest -> INVALID_REQUEST_MESSAGE
        RentalError.Unauthorized -> UNAUTHORIZED_MESSAGE
        RentalError.Forbidden -> FORBIDDEN_MESSAGE
        RentalError.NotFound -> NOT_FOUND_MESSAGE
        RentalError.Conflict -> CONFLICT_MESSAGE
        RentalError.Network -> NETWORK_ERROR_MESSAGE
        RentalError.Unexpected -> UNEXPECTED_ERROR_MESSAGE
    }

    private fun IyzicoError.toMessage(): String = when (this) {
        IyzicoError.InvalidRequest -> INVALID_REQUEST_MESSAGE
        IyzicoError.Unauthorized -> UNAUTHORIZED_MESSAGE
        IyzicoError.Forbidden -> FORBIDDEN_MESSAGE
        IyzicoError.ServiceUnavailable -> IYZICO_UNAVAILABLE_MESSAGE
        IyzicoError.Network -> NETWORK_ERROR_MESSAGE
        IyzicoError.Unexpected -> UNEXPECTED_ERROR_MESSAGE
    }

    private companion object {
        const val CARD_LAST4_LENGTH = 4
        const val CARD_MIN_MONTH = 1
        const val CARD_MAX_MONTH = 12
        const val CARD_MIN_YEAR = 2000
        const val IYZICO_PAYMENT_DESCRIPTION = "RenCar yolculuk ödemesi"
        const val DEFAULT_INSTALLMENT = 1
        const val IYZICO_MIN_PRICE = 1.0
        const val CARD_LAST4_MESSAGE = "Kartın son 4 hanesini girin."
        const val CARD_EXP_MONTH_MESSAGE = "Son kullanma ayı 1-12 arasında olmalı."
        const val CARD_EXP_YEAR_MESSAGE = "Son kullanma yılını girin."
        const val INVALID_REQUEST_MESSAGE = "Ödeme alınamadı. Lütfen tekrar deneyin."
        const val UNAUTHORIZED_MESSAGE = "Oturumunuz sona ermiş. Lütfen tekrar giriş yapın."
        const val FORBIDDEN_MESSAGE = "Bu kiralama size ait değil."
        const val NOT_FOUND_MESSAGE = "Kiralama, kart veya indirim kodu bulunamadı."
        const val CONFLICT_MESSAGE = "Bu işlem şu anda yapılamıyor."
        const val NETWORK_ERROR_MESSAGE = "İnternet bağlantınızı kontrol edip tekrar deneyin."
        const val UNEXPECTED_ERROR_MESSAGE = "Bir hata oluştu. Lütfen tekrar deneyin."
        const val IYZICO_UNAVAILABLE_MESSAGE = "Iyzico ödeme servisi şu anda hazır değil."
        const val IYZICO_PENDING_MESSAGE = "Ödeme henüz tamamlanmadı. Iyzico sayfasında işlemi tamamlayıp tekrar kontrol edin."
    }
}
