package com.turkcell.rencar.data.repository.rental

import android.content.Context
import android.net.Uri
import com.turkcell.rencar.data.remote.rental.RentalApiService
import com.turkcell.rencar.data.remote.rental.dto.ActiveRentalResponseDto
import com.turkcell.rencar.data.remote.rental.dto.CreateRentalRequestDto
import com.turkcell.rencar.data.remote.rental.dto.RentalHistoryItemResponseDto
import com.turkcell.rencar.data.remote.rental.dto.RentalPhotosStateResponseDto
import com.turkcell.rencar.data.remote.rental.dto.RentalResponseDto
import com.turkcell.rencar.data.remote.rental.dto.RentalStatsResponseDto
import com.turkcell.rencar.data.remote.rental.dto.RentalSummaryResponseDto
import com.turkcell.rencar.domain.rental.ActiveRental
import com.turkcell.rencar.domain.rental.Rental
import com.turkcell.rencar.domain.rental.RentalError
import com.turkcell.rencar.domain.rental.RentalHistoryItem
import com.turkcell.rencar.domain.rental.RentalPhotoSide
import com.turkcell.rencar.domain.rental.RentalPhotosState
import com.turkcell.rencar.domain.rental.RentalPlan
import com.turkcell.rencar.domain.rental.RentalRepository
import com.turkcell.rencar.domain.rental.RentalResult
import com.turkcell.rencar.domain.rental.RentalStats
import com.turkcell.rencar.domain.rental.RentalStatus
import com.turkcell.rencar.domain.rental.RentalSummary
import com.turkcell.rencar.domain.vehicle.VehicleType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class ApiRentalRepository @Inject constructor(
    @ApplicationContext private val context: Context,
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

    override suspend fun getRentalHistory(): RentalResult<List<RentalHistoryItem>> = runRequest {
        apiService.listMineDetailed()
            .filter { it.status == COMPLETED_STATUS }
            .map { it.toDomain() }
    }

    override suspend fun getRentalStats(): RentalResult<RentalStats> = runRequest {
        apiService.getStats().toDomain()
    }

    override suspend fun uploadRentalPhoto(
        rentalId: String,
        side: RentalPhotoSide,
        imageUri: Uri
    ): RentalResult<RentalPhotosState> = runRequest {
        val sidePart = MultipartBody.Part.createFormData(SIDE_FIELD_NAME, side.name)
        val filePart = imageUri.toMultipartPart(FILE_FIELD_NAME)
        apiService.uploadPhoto(id = rentalId, side = sidePart, file = filePart).toDomain()
    }

    override suspend fun getRentalPhotos(rentalId: String): RentalResult<RentalPhotosState> = runRequest {
        apiService.getPhotos(id = rentalId).toDomain()
    }

    override suspend fun startRental(rentalId: String): RentalResult<Rental> = runRequest {
        apiService.start(id = rentalId).toDomain()
    }

    override suspend fun cancelRental(rentalId: String): RentalResult<Unit> = runRequest {
        apiService.cancel(id = rentalId)
    }

    override suspend fun getActiveRental(): RentalResult<ActiveRental> = runRequest {
        apiService.getActive().toDomain()
    }

    override suspend fun finishRental(rentalId: String): RentalResult<Rental> = runRequest {
        apiService.finish(id = rentalId).toDomain()
    }

    private suspend fun <T> runRequest(request: suspend () -> T): RentalResult<T> = try {
        RentalResult.Success(request())
    } catch (error: CancellationException) {
        throw error
    } catch (error: HttpException) {
        RentalResult.Failure(error.code().toRentalError())
    } catch (error: IOException) {
        RentalResult.Failure(RentalError.Network)
    } catch (error: Exception) {
        RentalResult.Failure(RentalError.Unexpected)
    }

    private fun Uri.toMultipartPart(fieldName: String): MultipartBody.Part {
        val bytes = context.contentResolver.openInputStream(this)?.use { it.readBytes() }
            ?: throw IOException("Görsel okunamadı.")
        val mimeType = context.contentResolver.getType(this) ?: DEFAULT_MIME_TYPE
        val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(fieldName, "$fieldName.jpg", requestBody)
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
        const val SIDE_FIELD_NAME = "side"
        const val FILE_FIELD_NAME = "file"
        const val DEFAULT_MIME_TYPE = "image/jpeg"
        const val HTTP_BAD_REQUEST = 400
        const val HTTP_UNAUTHORIZED = 401
        const val HTTP_FORBIDDEN = 403
        const val HTTP_NOT_FOUND = 404
        const val HTTP_CONFLICT = 409
        const val COMPLETED_STATUS = "COMPLETED"
    }
}

internal fun RentalResponseDto.toDomain() = Rental(
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

internal fun RentalSummaryResponseDto.toDomainOrNull(): RentalSummary? {
    val resolvedVehicleId = vehicleId ?: return null
    return RentalSummary(
        id = id,
        vehicleId = resolvedVehicleId,
        status = status.toRentalStatusOrDefault()
    )
}

internal fun RentalPhotosStateResponseDto.toDomain() = RentalPhotosState(
    rentalId = rentalId,
    uploadedSides = photos.mapNotNull { it.side.toRentalPhotoSideOrNull() }.toSet(),
    remainingSides = remainingSides.mapNotNull { it.toRentalPhotoSideOrNull() }.toSet(),
    photosComplete = photosComplete
)

internal fun ActiveRentalResponseDto.toDomain() = ActiveRental(
    id = id,
    vehicleId = vehicleId,
    status = status.toRentalStatusOrDefault(),
    plan = plan.toRentalPlanOrDefault(),
    startFee = startFee,
    startedAt = startedAt,
    elapsedSeconds = elapsedSeconds,
    currentCost = currentCost,
    distanceKm = distanceKm
)

private fun String?.toRentalStatusOrDefault(): RentalStatus =
    this?.let { runCatching { RentalStatus.valueOf(it) }.getOrNull() } ?: RentalStatus.COMPLETED

private fun String.toRentalPhotoSideOrNull(): RentalPhotoSide? =
    runCatching { RentalPhotoSide.valueOf(this) }.getOrNull()

internal fun RentalHistoryItemResponseDto.toDomain() = RentalHistoryItem(
    id = id,
    vehicleBrand = vehicle.brand,
    vehicleModel = vehicle.model,
    vehiclePlate = vehicle.plate,
    vehicleType = vehicle.type.toVehicleTypeOrDefault(),
    totalPrice = totalPrice,
    distanceKm = distanceKm,
    durationMinutes = durationMinutes,
    startedAt = startedAt
)

internal fun RentalStatsResponseDto.toDomain() = RentalStats(
    tripCount = tripCount,
    totalSpent = totalSpent,
    totalMinutes = totalMinutes,
    totalKm = totalKm
)

private fun String.toVehicleTypeOrDefault(): VehicleType =
    runCatching { VehicleType.valueOf(this) }.getOrDefault(VehicleType.SEDAN)
