package com.turkcell.rencar.presentation.screen.cardetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.rencar.R
import com.turkcell.rencar.presentation.component.map.DEFAULT_CENTER
import com.turkcell.rencar.presentation.component.map.LatLng
import com.turkcell.rencar.presentation.component.map.RencarMap
import com.turkcell.rencar.presentation.component.map.RencarMapController
import com.turkcell.rencar.presentation.theme.extendedColors
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun CarDetailRoute(
    modifier: Modifier = Modifier,
    viewModel: CarDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onIntent(CarDetailIntent.ScreenStarted)
        viewModel.effect.collect { effect ->
            when (effect) {
                CarDetailEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    CarDetailScreen(state = state, modifier = modifier, onIntent = viewModel::onIntent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarDetailScreen(
    state: CarDetailState,
    modifier: Modifier = Modifier,
    onIntent: (CarDetailIntent) -> Unit
) {
    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.PartiallyExpanded,
        skipHiddenState = false
    )
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)

    // Sheet tamamen aşağı çekilip kapandığında (Hidden) ekran geri navigasyonuyla kapatılır.
    LaunchedEffect(sheetState) {
        snapshotFlow { sheetState.currentValue }.collect { value ->
            if (value == SheetValue.Hidden) {
                onIntent(CarDetailIntent.BackClicked)
            }
        }
    }

    var mapController by remember { mutableStateOf<RencarMapController?>(null) }
    var hasCenteredOnVehicle by remember { mutableStateOf(false) }

    LaunchedEffect(state.hasLoaded, mapController) {
        val controller = mapController
        if (state.hasLoaded && state.errorMessage == null && controller != null && !hasCenteredOnVehicle) {
            controller.animateTo(LatLng(state.vehicleLatitude, state.vehicleLongitude), zoom = 16.0)
            hasCenteredOnVehicle = true
        }
    }

    BottomSheetScaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        sheetPeekHeight = SHEET_PEEK_HEIGHT,
        sheetContainerColor = MaterialTheme.colorScheme.surface,
        sheetContent = {
            CarDetailSheetContent(state = state, onIntent = onIntent)
        }
    ) { _ ->
        Box(modifier = Modifier.fillMaxSize()) {
            RencarMap(
                modifier = Modifier.fillMaxSize(),
                initialCenter = DEFAULT_CENTER,
                initialZoom = 15.0,
                myLocation = null,
                onControllerReady = { mapController = it }
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = MAP_SCRIM_ALPHA))
            )

            Box(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(start = 18.dp, top = 12.dp)
                    .size(42.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { onIntent(CarDetailIntent.BackClicked) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun CarDetailSheetContent(
    state: CarDetailState,
    onIntent: (CarDetailIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 4.dp)
    ) {
        when {
            state.isLoading && !state.hasLoaded -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            state.errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = state.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = stringResource(R.string.home_vehicles_retry),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .clickable { onIntent(CarDetailIntent.RetryClicked) }
                    )
                }
            }

            else -> CarDetailContent(state = state)
        }
    }
}

@Composable
private fun CarDetailContent(state: CarDetailState) {
    val distanceText = remember(
        state.myLatitude,
        state.myLongitude,
        state.vehicleLatitude,
        state.vehicleLongitude
    ) {
        val myLat = state.myLatitude
        val myLng = state.myLongitude
        if (myLat == null || myLng == null) {
            null
        } else {
            formatDistance(
                haversineMeters(myLat, myLng, state.vehicleLatitude, state.vehicleLongitude)
            )
        }
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "${state.brand} ${state.model}".trim(),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Box(
            modifier = Modifier
                .padding(start = 8.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(MaterialTheme.extendedColors.successContainer)
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = stringResource(R.string.car_detail_status_available),
                color = MaterialTheme.extendedColors.success,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }

    Text(
        text = if (distanceText != null) {
            stringResource(R.string.car_detail_meta_with_distance, state.plate, distanceText)
        } else {
            state.plate
        },
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.padding(top = 4.dp)
    )

    HorizontalDivider(
        modifier = Modifier.padding(top = 16.dp),
        color = MaterialTheme.colorScheme.outline
    )

    val perMinute = state.pricePerDay / MINUTES_PER_DAY
    val perHour = state.pricePerDay / HOURS_PER_DAY

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = formatCurrency(perMinute),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = " ${stringResource(R.string.car_detail_price_per_minute_suffix)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = stringResource(R.string.car_detail_price_per_hour, formatCurrency(perHour)),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        OutlinedButton(
            onClick = {},
            modifier = Modifier.height(56.dp),
            shape = RoundedCornerShape(18.dp)
        ) {
            Text(
                text = stringResource(R.string.car_detail_reserve_button),
                fontWeight = FontWeight.Bold
            )
        }
        Button(
            onClick = {},
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(
                text = stringResource(R.string.car_detail_unlock_button),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun haversineMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val earthRadiusMeters = 6_371_000.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
        cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return earthRadiusMeters * c
}

private fun formatDistance(meters: Double): String = if (meters < METERS_IN_KILOMETER) {
    "~${meters.roundToInt()} m"
} else {
    "~${String.format(TURKISH_LOCALE, "%.1f", meters / METERS_IN_KILOMETER)} km"
}

private fun formatCurrency(value: Double): String =
    "₺${String.format(TURKISH_LOCALE, "%.2f", value)}"

private val SHEET_PEEK_HEIGHT = 420.dp
private val TURKISH_LOCALE: Locale = Locale.forLanguageTag("tr-TR")
private const val MAP_SCRIM_ALPHA = 0.35f
private const val MINUTES_PER_DAY = 1440.0
private const val HOURS_PER_DAY = 24.0
private const val METERS_IN_KILOMETER = 1000.0
