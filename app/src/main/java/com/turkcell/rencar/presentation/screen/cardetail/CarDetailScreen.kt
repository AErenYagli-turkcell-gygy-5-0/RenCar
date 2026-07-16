package com.turkcell.rencar.presentation.screen.cardetail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.rencar.R
import com.turkcell.rencar.presentation.component.map.DEFAULT_CENTER
import com.turkcell.rencar.presentation.component.map.LatLng
import com.turkcell.rencar.domain.vehicle.Transmission
import com.turkcell.rencar.domain.vehicle.VehicleSegment
import com.turkcell.rencar.domain.vehicle.VehicleStatus
import com.turkcell.rencar.domain.vehicle.VehicleType
import com.turkcell.rencar.presentation.component.map.RencarMap
import com.turkcell.rencar.presentation.component.map.RencarMapController
import com.turkcell.rencar.presentation.component.map.color
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
    onNavigateBack: () -> Unit,
    onNavigateToReservationConfirmation: (vehicleId: String) -> Unit,
    onNavigateToRentalPhotoUpload: (rentalId: String, vehicleId: String) -> Unit,
    onNavigateToActiveRental: (rentalId: String, vehicleId: String) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onIntent(CarDetailIntent.ScreenStarted)
        viewModel.effect.collect { effect ->
            when (effect) {
                CarDetailEffect.NavigateBack -> onNavigateBack()
                is CarDetailEffect.NavigateToReservationConfirmation ->
                    onNavigateToReservationConfirmation(effect.vehicleId)

                is CarDetailEffect.NavigateToRentalPhotoUpload ->
                    onNavigateToRentalPhotoUpload(effect.rentalId, effect.vehicleId)

                is CarDetailEffect.NavigateToActiveRental ->
                    onNavigateToActiveRental(effect.rentalId, effect.vehicleId)
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
            .heightIn(min = SHEET_PEEK_HEIGHT)
            .verticalScroll(rememberScrollState())
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

            else -> CarDetailContent(state = state, onIntent = onIntent)
        }
    }
}

@Composable
private fun CarDetailContent(
    state: CarDetailState,
    onIntent: (CarDetailIntent) -> Unit
) {
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

    val statusPresentation = state.status.toPresentation()

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
                .background(statusPresentation.containerColor)
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = stringResource(statusPresentation.labelRes),
                color = statusPresentation.contentColor,
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = formatCurrency(state.pricePerMinute),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = " ${stringResource(R.string.car_detail_price_per_minute_suffix)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            if (state.hasFullVehicleDetails) {
                Text(
                    text = stringResource(R.string.car_detail_price_per_hour, formatCurrency(state.pricePerHour)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.car_detail_price_per_day, formatCurrency(state.pricePerDay)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (state.hasFullVehicleDetails) {
        Text(
            text = stringResource(R.string.car_detail_specs_title),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 18.dp, bottom = 10.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CategoryChip(
                label = stringResource(state.type.labelRes()),
                color = state.type.color(MaterialTheme.extendedColors),
                modifier = Modifier.weight(1f)
            )
            CategoryChip(
                label = stringResource(state.segment.labelRes()),
                color = state.segment.chipColor(),
                modifier = Modifier.weight(1f)
            )
            IconTextChip(
                drawIcon = { color -> drawGearboxIcon(color) },
                text = stringResource(state.transmission.labelRes()),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconTextChip(
                drawIcon = { color -> drawSeatIcon(color) },
                text = stringResource(R.string.car_detail_spec_seats_value, state.seats),
                modifier = Modifier.weight(1f)
            )

            val fuelFraction = (state.fuelPercent / 100.0).coerceIn(0.0, 1.0).toFloat()
            val fuelColor = when {
                state.fuelPercent <= FUEL_CRITICAL_THRESHOLD -> MaterialTheme.colorScheme.error
                state.fuelPercent <= FUEL_LOW_THRESHOLD -> MaterialTheme.extendedColors.warning
                else -> MaterialTheme.extendedColors.success
            }
            StatCard(
                drawIcon = { color -> drawFuelIcon(color) },
                label = stringResource(R.string.car_detail_spec_fuel_label),
                value = stringResource(R.string.car_detail_spec_fuel_value, state.fuelPercent.roundToInt()),
                progressFraction = fuelFraction,
                progressColor = fuelColor,
                modifier = Modifier.weight(1f)
            )

            StatCard(
                drawIcon = { color -> drawRouteIcon(color) },
                label = stringResource(R.string.car_detail_spec_range_label),
                value = stringResource(R.string.car_detail_spec_range_value, state.rangeKm.roundToInt()),
                modifier = Modifier.weight(1f)
            )
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 18.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        OutlinedButton(
            onClick = { onIntent(CarDetailIntent.ReserveClicked) },
            enabled = !state.isActiveReservationVehicle,
            modifier = Modifier.height(56.dp),
            shape = RoundedCornerShape(18.dp)
        ) {
            Text(
                text = stringResource(R.string.car_detail_reserve_button),
                fontWeight = FontWeight.Bold
            )
        }
        Button(
            onClick = { onIntent(CarDetailIntent.UnlockClicked) },
            enabled = state.canUnlock,
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
                .shadow(
                    elevation = 14.dp,
                    shape = RoundedCornerShape(18.dp),
                    ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                ),
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

@Composable
private fun CategoryChip(label: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = STATUS_CONTAINER_ALPHA))
            .padding(horizontal = 10.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = color
        )
    }
}

@Composable
private fun IconTextChip(
    drawIcon: DrawScope.(color: Color) -> Unit,
    text: String,
    modifier: Modifier = Modifier
) {
    val contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Canvas(modifier = Modifier.size(14.dp)) { drawIcon(contentColor) }
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            modifier = Modifier.padding(start = 6.dp)
        )
    }
}

@Composable
private fun StatCard(
    drawIcon: DrawScope.(color: Color) -> Unit,
    label: String,
    value: String,
    progressFraction: Float? = null,
    progressColor: Color? = null,
    modifier: Modifier = Modifier
) {
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Canvas(modifier = Modifier.size(14.dp)) { drawIcon(labelColor) }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = labelColor,
                modifier = Modifier.padding(start = 6.dp)
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 5.dp)
        )
        if (progressFraction != null && progressColor != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 7.dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progressFraction)
                        .background(progressColor)
                )
            }
        }
    }
}

