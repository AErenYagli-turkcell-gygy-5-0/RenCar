package com.turkcell.rencar.data.repository.iyzico

import com.turkcell.rencar.data.remote.iyzico.IyzicoApiService
import com.turkcell.rencar.data.remote.iyzico.dto.CheckoutFormInitializeRequestDto
import com.turkcell.rencar.data.remote.iyzico.dto.CheckoutFormInitializeResponseDto
import com.turkcell.rencar.data.remote.iyzico.dto.IyzicoPaymentResponseDto
import com.turkcell.rencar.domain.iyzico.IyzicoCheckoutSession
import com.turkcell.rencar.domain.iyzico.IyzicoError
import com.turkcell.rencar.domain.iyzico.IyzicoPaymentResult
import com.turkcell.rencar.domain.iyzico.IyzicoRepository
import com.turkcell.rencar.domain.iyzico.IyzicoResult
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class ApiIyzicoRepository @Inject constructor(
    private val apiService: IyzicoApiService
) : IyzicoRepository {

    override suspend fun initializeCheckoutForm(
        price: Double,
        description: String,
        basketId: String,
        enabledInstallments: List<Int>
    ): IyzicoResult<IyzicoCheckoutSession> = runRequest {
        apiService.initializeCheckoutForm(
            CheckoutFormInitializeRequestDto(
                price = price,
                description = description,
                basketId = basketId,
                enabledInstallments = enabledInstallments
            )
        ).toDomain()
    }

    override suspend fun getCheckoutFormResult(token: String): IyzicoResult<IyzicoPaymentResult> =
        runRequest { apiService.getCheckoutFormResult(token).toDomain() }

    private suspend fun <T> runRequest(request: suspend () -> T): IyzicoResult<T> = try {
        IyzicoResult.Success(request())
    } catch (error: CancellationException) {
        throw error
    } catch (error: HttpException) {
        IyzicoResult.Failure(error.code().toIyzicoError())
    } catch (error: IOException) {
        IyzicoResult.Failure(IyzicoError.Network)
    } catch (error: Exception) {
        IyzicoResult.Failure(IyzicoError.Unexpected)
    }

    private fun Int.toIyzicoError(): IyzicoError = when (this) {
        HTTP_BAD_REQUEST -> IyzicoError.InvalidRequest
        HTTP_UNAUTHORIZED -> IyzicoError.Unauthorized
        HTTP_FORBIDDEN -> IyzicoError.Forbidden
        HTTP_SERVICE_UNAVAILABLE -> IyzicoError.ServiceUnavailable
        else -> IyzicoError.Unexpected
    }

    private companion object {
        const val HTTP_BAD_REQUEST = 400
        const val HTTP_UNAUTHORIZED = 401
        const val HTTP_FORBIDDEN = 403
        const val HTTP_SERVICE_UNAVAILABLE = 503
    }
}

internal fun CheckoutFormInitializeResponseDto.toDomain() = IyzicoCheckoutSession(
    token = token,
    paymentPageUrl = paymentPageUrl.orEmpty(),
    tokenExpireTime = tokenExpireTime
)

internal fun IyzicoPaymentResponseDto.toDomain() = IyzicoPaymentResult(
    status = status,
    paymentId = paymentId,
    paymentStatus = paymentStatus,
    paidPrice = paidPrice,
    currency = currency
)
