package com.turkcell.rencar.presentation.screen.reservation.confirmation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.turkcell.rencar.presentation.theme.RenCarTheme
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ReservationConfirmationRoute(
    vehicleId: String,
    onNavigateBack: () -> Unit,
    onReservationCreated: (rentalId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReservationConfirmationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(vehicleId) {
        viewModel.onIntent(ReservationConfirmationIntent.ScreenStarted(vehicleId))
    }
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                ReservationConfirmationEffect.NavigateBack -> onNavigateBack()
                is ReservationConfirmationEffect.ReservationCreated -> onReservationCreated(effect.rentalId)
            }
        }
    }

    ReservationConfirmationScreen(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier
    )
}

@Composable
fun ReservationConfirmationScreen(
    state: ReservationConfirmationState,
    onIntent: (ReservationConfirmationIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        ReservationHeader(onBack = { onIntent(ReservationConfirmationIntent.BackClicked) })
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when {
                state.isLoading && !state.hasLoaded -> LoadingContent()
                !state.hasLoaded && state.error != null -> ErrorContent(
                    error = state.error,
                    onRetry = { onIntent(ReservationConfirmationIntent.RetryClicked) }
                )

                else -> ReservationContent(
                    state = state,
                    onTermsChanged = {
                        onIntent(ReservationConfirmationIntent.TermsAcceptanceChanged(it))
                    }
                )
            }
        }

        ReservationBottomBar(
            enabled = state.canComplete,
            isSubmitting = state.isSubmitting,
            onClick = { onIntent(ReservationConfirmationIntent.CompleteReservationClicked) }
        )
    }
}

@Composable
private fun ReservationHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            BackIcon(
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(18.dp)
            )
        }
        Text(
            text = stringResource(R.string.reservation_confirmation_title),
            modifier = Modifier.padding(start = 14.dp),
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun ReservationContent(
    state: ReservationConfirmationState,
    onTermsChanged: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        VehicleSummaryCard(state)
        DailyPlanCard(state.pricePerDay)
        PriceSummaryCard(state.pricePerDay)
        TermsRow(
            checked = state.termsAccepted,
            onCheckedChange = onTermsChanged
        )
        state.error?.let { error ->
            Text(
                text = error.toMessage(),
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun VehicleSummaryCard(state: ReservationConfirmationState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = state.vehicleName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = listOf(state.plate, state.vehicleType).filter(String::isNotBlank).joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DailyPlanCard(pricePerDay: Double) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.reservation_rental_plan),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp))
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        RoundedCornerShape(14.dp)
                    )
                    .padding(vertical = 13.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.reservation_daily),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.reservation_price_per_day, pricePerDay.toTryPrice()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun PriceSummaryCard(pricePerDay: Double) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 15.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PriceRow(
                label = stringResource(R.string.reservation_daily_fee),
                value = pricePerDay.toTryPrice()
            )
            PriceRow(
                label = stringResource(R.string.reservation_estimated_total),
                value = stringResource(R.string.reservation_estimated_price, pricePerDay.toTryPrice()),
                emphasized = true
            )
        }
    }
}

@Composable
private fun PriceRow(label: String, value: String, emphasized: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (emphasized) FontWeight.Bold else FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun TermsRow(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.Top
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                checkmarkColor = MaterialTheme.colorScheme.onPrimary
            )
        )
        Text(
            text = stringResource(R.string.reservation_terms_confirmation),
            modifier = Modifier.padding(top = 12.dp, end = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun ReservationBottomBar(enabled: Boolean, isSubmitting: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp)
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = stringResource(R.string.reservation_complete),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun ErrorContent(error: ReservationConfirmationError, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = error.toMessage(),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.reservation_retry),
            modifier = Modifier.clickable(onClick = onRetry),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
private fun ReservationConfirmationError.toMessage(): String = when (this) {
    ReservationConfirmationError.VEHICLE_NOT_FOUND -> stringResource(R.string.reservation_error_vehicle_not_found)
    ReservationConfirmationError.RESERVATION_CONFLICT -> stringResource(R.string.reservation_error_conflict)
    ReservationConfirmationError.UNAUTHORIZED -> stringResource(R.string.reservation_error_unauthorized)
    ReservationConfirmationError.FORBIDDEN -> stringResource(R.string.reservation_error_forbidden)
    ReservationConfirmationError.INVALID_REQUEST -> stringResource(R.string.reservation_error_invalid_request)
    ReservationConfirmationError.NETWORK -> stringResource(R.string.reservation_error_network)
    ReservationConfirmationError.UNEXPECTED -> stringResource(R.string.reservation_error_unexpected)
}

@Composable
private fun BackIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = 2.dp.toPx()
        drawLine(
            color = color,
            start = Offset(size.width * 0.65f, size.height * 0.18f),
            end = Offset(size.width * 0.34f, size.height * 0.5f),
            strokeWidth = width,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.34f, size.height * 0.5f),
            end = Offset(size.width * 0.65f, size.height * 0.82f),
            strokeWidth = width,
            cap = StrokeCap.Round
        )
    }
}

private fun Double.toTryPrice(): String {
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("tr-TR")).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 2
    }
    return "₺${formatter.format(this)}"
}

@Preview(showBackground = true)
@Composable
private fun ReservationConfirmationPreview() {
    RenCarTheme(darkTheme = false) {
        ReservationConfirmationScreen(
            state = previewState,
            onIntent = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ReservationConfirmationDarkPreview() {
    RenCarTheme(darkTheme = true) {
        ReservationConfirmationScreen(
            state = previewState,
            onIntent = {}
        )
    }
}

private val previewState = ReservationConfirmationState(
    vehicleId = "vehicle-1",
    vehicleName = "Renault Clio",
    plate = "34 RNC 022",
    vehicleType = "Sedan",
    pricePerDay = 1450.0,
    hasLoaded = true,
    termsAccepted = true
)