private data class StatusPresentation(
    val labelRes: Int,
    val contentColor: Color,
    val containerColor: Color
)

@Composable
private fun VehicleStatus.toPresentation(): StatusPresentation = when (this) {
    VehicleStatus.AVAILABLE -> StatusPresentation(
        labelRes = R.string.car_detail_status_available,
        contentColor = MaterialTheme.extendedColors.success,
        containerColor = MaterialTheme.extendedColors.successContainer
    )

    VehicleStatus.RESERVED -> StatusPresentation(
        labelRes = R.string.car_detail_status_reserved,
        contentColor = MaterialTheme.extendedColors.warning,
        containerColor = MaterialTheme.extendedColors.warning.copy(alpha = STATUS_CONTAINER_ALPHA)
    )

    VehicleStatus.RENTED -> StatusPresentation(
        labelRes = R.string.car_detail_status_rented,
        contentColor = MaterialTheme.extendedColors.warning,
        containerColor = MaterialTheme.extendedColors.warning.copy(alpha = STATUS_CONTAINER_ALPHA)
    )

    VehicleStatus.MAINTENANCE -> StatusPresentation(
        labelRes = R.string.car_detail_status_maintenance,
        contentColor = MaterialTheme.colorScheme.error,
        containerColor = MaterialTheme.colorScheme.errorContainer
    )
}

private fun VehicleType.labelRes(): Int = when (this) {
    VehicleType.SEDAN -> R.string.home_filter_sedan
    VehicleType.SUV -> R.string.home_filter_suv
    VehicleType.HATCHBACK -> R.string.home_filter_hatchback
    VehicleType.STATION -> R.string.home_filter_station
    VehicleType.MINIVAN -> R.string.home_filter_minivan
}

private fun VehicleSegment.labelRes(): Int = when (this) {
    VehicleSegment.ECONOMY -> R.string.car_detail_segment_economy
    VehicleSegment.COMFORT -> R.string.car_detail_segment_comfort
    VehicleSegment.SUV -> R.string.car_detail_segment_suv
}

