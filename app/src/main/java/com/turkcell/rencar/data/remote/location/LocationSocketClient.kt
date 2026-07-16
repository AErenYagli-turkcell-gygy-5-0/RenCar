package com.turkcell.rencar.data.remote.location

import com.turkcell.rencar.BuildConfig
import com.turkcell.rencar.data.session.SessionTokenHolder
import com.turkcell.rencar.domain.location.VehicleLocation
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.json.JSONObject
import javax.inject.Inject

// Backend sözleşmesi (bkz. kullanıcı isteği): Socket.IO namespace /ws/locations, "my-vehicle"
// event'i yalnızca CUSTOMER'ın aktif kiralamasındaki aracın konumunu taşır. Projede reaktif token
// yenileme mekanizması olmadığından (bkz. docs/decisions.md, 2026-07-13 "Kapsam sınırı") burada da
// eklenmez; connect_error alınırsa akış sessizce kapanır.
class LocationSocketClient @Inject constructor(
    private val sessionTokenHolder: SessionTokenHolder
) {
    fun observeMyVehicle(): Flow<VehicleLocation> = callbackFlow {
        val token = sessionTokenHolder.accessToken
        if (token == null) {
            close()
            return@callbackFlow
        }

        val options = IO.Options().apply {
            auth = mapOf(AUTH_TOKEN_FIELD to token)
            forceNew = true
            reconnection = true
        }
        val socket = IO.socket(BuildConfig.API_BASE_URL.trimEnd('/') + NAMESPACE, options)

        socket.on(MY_VEHICLE_EVENT) { args ->
            parseVehicleLocation(args)?.let { trySend(it) }
        }
        socket.on(Socket.EVENT_CONNECT_ERROR) { close() }
        socket.connect()

        awaitClose {
            socket.off()
            socket.disconnect()
            socket.close()
        }
    }

    private fun parseVehicleLocation(args: Array<Any?>): VehicleLocation? {
        val root = args.getOrNull(0) as? JSONObject ?: return null
        val vehicle = root.optJSONObject(VEHICLE_FIELD) ?: return null
        val vehicleId = vehicle.optString(VEHICLE_ID_FIELD).takeIf { it.isNotBlank() } ?: return null
        val latitude = vehicle.optDouble(LATITUDE_FIELD, Double.NaN)
        val longitude = vehicle.optDouble(LONGITUDE_FIELD, Double.NaN)
        if (latitude.isNaN() || longitude.isNaN()) return null
        return VehicleLocation(vehicleId = vehicleId, latitude = latitude, longitude = longitude)
    }

    private companion object {
        const val NAMESPACE = "/ws/locations"
        const val MY_VEHICLE_EVENT = "my-vehicle"
        const val AUTH_TOKEN_FIELD = "token"
        const val VEHICLE_FIELD = "vehicle"
        const val VEHICLE_ID_FIELD = "vehicleId"
        const val LATITUDE_FIELD = "latitude"
        const val LONGITUDE_FIELD = "longitude"
    }
}
