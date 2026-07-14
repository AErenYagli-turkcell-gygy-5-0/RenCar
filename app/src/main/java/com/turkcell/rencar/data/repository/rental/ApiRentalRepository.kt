package com.turkcell.rencar.data.repository.rental

import com.turkcell.rencar.data.remote.rental.RentalApiService
import com.turkcell.rencar.data.remote.rental.dto.CreateRentalRequestDto
import com.turkcell.rencar.data.remote.rental.dto.RentalResponseDto
import com.turkcell.rencar.data.remote.rental.dto.RentalSummaryResponseDto
import com.turkcell.rencar.domain.rental.Rental
import com.turkcell.rencar.domain.rental.RentalError
import com.turkcell.rencar.domain.rental.RentalRepository
import com.turkcell.rencar.domain.rental.RentalResult
import com.turkcell.rencar.domain.rental.RentalPlan
import com.turkcell.rencar.domain.rental.RentalStatus
import com.turkcell.rencar.domain.rental.RentalSummary
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class ApiRentalRepository @Inject constructor(
    private val apiService: RentalApiService
) : RentalRepository {

    override suspend fun createRental(
        vehicleId: String,
        plan: RentalPlan,
        endDate: String?
    ): RentalResult<Rental> = try {
        val request = CreateRentalRequestDto(
            vehicleId = vehicleId,
            plan = plan.name,
            endDate = endDate.takeIf { plan == RentalPlan.DAILY }
        )
        RentalResult.Success(apiService.create(request).toDomain())
    } catch (error: CancellationException) {
        throw error
    } catch (error: HttpException) {
        RentalResult.Failure(error.code().toRentalError())
    } catch (error: IOException) {
        RentalResult.Failure(RentalError.Network)
    } catch (error: Exception) {
        RentalResult.Failure(RentalError.Unexpected)
    }

    override suspend fun getMyRentals(): RentalResult<List<RentalSummary>> = try {
        RentalResult.Success(apiService.listMine().mapNotNull { it.toDomainOrNull() })
    } catch (error: CancellationException) {
        throw error
    } catch (error: HttpException) {
        RentalResult.Failure(error.code().toRentalError())
    } catch (error: IOException) {
        RentalResult.Failure(RentalError.Network)
    } catch (error: Exception) {
        RentalResult.Failure(RentalError.Unexpected)
    }

    private fun Int.toRentalError(): RentalError = when (this) {
        HTTP_BAD_REQUEST -> RentalError.InvalidRequest
        HTTP_UNAUTHORIZED -> RentalError.Unauthorized
        HTTP_FORBIDDEN -> RentalError.Forbidden
        HTTP_NOT_FOUND -> RentalError.NotFound
        HTTP_CONFLICT -> RentalError.Conflict
        else -> RentalError.Unexpected
    }

    private companion object {
        const val HTTP_BAD_REQUEST = 400
        const val HTTP_UNAUTHORIZED = 401
        const val HTTP_FORBIDDEN = 403
        const val HTTP_NOT_FOUND = 404
        const val HTTP_CONFLICT = 409
    }
}

private fun RentalResponseDto.toDomain() = Rental(
    id = id,
    userId = userId,
    vehicleId = vehicleId,
    plan = plan.toRentalPlanOrDefault(),
    startDate = startDate,
    endDate = endDate,
    totalPrice = totalPrice,
    status = status,
    createdAt = createdAt
)

private fun String?.toRentalPlanOrDefault(): RentalPlan =
    this?.let { runCatching { RentalPlan.valueOf(it) }.getOrNull() } ?: RentalPlan.DAILY

private fun RentalSummaryResponseDto.toDomainOrNull(): RentalSummary? {
    val resolvedVehicleId = vehicleId ?: return null
    return RentalSummary(
        id = id,
        vehicleId = resolvedVehicleId,
        status = status.toRentalStatusOrDefault()
    )
}

private fun String?.toRentalStatusOrDefault(): RentalStatus =
    this?.let { runCatching { RentalStatus.valueOf(it) }.getOrNull() } ?: RentalStatus.COMPLETED
