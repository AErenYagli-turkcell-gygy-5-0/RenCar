package com.turkcell.rencar.presentation.screen.home

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Looper
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.turkcell.rencar.R
import com.turkcell.rencar.presentation.component.map.LatLng
import com.turkcell.rencar.presentation.component.map.RencarMap
import com.turkcell.rencar.presentation.component.map.RencarMapController
import com.turkcell.rencar.presentation.component.navigation.BottomNavBar

private const val LOCATION_UPDATE_INTERVAL_MS = 5000L

@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToProfile: () -> Unit,
    onNavigateToCarDetail: (String, Double?, Double?) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = LocalActivity.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results.values.any { it }
        val canRequestAgain = granted || activity == null || locationPermissions.any {
            ActivityCompat.shouldShowRequestPermissionRationale(activity, it)
        }
        viewModel.onIntent(HomeIntent.LocationPermissionResult(granted, canRequestAgain))
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(locationPermissions)
    }

    val currentPermissionDenied = rememberUpdatedState(state.permissionDenied)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val granted = locationPermissions.any {
                    ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
                }
                if (granted && currentPermissionDenied.value != false) {
                    viewModel.onIntent(HomeIntent.LocationPermissionResult(granted = true, canRequestAgain = true))
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        viewModel.onIntent(HomeIntent.ScreenStarted)
        viewModel.effect.collect { effect ->
            when (effect) {
                HomeEffect.NavigateToProfile -> onNavigateToProfile()
                is HomeEffect.NavigateToCarDetail -> onNavigateToCarDetail(
                    effect.vehicleId,
                    effect.myLocation?.latitude,
                    effect.myLocation?.longitude
                )
            }
        }
    }

    DisposableEffect(state.permissionDenied) {
        var callback: LocationCallback? = null
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (state.permissionDenied == false && hasPermission) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    viewModel.onIntent(HomeIntent.MyLocationChanged(LatLng(it.latitude, it.longitude)))
                }
            }

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

    var settingsDialogDismissed by remember { mutableStateOf(false) }
    LaunchedEffect(state.permissionDenied, state.canRequestPermission) {
        settingsDialogDismissed = false
    }

    if (state.permissionDenied == true && !state.canRequestPermission && !settingsDialogDismissed) {
        LocationPermissionSettingsDialog(
            onOpenSettingsClick = { openAppSettings(context) },
            onDismiss = { settingsDialogDismissed = true }
        )
    }

    HomeScreen(
        state = state,
        modifier = modifier,
        onIntent = { intent ->
            when (intent) {
                HomeIntent.RequestLocationPermissionClicked -> {
                    if (state.canRequestPermission) {
                        permissionLauncher.launch(locationPermissions)
                    } else {
                        openAppSettings(context)
                    }
                }

                HomeIntent.RefreshMapClicked -> {
                    val hasPermission = locationPermissions.any {
                        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
                    }
                    if (hasPermission) {
                        fusedLocationClient.getCurrentLocation(
                            Priority.PRIORITY_HIGH_ACCURACY,
                            CancellationTokenSource().token
                        ).addOnSuccessListener { location ->
                            location?.let {
                                viewModel.onIntent(HomeIntent.MyLocationChanged(LatLng(it.latitude, it.longitude)))
                            }
                        }
                    }
                    viewModel.onIntent(intent)
                }

                else -> viewModel.onIntent(intent)
            }
        }
    )
}

private fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
    }
    context.startActivity(intent)
}

@Composable
private fun LocationPermissionSettingsDialog(
    onDismiss: () -> Unit,
    onOpenSettingsClick: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onOpenSettingsClick) {
                Text(text = stringResource(R.string.home_location_permission_permanently_denied_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.home_location_permission_permanently_denied_dismiss))
            }
        },
        title = { Text(text = stringResource(R.string.home_location_permission_permanently_denied_title)) },
        text = { Text(text = stringResource(R.string.home_location_permission_permanently_denied_message)) }
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
                onControllerReady = { mapController = it },
                onVehicleClick = { vehicleId -> onIntent(HomeIntent.VehicleMarkerClicked(vehicleId)) }
            )

            if (state.isVehiclesLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            HomeRefreshMapFab(
                onClick = { onIntent(HomeIntent.RefreshMapClicked) },
                isRefreshing = state.isVehiclesLoading,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 84.dp)
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
