package com.turkcell.rencar.presentation.screen.reservation.confirmation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.rencar.R
import com.turkcell.rencar.domain.rental.RentalPlan
import com.turkcell.rencar.domain.vehicle.Transmission
import com.turkcell.rencar.presentation.theme.RenCarTheme
import com.turkcell.rencar.presentation.theme.extendedColors
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun ReservationConfirmationRoute(
    vehicleId: String,
    onNavigateBack: () -> Unit,
    onReservationCreated: (rentalId: String, vehicleId: String, isPreparing: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReservationConfirmationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(vehicleId) { viewModel.onIntent(ReservationConfirmationIntent.ScreenStarted(vehicleId)) }
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                ReservationConfirmationEffect.NavigateBack -> onNavigateBack()
                is ReservationConfirmationEffect.ReservationCreated ->
                    onReservationCreated(effect.rentalId, effect.vehicleId, effect.isPreparing)
            }
        }
    }
    ReservationConfirmationScreen(state, viewModel::onIntent, modifier)
}

@Composable
fun ReservationConfirmationScreen(
    state: ReservationConfirmationState,
    onIntent: (ReservationConfirmationIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Header { onIntent(ReservationConfirmationIntent.BackClicked) }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        Box(Modifier.weight(1f).fillMaxWidth()) {
            when {
                state.isLoading && !state.hasLoaded -> LoadingContent()
                !state.hasLoaded && state.error != null -> ErrorContent(state.error) {
                    onIntent(ReservationConfirmationIntent.RetryClicked)
                }
                else -> Content(state, onIntent)
            }
        }
        BottomBar(state.canComplete, state.isSubmitting) {
            onIntent(ReservationConfirmationIntent.CompleteReservationClicked)
        }
    }
}

