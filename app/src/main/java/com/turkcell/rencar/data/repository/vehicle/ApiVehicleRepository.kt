package com.turkcell.rencar.data.repository.vehicle

import com.turkcell.rencar.data.remote.vehicle.VehicleApiService
import com.turkcell.rencar.data.remote.vehicle.dto.VehicleResponseDto
import com.turkcell.rencar.domain.vehicle.Transmission
import com.turkcell.rencar.domain.vehicle.Vehicle
import com.turkcell.rencar.domain.vehicle.VehicleError
import com.turkcell.rencar.domain.vehicle.VehicleRepository
import com.turkcell.rencar.domain.vehicle.VehicleResult
import com.turkcell.rencar.domain.vehicle.VehicleSegment
import com.turkcell.rencar.domain.vehicle.VehicleStatus
import com.turkcell.rencar.domain.vehicle.VehicleType
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class ApiVehicleRepository @Inject constructor(
    private val apiService: VehicleApiService
) : VehicleRepository {

    override suspend fun listAvailableVehicles(type: VehicleType?): VehicleResult<List<Vehicle>> = try {
        VehicleResult.Success(apiService.list(type = type?.name).map { it.toDomain() })
    } catch (error: CancellationException) {
        throw error
    } catch (error: HttpException) {
        VehicleResult.Failure(error.code().toVehicleError())
    } catch (error: IOException) {
        VehicleResult.Failure(VehicleError.Network)
    } catch (error: Exception) {
        VehicleResult.Failure(VehicleError.Unexpected)
    }

    override suspend fun getVehicle(id: String): VehicleResult<Vehicle> = try {
        VehicleResult.Success(apiService.getOne(id = id).toDomain())
    } catch (error: CancellationException) {
        throw error
    } catch (error: HttpException) {
        VehicleResult.Failure(error.code().toVehicleError())
    } catch (error: IOException) {
        VehicleResult.Failure(VehicleError.Network)
    } catch (error: Exception) {
        VehicleResult.Failure(VehicleError.Unexpected)
    }

    private fun Int.toVehicleError(): VehicleError = when (this) {
        HTTP_UNAUTHORIZED -> VehicleError.Unauthorized
        HTTP_FORBIDDEN -> VehicleError.Forbidden
        HTTP_NOT_FOUND -> VehicleError.NotFound
        else -> VehicleError.Unexpected
    }

    private companion object {
        const val HTTP_UNAUTHORIZED = 401
        const val HTTP_FORBIDDEN = 403
        const val HTTP_NOT_FOUND = 404
    }
}

private fun VehicleResponseDto.toDomain() = Vehicle(
    id = id,
    plate = plate,
    brand = brand,
    model = model,
    type = type.toVehicleTypeOrDefault(),
    pricePerDay = pricePerDay,
    pricePerMinute = pricePerMinute ?: 0.0,
    pricePerHour = pricePerHour ?: 0.0,
    fuelPercent = fuelPercent ?: 0.0,
    rangeKm = rangeKm ?: 0.0,
    transmission = transmission.toTransmissionOrDefault(),
    seats = seats ?: 0,
    segment = segment.toVehicleSegmentOrDefault(),
    status = status.toVehicleStatusOrDefault(),
    latitude = latitude,
    longitude = longitude
)

// Bilinmeyen/eksik enum değeri tüm araç listesini çökertmesin diye valueOf() yerine
// güvenli varsayılana düşülür (bkz. ana ekranda "araçlar yüklenemedi" hatası, 2026-07-13).
private fun String?.toVehicleTypeOrDefault(): VehicleType =
    this?.let { runCatching { VehicleType.valueOf(it) }.getOrNull() } ?: VehicleType.SEDAN

private fun String?.toTransmissionOrDefault(): Transmission =
    this?.let { runCatching { Transmission.valueOf(it) }.getOrNull() } ?: Transmission.MANUAL

private fun String?.toVehicleSegmentOrDefault(): VehicleSegment =
    this?.let { runCatching { VehicleSegment.valueOf(it) }.getOrNull() } ?: VehicleSegment.ECONOMY

private fun String?.toVehicleStatusOrDefault(): VehicleStatus =
    this?.let { runCatching { VehicleStatus.valueOf(it) }.getOrNull() } ?: VehicleStatus.AVAILABLE
