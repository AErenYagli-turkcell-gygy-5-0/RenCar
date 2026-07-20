package com.turkcell.rencar.data.remote.iyzico

import com.turkcell.rencar.data.remote.iyzico.dto.CheckoutFormInitializeRequestDto
import com.turkcell.rencar.data.remote.iyzico.dto.CheckoutFormInitializeResponseDto
import com.turkcell.rencar.data.remote.iyzico.dto.IyzicoPaymentResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface IyzicoApiService {
    @POST("iyzico/checkout-form/initialize")
    suspend fun initializeCheckoutForm(
        @Body request: CheckoutFormInitializeRequestDto
    ): CheckoutFormInitializeResponseDto

    @GET("iyzico/checkout-form/result/{token}")
    suspend fun getCheckoutFormResult(@Path("token") token: String): IyzicoPaymentResponseDto
}
