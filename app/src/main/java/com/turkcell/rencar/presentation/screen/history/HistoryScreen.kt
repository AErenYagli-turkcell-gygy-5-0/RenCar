package com.turkcell.rencar.presentation.screen.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.style.TextOverflow
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
import java.util.Date
import kotlin.math.roundToInt

@Composable
fun HistoryRoute(
    onNavigateToMap: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToWallet: () -> Unit,
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
                HistoryEffect.NavigateToWallet -> onNavigateToWallet()
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
                text = stringResource(R.string.history_overview_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 3.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HistorySummaryCard(
                    label = stringResource(R.string.history_trip_count_label),
                    value = state.tripCount.toString(),
                    modifier = Modifier.weight(1f)
                )
                HistorySummaryCard(
                    label = stringResource(R.string.history_total_spent_label),
                    value = state.totalSpent.toTryPrice(),
                    modifier = Modifier.weight(1f)
                )
            }
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
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(top = 14.dp).clickable { onIntent(HistoryIntent.RetryClicked) }
                    ) {
                    Text(
                        text = stringResource(R.string.home_vehicles_retry),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 11.dp)
                    )
                    }
                }

                state.items.isEmpty() -> Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_clock_outline),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                        modifier = Modifier.size(42.dp)
                    )
                    Text(
                        text = stringResource(R.string.history_empty),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 14.dp)
                    )
                    Text(
                        text = stringResource(R.string.history_empty_description),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 6.dp)
                    )
                }

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    state.items.groupBy { it.startedAt.toMonthYear() }.forEach { (month, monthItems) ->
                        item(key = "month-$month") {
                            Text(
                                text = month,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        items(monthItems, key = { it.id }) { item -> HistoryItemCard(item) }
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
private fun HistorySummaryCard(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun HistoryItemCard(item: RentalHistoryItem) {
    val colors = MaterialTheme.extendedColors
    Surface(
        modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp)),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(item.vehicleType.color(colors).copy(alpha = 0.16f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_rencar_car),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(item.vehicleType.color(colors)),
                    modifier = Modifier.size(32.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${item.vehicleBrand} ${item.vehicleModel}".trim(),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = (item.totalPrice ?: 0.0).toTryPrice(),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = item.vehiclePlate, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Surface(color = colors.successContainer, shape = RoundedCornerShape(12.dp)) {
                        Text(
                            text = stringResource(R.string.history_completed),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.success,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
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

private fun String.toDate(): Date? {
    val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    return runCatching { parser.parse(this) }.getOrNull()
}

private fun String.toFormattedDate(): String {
    val date = toDate() ?: return this
    val formatter = SimpleDateFormat("d MMM yyyy · HH:mm", Locale.forLanguageTag("tr-TR"))
    return formatter.format(date)
}

private fun String.toMonthYear(): String {
    val date = toDate() ?: return this
    val locale = Locale.forLanguageTag("tr-TR")
    return SimpleDateFormat("MMMM yyyy", locale).format(date)
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
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
