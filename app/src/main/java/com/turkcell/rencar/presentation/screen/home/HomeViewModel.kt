package com.turkcell.rencar.presentation.screen.home

import androidx.lifecycle.viewModelScope
import com.turkcell.rencar.domain.rental.RentalRepository
import com.turkcell.rencar.domain.rental.RentalResult
import com.turkcell.rencar.domain.rental.RentalStatus
import com.turkcell.rencar.domain.reservation.ReservationError
import com.turkcell.rencar.domain.reservation.ReservationRepository
import com.turkcell.rencar.domain.reservation.ReservationResult
import com.turkcell.rencar.domain.reservation.ReservationVehicleSummary
import com.turkcell.rencar.domain.vehicle.Vehicle
import com.turkcell.rencar.domain.vehicle.VehicleError
import com.turkcell.rencar.domain.vehicle.VehicleRepository
import com.turkcell.rencar.domain.vehicle.VehicleResult
import com.turkcell.rencar.presentation.component.map.VehicleMarker
import com.turkcell.rencar.presentation.component.navigation.BottomNavItem
import com.turkcell.rencar.presentation.core.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val reservationRepository: ReservationRepository,
    private val rentalRepository: RentalRepository
) : MviViewModel<HomeState, HomeIntent, HomeEffect>(HomeState()) {

    override fun onIntent(intent: HomeIntent) {
        when (intent) {
            HomeIntent.ScreenStarted -> {
                if (!state.value.hasCheckedActiveReservation) {
                    checkActiveReservation()
                } else if (!state.value.hasCheckedActiveRental && state.value.activeReservationErrorMessage == null) {
                    checkActiveRental()
                } else if (!state.value.hasLoadedVehicles && state.value.activeReservationErrorMessage == null) {
                    loadVehicles()
                }
            }

            HomeIntent.RetryVehiclesClicked -> {
                if (state.value.activeReservationErrorMessage != null) {
                    checkActiveReservation()
                } else {
                    loadVehicles()
                }
            }

            HomeIntent.RefreshMapClicked -> {
                if (state.value.activeReservationErrorMessage != null) {
                    checkActiveReservation()
                } else {
                    loadVehicles()
                }
            }

            is HomeIntent.LocationPermissionResult ->
                setState {
                    copy(
                        permissionDenied = intent.granted.not(),
                        canRequestPermission = intent.canRequestAgain
                    )
                }

            is HomeIntent.MyLocationChanged ->
                setState { copy(myLocation = intent.location) }

            // Route tarafından yakalanıp izin isteme launcher'ına çevrilir (bkz. LicenseUploadRoute örüntüsü).
            HomeIntent.RequestLocationPermissionClicked -> Unit

            is HomeIntent.CategorySelected ->
                setState { copy(selectedCategory = intent.category) }

            // Konum izni verilmeden (veya henüz konum alınmadan) en yakın araç aranmaz; buton sessizce hiçbir şey yapmaz.
            HomeIntent.FindNearestClicked -> {
                val locationGranted = state.value.permissionDenied == false
                val myLocation = state.value.myLocation
                if (locationGranted && myLocation != null) {
                    val visibleVehicles = state.value.vehicles.filter { vehicle ->
                        state.value.selectedCategory == null || vehicle.category == state.value.selectedCategory
                    }
                    val nearestVehicle = visibleVehicles.minByOrNull { vehicle ->
                        haversineMeters(myLocation.latitude, myLocation.longitude, vehicle.latitude, vehicle.longitude)
                    }
                    // CarDetailScreen açılışta kendi haritasını bu aracın konumuna zoom'lar (bkz. docs/decisions.md, 2026-07-10).
                    nearestVehicle?.let { vehicle ->
                        sendEffect { HomeEffect.NavigateToCarDetail(vehicle.id, myLocation) }
                    }
                }
            }

            is HomeIntent.NavItemSelected -> {
                // Konum izni verilmeden harita dışındaki ekranlara geçiş engellenir (bkz. docs/decisions.md).
                val locationGranted = state.value.permissionDenied == false
                if (!locationGranted) {
                    Unit
                } else when (intent.item) {
                    BottomNavItem.Profile -> sendEffect { HomeEffect.NavigateToProfile }
                    BottomNavItem.History -> sendEffect { HomeEffect.NavigateToHistory }
                    else -> setState { copy(selectedNavItem = intent.item) }
                }
            }

            is HomeIntent.VehicleMarkerClicked -> {
                // Konum izni verilmeden CarDetail ekranına geçiş engellenir (bkz. docs/decisions.md).
                val locationGranted = state.value.permissionDenied == false
                if (locationGranted) {
                    sendEffect { HomeEffect.NavigateToCarDetail(intent.vehicleId, state.value.myLocation) }
                }
            }

            HomeIntent.ActiveRentalBannerClicked -> {
                val rentalId = state.value.activeRentalId
                val vehicleId = state.value.activeRentalVehicleId
                if (rentalId != null && vehicleId != null) {
                    sendEffect { HomeEffect.NavigateToActiveRentalScreen(rentalId, vehicleId) }
                }
            }

            HomeIntent.ActiveReservationBannerClicked -> {
                val vehicleId = state.value.activeReservationVehicleId
                if (vehicleId != null) {
                    sendEffect { HomeEffect.NavigateToCarDetail(vehicleId, state.value.myLocation) }
                }
            }
        }
    }

    private fun loadVehicles() {
        if (state.value.isVehiclesLoading) return

        setState { copy(isVehiclesLoading = true, vehiclesErrorMessage = null) }
        viewModelScope.launch {
            when (val result = vehicleRepository.listAvailableVehicles()) {
                is VehicleResult.Success -> setState {
                    copy(
                        vehicles = result.data.map { it.toMarker() }.withActiveReservationMarker(),
                        isVehiclesLoading = false,
                        hasLoadedVehicles = true
                    )
                }

                is VehicleResult.Failure -> setState {
                    copy(
                        vehicles = listOfNotNull(activeReservationMarker),
                        isVehiclesLoading = false,
                        hasLoadedVehicles = true,
                        vehiclesErrorMessage = result.error.toMessage()
                    )
                }
            }
        }
    }

    private fun checkActiveReservation() {
        if (state.value.isCheckingActiveReservation) return

        setState {
            copy(
                isCheckingActiveReservation = true,
                activeReservationErrorMessage = null,
                vehiclesErrorMessage = null
            )
        }
        viewModelScope.launch {
            when (val result = reservationRepository.getActiveReservation()) {
                is ReservationResult.Success -> {
                    val reservation = result.data
                    setState {
                        copy(
                            isCheckingActiveReservation = false,
                            hasCheckedActiveReservation = true,
                            activeReservationVehicleId = reservation.vehicleId,
                            activeReservationVehicleName = "${reservation.vehicle.brand} ${reservation.vehicle.model}".trim(),
                            activeReservationMarker = reservation.vehicle.toMarker()
                        )
                    }
                    checkActiveRental()
                }

                is ReservationResult.Failure -> {
                    if (result.error == ReservationError.NotFound) {
                        setState {
                            copy(
                                isCheckingActiveReservation = false,
                                hasCheckedActiveReservation = true,
                                activeReservationVehicleId = null,
                                activeReservationVehicleName = "",
                                activeReservationMarker = null,
                                activeReservationErrorMessage = null
                            )
                        }
                        checkActiveRental()
                    } else {
                        setState {
                            copy(
                                isCheckingActiveReservation = false,
                                hasCheckedActiveReservation = true,
                                hasLoadedVehicles = true,
                                isVehiclesLoading = false,
                                activeReservationErrorMessage = result.error.toMessage()
                            )
                        }
                    }
                }
            }
        }
    }

    // Rezervasyon kiralamaya donusunce "aktif rezervasyon" kalmaz; bu nedenle rezervasyon
    // bulunamadiginda da kullanicinin yarim kalan foto akisi (PREPARING) veya suren yolculugu
    // (ACTIVE) olup olmadigi kontrol edilir. Hata durumunda sessizce arac listesine dusulur
    // (bkz. CarDetailViewModel.loadCanUnlock ile ayni orunku).
    private fun checkActiveRental() {
        if (state.value.isCheckingActiveRental) return

        setState { copy(isCheckingActiveRental = true) }
        viewModelScope.launch {
            when (val result = rentalRepository.getMyRentals()) {
                is RentalResult.Success -> {
                    val match = result.data.firstOrNull { it.status in RESUMABLE_RENTAL_STATUSES }
                    setState { copy(isCheckingActiveRental = false, hasCheckedActiveRental = true) }
                    when (match?.status) {
                        RentalStatus.PREPARING -> sendEffect {
                            HomeEffect.NavigateToActiveRentalPhotoUpload(match.id, match.vehicleId)
                        }

                        // ACTIVE kiralama artık Home'u zorla terk ettirmez; kullanıcı geri tuşuyla
                        // Aktif Kiralama ekranından döndüğünde de aynı bilgi çipini görür (bkz.
                        // docs/decisions.md).
                        RentalStatus.ACTIVE -> {
                            setState {
                                copy(activeRentalId = match.id, activeRentalVehicleId = match.vehicleId)
                            }
                            loadActiveRentalVehicleName(match.vehicleId)
                            loadVehicles()
                        }

                        else -> loadVehicles()
                    }
                }

                is RentalResult.Failure -> {
                    setState { copy(isCheckingActiveRental = false, hasCheckedActiveRental = true) }
                    loadVehicles()
                }
            }
        }
    }

    private fun loadActiveRentalVehicleName(vehicleId: String) {
        viewModelScope.launch {
            when (val result = vehicleRepository.getVehicle(vehicleId)) {
                is VehicleResult.Success -> setState {
                    copy(activeRentalVehicleName = "${result.data.brand} ${result.data.model}".trim())
                }

                is VehicleResult.Failure -> Unit
            }
        }
    }

    private fun Vehicle.toMarker() = VehicleMarker(
        id = id,
        latitude = latitude,
        longitude = longitude,
        price = pricePerDay.toInt(),
        category = type
    )

    private fun List<VehicleMarker>.withActiveReservationMarker(): List<VehicleMarker> {
        val reservationVehicleId = state.value.activeReservationVehicleId ?: return this
        if (any { it.id == reservationVehicleId }) {
            return map { marker ->
                if (marker.id == reservationVehicleId) marker.copy(isReservedByMe = true) else marker
            }
        }
        val marker = state.value.activeReservationMarker ?: return this
        return this + marker
    }

    private fun ReservationVehicleSummary.toMarker() = VehicleMarker(
        id = id,
        latitude = latitude,
        longitude = longitude,
        price = pricePerMinute.toInt(),
        category = type,
        isReservedByMe = true
    )

    private fun VehicleError.toMessage(): String = when (this) {
        VehicleError.Unauthorized, VehicleError.Forbidden -> UNAUTHORIZED_MESSAGE
        VehicleError.Network -> NETWORK_ERROR_MESSAGE
        VehicleError.NotFound, VehicleError.Unexpected -> UNEXPECTED_ERROR_MESSAGE
    }

    private fun ReservationError.toMessage(): String = when (this) {
        ReservationError.Unauthorized, ReservationError.Forbidden -> UNAUTHORIZED_MESSAGE
        ReservationError.Network -> NETWORK_ERROR_MESSAGE
        ReservationError.InvalidRequest,
        ReservationError.Conflict,
        ReservationError.NotFound,
        ReservationError.Unexpected -> ACTIVE_RESERVATION_ERROR_MESSAGE
    }

    private companion object {
        const val UNAUTHORIZED_MESSAGE = "Oturumunuz sona ermis. Lutfen tekrar giris yapin."
        const val NETWORK_ERROR_MESSAGE = "Internet baglantinizi kontrol edip tekrar deneyin."
        const val UNEXPECTED_ERROR_MESSAGE = "Araclar yuklenemedi. Lutfen tekrar deneyin."
        const val ACTIVE_RESERVATION_ERROR_MESSAGE =
            "Aktif rezervasyon durumu kontrol edilemedi. Lutfen tekrar deneyin."
        val RESUMABLE_RENTAL_STATUSES = setOf(RentalStatus.PREPARING, RentalStatus.ACTIVE)
    }
}

// CarDetailScreen.kt'deki private haversineMeters ile ayni formul; ekranlar birbirinden bagimsiz
// oldugundan (bkz. docs/decisions.md) burada da ayni sekilde ozel/private tutulmustur.
private fun haversineMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val earthRadiusMeters = 6_371_000.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
        kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
        kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)
    val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
    return earthRadiusMeters * c
}
