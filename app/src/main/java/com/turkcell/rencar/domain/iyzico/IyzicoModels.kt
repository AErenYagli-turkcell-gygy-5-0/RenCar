package com.turkcell.rencar.domain.iyzico

data class IyzicoCheckoutSession(
    val token: String,
    val paymentPageUrl: String,
    val tokenExpireTime: Long? = null
)

data class IyzicoPaymentResult(
    val status: String,
    val paymentId: String?,
    val paymentStatus: String?,
    val paidPrice: Double?,
    val currency: String?
) {
    val isSuccessful: Boolean
        get() = paymentStatus == SUCCESS_STATUS && !paymentId.isNullOrBlank()

    private companion object {
        const val SUCCESS_STATUS = "SUCCESS"
    }
}

sealed interface IyzicoResult<out T> {
    data class Success<T>(val data: T) : IyzicoResult<T>
    data class Failure(val error: IyzicoError) : IyzicoResult<Nothing>
}

enum class IyzicoError {
    InvalidRequest,
    Unauthorized,
    Forbidden,
    ServiceUnavailable,
    Network,
    Unexpected
}
