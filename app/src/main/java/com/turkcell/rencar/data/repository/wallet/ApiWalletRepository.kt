package com.turkcell.rencar.data.repository.wallet

import com.turkcell.rencar.data.remote.wallet.WalletApiService
import com.turkcell.rencar.data.remote.wallet.dto.TopupRequestDto
import com.turkcell.rencar.data.remote.wallet.dto.WalletResponseDto
import com.turkcell.rencar.data.remote.wallet.dto.WalletTransactionResponseDto
import com.turkcell.rencar.domain.wallet.Wallet
import com.turkcell.rencar.domain.wallet.WalletError
import com.turkcell.rencar.domain.wallet.WalletRepository
import com.turkcell.rencar.domain.wallet.WalletResult
import com.turkcell.rencar.domain.wallet.WalletTransaction
import com.turkcell.rencar.domain.wallet.WalletTransactionType
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class ApiWalletRepository @Inject constructor(
    private val apiService: WalletApiService
) : WalletRepository {

    override suspend fun getWallet(): WalletResult<Wallet> = runRequest {
        apiService.getWallet().toDomain()
    }

    override suspend fun topUp(amount: Double): WalletResult<Wallet> = runRequest {
        apiService.topup(TopupRequestDto(amount = amount)).toDomain()
    }

    private suspend fun <T> runRequest(request: suspend () -> T): WalletResult<T> = try {
        WalletResult.Success(request())
    } catch (error: CancellationException) {
        throw error
    } catch (error: HttpException) {
        WalletResult.Failure(error.code().toWalletError())
    } catch (error: IOException) {
        WalletResult.Failure(WalletError.Network)
    } catch (error: Exception) {
        WalletResult.Failure(WalletError.Unexpected)
    }

    private fun Int.toWalletError(): WalletError = when (this) {
        HTTP_BAD_REQUEST -> WalletError.InvalidRequest
        HTTP_UNAUTHORIZED -> WalletError.Unauthorized
        HTTP_FORBIDDEN -> WalletError.Forbidden
        HTTP_NOT_FOUND -> WalletError.NotFound
        HTTP_CONFLICT -> WalletError.Conflict
        else -> WalletError.Unexpected
    }

    private companion object {
        const val HTTP_BAD_REQUEST = 400
        const val HTTP_UNAUTHORIZED = 401
        const val HTTP_FORBIDDEN = 403
        const val HTTP_NOT_FOUND = 404
        const val HTTP_CONFLICT = 409
    }
}

internal fun WalletResponseDto.toDomain() = Wallet(
    id = id,
    balance = balance,
    transactions = transactions.map { it.toDomain() }
)

internal fun WalletTransactionResponseDto.toDomain() = WalletTransaction(
    id = id,
    type = type.toWalletTransactionTypeOrDefault(),
    amount = amount,
    rentalId = rentalId,
    description = description,
    createdAt = createdAt
)

private fun String.toWalletTransactionTypeOrDefault(): WalletTransactionType =
    runCatching { WalletTransactionType.valueOf(this) }.getOrDefault(WalletTransactionType.TOPUP)