@Composable
private fun Header(onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(72.dp).padding(horizontal = 18.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(42.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) { BackIcon(MaterialTheme.colorScheme.onSurface, Modifier.size(18.dp)) }
        Text(
            stringResource(R.string.reservation_confirmation_title), Modifier.padding(start = 14.dp),
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
private fun Content(state: ReservationConfirmationState, onIntent: (ReservationConfirmationIntent) -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        VehicleCard(state)
        PlanCard(state) { onIntent(ReservationConfirmationIntent.PlanSelected(it)) }
        PriceCard(state)
        TermsRow(state.termsAccepted) { onIntent(ReservationConfirmationIntent.TermsAcceptanceChanged(it)) }
        state.error?.let {
            Text(it.toMessage(), Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun VehicleCard(state: ReservationConfirmationState) {
    CardSurface {
        Column(Modifier.fillMaxWidth().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(state.vehicleName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Text(
                listOf(
                    state.plate,
                    stringResource(if (state.transmission == Transmission.MANUAL) R.string.car_detail_transmission_manual else R.string.car_detail_transmission_automatic),
                    stringResource(R.string.reservation_seats, state.seats)
                ).joinToString(" · "),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.reservation_fuel, state.fuelPercent.roundToInt()),
                Modifier.background(MaterialTheme.extendedColors.successContainer, RoundedCornerShape(7.dp)).padding(horizontal = 9.dp, vertical = 4.dp),
                color = MaterialTheme.extendedColors.success,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
private fun PlanCard(state: ReservationConfirmationState, onPlan: (RentalPlan) -> Unit) {
    CardSurface {
        Column(Modifier.padding(16.dp)) {
            Text(stringResource(R.string.reservation_rental_plan), style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PlanOption(RentalPlan.PER_MINUTE, stringResource(R.string.reservation_per_minute), stringResource(R.string.reservation_price_per_minute, state.pricePerMinute.toTryPrice()), state.selectedPlan, onPlan, Modifier.weight(1f))
                PlanOption(RentalPlan.HOURLY, stringResource(R.string.reservation_hourly), stringResource(R.string.reservation_price_per_hour, state.pricePerHour.toTryPrice()), state.selectedPlan, onPlan, Modifier.weight(1f))
                PlanOption(RentalPlan.DAILY, stringResource(R.string.reservation_daily), state.pricePerDay.toTryPrice(), state.selectedPlan, onPlan, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun PlanOption(plan: RentalPlan, title: String, price: String, selected: RentalPlan, onPlan: (RentalPlan) -> Unit, modifier: Modifier) {
    val active = plan == selected
    Column(
        modifier.height(62.dp).border(1.dp, if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, RoundedCornerShape(13.dp))
            .background(if (active) MaterialTheme.colorScheme.primaryContainer else Color.Transparent, RoundedCornerShape(13.dp))
            .clickable { onPlan(plan) }.padding(vertical = 9.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(title, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
        Text(price, style = MaterialTheme.typography.bodySmall, color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun PriceCard(state: ReservationConfirmationState) {
    CardSurface {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 15.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            PriceRow(stringResource(R.string.reservation_free_hold), stringResource(R.string.reservation_free_hold_duration))
            PriceRow(stringResource(R.string.reservation_usage_fee), state.usageFee.toTryPrice())
            PriceRow(stringResource(R.string.reservation_start_fee), state.startFee.toTryPrice())
            PriceRow(stringResource(R.string.reservation_service_fee), state.serviceFee.toTryPrice())
            PriceRow(
                when (state.selectedPlan) {
                    RentalPlan.PER_MINUTE -> stringResource(R.string.reservation_estimated_minutes, state.quoteMinutes)
                    RentalPlan.HOURLY -> stringResource(R.string.reservation_estimated_first_hour)
                    RentalPlan.DAILY -> stringResource(R.string.reservation_estimated_one_day)
                },
                if (state.isQuoteLoading) stringResource(R.string.reservation_calculating) else stringResource(R.string.reservation_estimated_price, state.estimatedTotal.toTryPrice()),
                true
            )
        }
    }
}

@Composable
private fun CardSurface(content: @Composable () -> Unit) {
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline), content = content)
}

@Composable
private fun PriceRow(label: String, value: String, bold: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (bold) FontWeight.Bold else FontWeight.SemiBold))
    }
}

@Composable
private fun TermsRow(checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().clickable { onChange(!checked) }, verticalAlignment = Alignment.Top) {
        Checkbox(checked, onChange, colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary))
        Text(stringResource(R.string.reservation_terms_confirmation), Modifier.padding(top = 12.dp, end = 4.dp), style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun BottomBar(enabled: Boolean, loading: Boolean, onClick: () -> Unit) {
    Surface(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) {
        Button(onClick, Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp).height(56.dp), enabled = enabled,
            shape = RoundedCornerShape(16.dp)) {
            if (loading) CircularProgressIndicator(Modifier.size(22.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
            else Text(stringResource(R.string.reservation_complete), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
        }
    }
}

@Composable private fun LoadingContent() = Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }

@Composable
private fun ErrorContent(error: ReservationConfirmationError, retry: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(error.toMessage(), textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.reservation_retry), Modifier.clickable(onClick = retry), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ReservationConfirmationError.toMessage() = when (this) {
    ReservationConfirmationError.VEHICLE_NOT_FOUND -> stringResource(R.string.reservation_error_vehicle_not_found)
    ReservationConfirmationError.RESERVATION_CONFLICT -> stringResource(R.string.reservation_error_conflict)
    ReservationConfirmationError.UNAUTHORIZED -> stringResource(R.string.reservation_error_unauthorized)
    ReservationConfirmationError.FORBIDDEN -> stringResource(R.string.reservation_error_forbidden)
    ReservationConfirmationError.INVALID_REQUEST -> stringResource(R.string.reservation_error_invalid_request)
    ReservationConfirmationError.NETWORK -> stringResource(R.string.reservation_error_network)
    ReservationConfirmationError.UNEXPECTED -> stringResource(R.string.reservation_error_unexpected)
}

@Composable
private fun BackIcon(color: Color, modifier: Modifier = Modifier) = Canvas(modifier) {
    val width = 2.dp.toPx()
    drawLine(color, Offset(size.width * .65f, size.height * .18f), Offset(size.width * .34f, size.height * .5f), width, StrokeCap.Round)
    drawLine(color, Offset(size.width * .34f, size.height * .5f), Offset(size.width * .65f, size.height * .82f), width, StrokeCap.Round)
}

private fun Double.toTryPrice(): String = "₺" + NumberFormat.getNumberInstance(Locale.forLanguageTag("tr-TR")).apply {
    minimumFractionDigits = 0; maximumFractionDigits = 2
}.format(this)

private val previewState = ReservationConfirmationState(
    vehicleId = "vehicle-1", vehicleName = "Renault Clio", plate = "34 RNC 022", seats = 5,
    fuelPercent = 72.0, pricePerMinute = 4.5, pricePerHour = 180.0, pricePerDay = 1450.0,
    usageFee = 120.0, startFee = 15.0, serviceFee = 6.0, estimatedTotal = 141.0,
    hasLoaded = true, hasQuote = true, termsAccepted = true
)

@Preview(showBackground = true) @Composable private fun LightPreview() = RenCarTheme(false) { ReservationConfirmationScreen(previewState, {}) }
@Preview(showBackground = true) @Composable private fun DarkPreview() = RenCarTheme(true) { ReservationConfirmationScreen(previewState, {}) }
