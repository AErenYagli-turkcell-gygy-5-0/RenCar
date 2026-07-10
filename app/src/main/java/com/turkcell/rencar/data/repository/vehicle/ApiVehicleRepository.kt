package com.turkcell.rencar.data.repository.vehicle

import com.turkcell.rencar.data.remote.vehicle.VehicleApiService
import com.turkcell.rencar.data.remote.vehicle.dto.VehicleResponseDto
import com.turkcell.rencar.domain.vehicle.Vehicle
import com.turkcell.rencar.domain.vehicle.VehicleError
import com.turkcell.rencar.domain.vehicle.VehicleRepository
import com.turkcell.rencar.domain.vehicle.VehicleResult
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

    override suspend fun getAvailableVehicle(vehicleId: String): VehicleResult<Vehicle> = try {
        VehicleResult.Success(apiService.getOne(vehicleId).toDomain())
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
    type = VehicleType.valueOf(type),
    pricePerDay = pricePerDay,
    latitude = latitude,
    longitude = longitude
)
