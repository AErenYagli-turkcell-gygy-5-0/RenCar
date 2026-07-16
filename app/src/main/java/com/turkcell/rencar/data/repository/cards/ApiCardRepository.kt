package com.turkcell.rencar.data.repository.cards

import com.turkcell.rencar.data.remote.cards.CardsApiService
import com.turkcell.rencar.data.remote.cards.dto.CardResponseDto
import com.turkcell.rencar.data.remote.cards.dto.CreateCardRequestDto
import com.turkcell.rencar.domain.cards.Card
import com.turkcell.rencar.domain.cards.CardBrand
import com.turkcell.rencar.domain.cards.CardError
import com.turkcell.rencar.domain.cards.CardRepository
import com.turkcell.rencar.domain.cards.CardResult
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class ApiCardRepository @Inject constructor(
    private val apiService: CardsApiService
) : CardRepository {

    override suspend fun getCards(): CardResult<List<Card>> = runRequest {
        apiService.list().map { it.toDomain() }
    }

    override suspend fun addCard(
        brand: CardBrand,
        last4: String,
        expMonth: Int,
        expYear: Int
    ): CardResult<Card> = runRequest {
        val request = CreateCardRequestDto(
            brand = brand.name,
            last4 = last4,
            expMonth = expMonth,
            expYear = expYear
        )
        apiService.create(request).toDomain()
    }

    override suspend fun setDefaultCard(cardId: String): CardResult<Card> = runRequest {
        apiService.setDefault(id = cardId).toDomain()
    }

    override suspend fun deleteCard(cardId: String): CardResult<Unit> = runRequest {
        apiService.delete(id = cardId)
    }

    private suspend fun <T> runRequest(request: suspend () -> T): CardResult<T> = try {
        CardResult.Success(request())
    } catch (error: CancellationException) {
        throw error
    } catch (error: HttpException) {
        CardResult.Failure(error.code().toCardError())
    } catch (error: IOException) {
        CardResult.Failure(CardError.Network)
    } catch (error: Exception) {
        CardResult.Failure(CardError.Unexpected)
    }

    private fun Int.toCardError(): CardError = when (this) {
        HTTP_BAD_REQUEST -> CardError.InvalidRequest
        HTTP_UNAUTHORIZED -> CardError.Unauthorized
        HTTP_FORBIDDEN -> CardError.Forbidden
        HTTP_NOT_FOUND -> CardError.NotFound
        HTTP_CONFLICT -> CardError.Conflict
        else -> CardError.Unexpected
    }

    private companion object {
        const val HTTP_BAD_REQUEST = 400
        const val HTTP_UNAUTHORIZED = 401
        const val HTTP_FORBIDDEN = 403
        const val HTTP_NOT_FOUND = 404
        const val HTTP_CONFLICT = 409
    }
}

internal fun CardResponseDto.toDomain() = Card(
    id = id,
    brand = brand.toCardBrandOrDefault(),
    last4 = last4,
    expMonth = expMonth,
    expYear = expYear,
    isDefault = isDefault,
    createdAt = createdAt
)

private fun String.toCardBrandOrDefault(): CardBrand =
    runCatching { CardBrand.valueOf(this) }.getOrDefault(CardBrand.VISA)
