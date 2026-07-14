package com.turkcell.rencar.presentation.screen.rental.active

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
fun ActiveRentalRoute(
    rentalId: String,
    vehicleId: String,
    onNavigateToFinishPhotoUpload: (rentalId: String, vehicleId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ActiveRentalViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(rentalId) {
        viewModel.onIntent(ActiveRentalIntent.ScreenStarted(rentalId, vehicleId))
    }
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ActiveRentalEffect.NavigateToFinishPhotoUpload ->
                    onNavigateToFinishPhotoUpload(effect.rentalId, effect.vehicleId)
            }
        }
    }

    ActiveRentalScreen(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier
    )
}

@Composable
fun ActiveRentalScreen(
    state: ActiveRentalState,
    onIntent: (ActiveRentalIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StatusPill(vehicleName = state.vehicleName)
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                .padding(horizontal = 22.dp, vertical = 24.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.active_rental_elapsed_label),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = state.elapsedSeconds.toClockText(),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 44.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(11.dp)
            ) {
                StatCard(
                    label = stringResource(R.string.active_rental_current_cost_label),
                    value = state.currentCost.toTryPrice(),
                    valueColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = stringResource(R.string.active_rental_distance_label),
                    value = stringResource(R.string.active_rental_distance_km, state.distanceKm.toKmText()),
                    valueColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }

            state.errorMessage?.let { error ->
                Text(
                    text = error,
                    modifier = Modifier.padding(top = 12.dp),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(11.dp)
            ) {
                OutlinedButton(
                    onClick = {},
                    enabled = false,
                    modifier = Modifier.weight(1f).height(54.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(text = stringResource(R.string.active_rental_lock_unlock), fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { onIntent(ActiveRentalIntent.FinishClicked) },
                    enabled = !state.isFinishing,
                    modifier = Modifier.weight(1f).height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    if (state.isFinishing) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(20.dp),
                            color = MaterialTheme.colorScheme.onError,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(text = stringResource(R.string.active_rental_finish_action), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusPill(vehicleName: String) {
    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.inverseSurface, RoundedCornerShape(30.dp))
            .padding(horizontal = 18.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .height(8.dp)
                .background(MaterialTheme.colorScheme.tertiary, RoundedCornerShape(50))
                .border(0.dp, Color.Transparent)
                .padding(4.dp)
        )
        Text(
            text = if (vehicleName.isNotBlank()) {
                stringResource(R.string.active_rental_status_active_with_vehicle, vehicleName)
            } else {
                stringResource(R.string.active_rental_status_active)
            },
            color = MaterialTheme.colorScheme.inverseOnSurface,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
private fun StatCard(label: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 13.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = valueColor
            )
        }
    }
}

private fun Long.toClockText(): String {
    val hours = this / 3600
    val minutes = (this % 3600) / 60
    val seconds = this % 60
    return "%02d:%02d:%02d".format(hours, minutes, seconds)
}

private fun Double.toTryPrice(): String {
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("tr-TR")).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 2
    }
    return "₺${formatter.format(this)}"
}

private fun Double.toKmText(): String {
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("tr-TR")).apply {
        minimumFractionDigits = 1
        maximumFractionDigits = 1
    }
    return formatter.format(this)
}

@Preview(showBackground = true)
@Composable
private fun ActiveRentalPreview() {
    RenCarTheme(darkTheme = false) {
        ActiveRentalScreen(
            state = ActiveRentalState(
                vehicleName = "Renault Clio",
                elapsedSeconds = 1458,
                currentCost = 108.0,
                distanceKm = 12.4
            ),
            onIntent = {}
        )
    }
}