@Composable
private fun VehicleSegment.chipColor(): Color = when (this) {
    VehicleSegment.ECONOMY -> MaterialTheme.extendedColors.categoryEconomic
    VehicleSegment.COMFORT -> MaterialTheme.extendedColors.categoryPremium
    VehicleSegment.SUV -> MaterialTheme.extendedColors.categorySuv
}

private fun Transmission.labelRes(): Int = when (this) {
    Transmission.MANUAL -> R.string.car_detail_transmission_manual
    Transmission.AUTOMATIC -> R.string.car_detail_transmission_automatic
}

private fun DrawScope.drawFuelIcon(color: Color) {
    val stroke = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round)
    val bodyWidth = size.width * 0.55f
    val bodyHeight = size.height * 0.85f
    val bodyTop = size.height - bodyHeight

    drawRect(
        color = color,
        topLeft = Offset(0f, bodyTop),
        size = Size(bodyWidth, bodyHeight),
        style = stroke
    )

    val hoseStartX = bodyWidth
    val hoseY = bodyTop + bodyHeight * 0.25f
    drawLine(
        color = color,
        start = Offset(hoseStartX, hoseY),
        end = Offset(size.width * 0.92f, hoseY),
        strokeWidth = stroke.width,
        cap = StrokeCap.Round
    )
    drawLine(
        color = color,
        start = Offset(size.width * 0.92f, hoseY),
        end = Offset(size.width * 0.92f, hoseY + bodyHeight * 0.35f),
        strokeWidth = stroke.width,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawRouteIcon(color: Color) {
    val stroke = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round)
    val centerX = size.width / 2f
    val circleCenterY = size.height * 0.36f
    val circleRadius = size.width * 0.34f

    val tailPath = Path().apply {
        moveTo(centerX - circleRadius * 0.8f, circleCenterY + circleRadius * 0.6f)
        lineTo(centerX, size.height * 0.95f)
        lineTo(centerX + circleRadius * 0.8f, circleCenterY + circleRadius * 0.6f)
        close()
    }
    drawPath(path = tailPath, color = color)
    drawCircle(color = color, radius = circleRadius, center = Offset(centerX, circleCenterY), style = stroke)
    drawCircle(color = color, radius = circleRadius * 0.35f, center = Offset(centerX, circleCenterY))
}

private fun DrawScope.drawGearboxIcon(color: Color) {
    val stroke = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round)
    val leftX = size.width * 0.22f
    val rightX = size.width * 0.78f
    val topY = size.height * 0.12f
    val bottomY = size.height * 0.88f
    val midY = size.height * 0.5f

    drawLine(color = color, start = Offset(leftX, topY), end = Offset(leftX, bottomY), strokeWidth = stroke.width, cap = StrokeCap.Round)
    drawLine(color = color, start = Offset(rightX, topY), end = Offset(rightX, bottomY), strokeWidth = stroke.width, cap = StrokeCap.Round)
    drawLine(color = color, start = Offset(leftX, midY), end = Offset(rightX, midY), strokeWidth = stroke.width, cap = StrokeCap.Round)
    drawCircle(color = color, radius = size.minDimension * 0.09f, center = Offset(leftX, topY))
    drawCircle(color = color, radius = size.minDimension * 0.09f, center = Offset(rightX, topY))
}

private fun DrawScope.drawSeatIcon(color: Color) {
    val stroke = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round)
    val backPath = Path().apply {
        moveTo(size.width * 0.3f, size.height * 0.15f)
        lineTo(size.width * 0.3f, size.height * 0.65f)
        lineTo(size.width * 0.85f, size.height * 0.65f)
        lineTo(size.width * 0.85f, size.height * 0.85f)
    }
    drawPath(path = backPath, color = color, style = stroke)
    drawLine(
        color = color,
        start = Offset(size.width * 0.15f, size.height * 0.85f),
        end = Offset(size.width * 0.85f, size.height * 0.85f),
        strokeWidth = stroke.width,
        cap = StrokeCap.Round
    )
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
private const val STATUS_CONTAINER_ALPHA = 0.15f
private const val METERS_IN_KILOMETER = 1000.0
private const val FUEL_LOW_THRESHOLD = 50.0
private const val FUEL_CRITICAL_THRESHOLD = 15.0
