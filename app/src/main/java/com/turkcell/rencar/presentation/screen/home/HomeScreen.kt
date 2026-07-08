package com.turkcell.rencar.presentation.screen.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.turkcell.rencar.R
import com.turkcell.rencar.presentation.component.map.LatLng
import com.turkcell.rencar.presentation.component.map.RencarMap
import com.turkcell.rencar.presentation.component.map.RencarMapController
import com.turkcell.rencar.presentation.component.navigation.BottomNavBar

private const val LOCATION_UPDATE_INTERVAL_MS = 5000L

@Composable
fun HomeRoute(
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results.values.any { it }
        viewModel.onIntent(HomeIntent.LocationPermissionResult(granted))
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(locationPermissions)
    }

    LaunchedEffect(Unit) {
        viewModel.onIntent(HomeIntent.ScreenStarted)
        viewModel.effect.collect { effect ->
            when (effect) {
                HomeEffect.NavigateToProfile -> onNavigateToProfile()
            }
        }
    }

    // Yalnızca izin durumu (verildi/reddedildi) değiştiğinde konum güncellemelerini yeniden kurar.
    DisposableEffect(state.permissionDenied) {
        var callback: LocationCallback? = null
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (state.permissionDenied == false && hasPermission) {
            val request = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                LOCATION_UPDATE_INTERVAL_MS
            ).build()

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    result.lastLocation?.let { location ->
                        viewModel.onIntent(
                            HomeIntent.MyLocationChanged(
                                LatLng(location.latitude, location.longitude)
                            )
                        )
                    }
                }
            }
            callback = locationCallback
            fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
        }

        onDispose {
            callback?.let { fusedLocationClient.removeLocationUpdates(it) }
        }
    }

    HomeScreen(
        state = state,
        modifier = modifier,
        onIntent = { intent ->
            when (intent) {
                // İzin sonucu her zaman platform diyaloğu üzerinden geldiğinden Route burada yakalar.
                HomeIntent.RequestLocationPermissionClicked -> permissionLauncher.launch(locationPermissions)
                else -> viewModel.onIntent(intent)
            }
        }
    )
}

@Composable
fun HomeScreen(
    state: HomeState,
    modifier: Modifier = Modifier,
    onIntent: (HomeIntent) -> Unit
) {
    var mapController by remember { mutableStateOf<RencarMapController?>(null) }
    var hasAnimatedToUser by remember { mutableStateOf(false) }

    LaunchedEffect(state.myLocation, mapController) {
        val location = state.myLocation
        val controller = mapController
        if (location != null && controller != null && !hasAnimatedToUser) {
            controller.animateTo(location)
            hasAnimatedToUser = true
        }
    }

    val filteredVehicles = state.vehicles.filter { vehicle ->
        state.selectedCategory == null || vehicle.category == state.selectedCategory
    }

    Column(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            RencarMap(
                modifier = Modifier.fillMaxSize(),
                myLocation = state.myLocation,
                vehicles = filteredVehicles,
                onControllerReady = { mapController = it }
            )

            if (state.isVehiclesLoading && !state.hasLoadedVehicles) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            HomeSearchBar(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .fillMaxWidth()
            )

            HomeLocateMeFab(
                onClick = {
                    val location = state.myLocation
                    if (location != null) {
                        mapController?.animateTo(location)
                    } else {
                        onIntent(HomeIntent.RequestLocationPermissionClicked)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 16.dp)
            )
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            if (state.permissionDenied == true) {
                HomeLocationPermissionBanner(
                    onGrantClick = { onIntent(HomeIntent.RequestLocationPermissionClicked) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            HomeNearbyInfoCard(
                nearbyCount = filteredVehicles.size,
                locationLabel = state.locationLabel,
                distanceLabel = state.distanceLabel,
                selectedCategory = state.selectedCategory,
                onCategorySelected = { onIntent(HomeIntent.CategorySelected(it)) },
                onFindNearestClicked = { onIntent(HomeIntent.FindNearestClicked) }
            )

            state.vehiclesErrorMessage?.let { message ->
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = stringResource(R.string.home_vehicles_retry),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .clickable { onIntent(HomeIntent.RetryVehiclesClicked) }
                    )
                }
            }

            BottomNavBar(
                selectedItem = state.selectedNavItem,
                onItemSelected = { onIntent(HomeIntent.NavItemSelected(it)) }
            )
        }
    }
}
