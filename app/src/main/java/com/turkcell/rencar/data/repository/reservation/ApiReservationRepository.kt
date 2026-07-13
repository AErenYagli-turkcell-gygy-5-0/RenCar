package com.turkcell.rencar.data.repository.reservation

import com.turkcell.rencar.data.remote.reservation.ReservationApiService
import com.turkcell.rencar.data.remote.reservation.dto.CreateReservationRequestDto
import com.turkcell.rencar.data.remote.reservation.dto.ReservationResponseDto
import com.turkcell.rencar.domain.reservation.Reservation
import com.turkcell.rencar.domain.reservation.ReservationError
import com.turkcell.rencar.domain.reservation.ReservationRepository
import com.turkcell.rencar.domain.reservation.ReservationResult
import com.turkcell.rencar.domain.reservation.ReservationStatus
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class ApiReservationRepository @Inject constructor(
    private val apiService: ReservationApiService
) : ReservationRepository {

    override suspend fun createReservation(vehicleId: String): ReservationResult<Reservation> = try {
        val request = CreateReservationRequestDto(vehicleId = vehicleId)
        ReservationResult.Success(apiService.create(request).toDomain())
    } catch (error: CancellationException) {
        throw error
    } catch (error: HttpException) {
        ReservationResult.Failure(error.code().toReservationError())
    } catch (error: IOException) {
        ReservationResult.Failure(ReservationError.Network)
    } catch (error: Exception) {
        ReservationResult.Failure(ReservationError.Unexpected)
    }

    override suspend fun getActiveReservation(): ReservationResult<Reservation> = try {
        ReservationResult.Success(apiService.getActive().toDomain())
    } catch (error: CancellationException) {
        throw error
    } catch (error: HttpException) {
        ReservationResult.Failure(error.code().toReservationError())
    } catch (error: IOException) {
        ReservationResult.Failure(ReservationError.Network)
    } catch (error: Exception) {
        ReservationResult.Failure(ReservationError.Unexpected)
    }

    override suspend fun cancelReservation(id: String): ReservationResult<Unit> = try {
        apiService.cancel(id = id)
        ReservationResult.Success(Unit)
    } catch (error: CancellationException) {
        throw error
    } catch (error: HttpException) {
        ReservationResult.Failure(error.code().toReservationError())
    } catch (error: IOException) {
        ReservationResult.Failure(ReservationError.Network)
    } catch (error: Exception) {
        ReservationResult.Failure(ReservationError.Unexpected)
    }

    private fun Int.toReservationError(): ReservationError = when (this) {
        HTTP_BAD_REQUEST -> ReservationError.InvalidRequest
        HTTP_UNAUTHORIZED -> ReservationError.Unauthorized
        HTTP_FORBIDDEN -> ReservationError.Forbidden
        HTTP_NOT_FOUND -> ReservationError.NotFound
        HTTP_CONFLICT -> ReservationError.Conflict
        else -> ReservationError.Unexpected
    }

    private companion object {
        const val HTTP_BAD_REQUEST = 400
        const val HTTP_UNAUTHORIZED = 401
        const val HTTP_FORBIDDEN = 403
        const val HTTP_NOT_FOUND = 404
        const val HTTP_CONFLICT = 409
    }
}

private fun ReservationResponseDto.toDomain() = Reservation(
    id = id,
    vehicleId = vehicleId,
    status = status.toReservationStatusOrDefault(),
    expiresAt = expiresAt,
    remainingSeconds = remainingSeconds
)

private fun String?.toReservationStatusOrDefault(): ReservationStatus =
    this?.let { runCatching { ReservationStatus.valueOf(it) }.getOrNull() } ?: ReservationStatus.EXPIRED
