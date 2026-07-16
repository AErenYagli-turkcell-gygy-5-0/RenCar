package com.turkcell.rencar.presentation.screen.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.rencar.R
import com.turkcell.rencar.domain.rental.RentalHistoryItem
import com.turkcell.rencar.domain.vehicle.VehicleType
import com.turkcell.rencar.presentation.component.map.color
import com.turkcell.rencar.presentation.component.navigation.BottomNavBar
import com.turkcell.rencar.presentation.theme.RenCarTheme
import com.turkcell.rencar.presentation.theme.extendedColors
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

@Composable
fun HistoryRoute(
    onNavigateToMap: () -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onIntent(HistoryIntent.ScreenStarted)
        viewModel.effect.collect { effect ->
            when (effect) {
                HistoryEffect.NavigateToMap -> onNavigateToMap()
                HistoryEffect.NavigateToProfile -> onNavigateToProfile()
            }
        }
    }

    HistoryScreen(state = state, onIntent = viewModel::onIntent, modifier = modifier)
}

@Composable
fun HistoryScreen(
    state: HistoryState,
    onIntent: (HistoryIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 22.dp, vertical = 6.dp)
        ) {
            Text(
                text = stringResource(R.string.history_title),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(
                    R.string.history_subtitle,
                    state.tripCount,
                    state.totalSpent.toTryPrice()
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 3.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            when {
                state.isLoading && state.items.isEmpty() -> Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }

                state.errorMessage != null -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = state.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
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
                            .padding(top = 8.dp)
                            .clickable { onIntent(HistoryIntent.RetryClicked) }
                    )
                }

                state.items.isEmpty() -> Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.history_empty),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.items, key = { it.id }) { item ->
                        HistoryItemCard(item)
                    }
                }
            }
        }

        BottomNavBar(
            selectedItem = state.selectedNavItem,
            onItemSelected = { onIntent(HistoryIntent.NavItemSelected(it)) }
        )
    }
}

@Composable
private fun HistoryItemCard(item: RentalHistoryItem) {
    val colors = MaterialTheme.extendedColors
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            Column(
                modifier = Modifier
                    .size(56.dp)
                    .background(item.vehicleType.color(colors), RoundedCornerShape(14.dp))
            ) {}

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${item.vehicleBrand} ${item.vehicleModel}".trim(),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = (item.totalPrice ?: 0.0).toTryPrice(),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = item.startedAt.toFormattedDate(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 3.dp)
                )
                Row(
                    modifier = Modifier.padding(top = 9.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HistoryBadge(text = stringResource(R.string.history_duration_minutes, item.durationMinutes.roundToInt()))
                    HistoryBadge(text = stringResource(R.string.active_rental_distance_km, item.distanceKm.toKmText()))
                }
            }
        }
    }
}

@Composable
private fun HistoryBadge(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
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

private fun Double.toKmText(): String {
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("tr-TR")).apply {
        minimumFractionDigits = 1
        maximumFractionDigits = 1
    }
    return formatter.format(this)
}

private fun String.toFormattedDate(): String {
    val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    val date = runCatching { parser.parse(this) }.getOrNull() ?: return this
    val formatter = SimpleDateFormat("d MMM yyyy · HH:mm", Locale("tr", "TR"))
    return formatter.format(date)
}

@Preview(showBackground = true)
@Composable
private fun HistoryScreenPreview() {
    RenCarTheme(darkTheme = false) {
        HistoryScreen(
            state = HistoryState(
                items = listOf(
                    RentalHistoryItem(
                        id = "1",
                        vehicleBrand = "Renault",
                        vehicleModel = "Clio",
                        vehiclePlate = "34 ABC 123",
                        vehicleType = VehicleType.HATCHBACK,
                        totalPrice = 110.50,
                        distanceKm = 12.4,
                        durationMinutes = 24.0,
                        startedAt = "2026-06-26T14:32:00.000Z"
                    )
                ),
                tripCount = 6,
                totalSpent = 612.0
            ),
            onIntent = {}
        )
    }
}
