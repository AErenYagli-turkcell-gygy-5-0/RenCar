package com.turkcell.rencar.domain.iyzico

interface IyzicoRepository {
    suspend fun initializeCheckoutForm(
        price: Double,
        description: String,
        basketId: String,
        enabledInstallments: List<Int>
    ): IyzicoResult<IyzicoCheckoutSession>

    suspend fun getCheckoutFormResult(token: String): IyzicoResult<IyzicoPaymentResult>
}
