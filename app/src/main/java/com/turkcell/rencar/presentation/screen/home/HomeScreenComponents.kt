package com.turkcell.rencar.presentation.screen.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.turkcell.rencar.R
import com.turkcell.rencar.presentation.component.map.VehicleCategory
import com.turkcell.rencar.presentation.component.map.color
import com.turkcell.rencar.presentation.theme.extendedColors
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HomeSearchBar(
    modifier: Modifier = Modifier,
    onFilterClick: () -> Unit = {}
) {
    val searchIconColor = Color(0xFF9AA3AE)
    val filterIconColor = MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Canvas(modifier = Modifier.size(18.dp)) { drawSearchIcon(color = searchIconColor) }
            Text(
                text = stringResource(R.string.home_search_placeholder),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 10.dp)
            )
        }

        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(onClick = onFilterClick),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(16.dp)) { drawFilterIcon(color = filterIconColor) }
        }
    }
}

@Composable
fun HomeLocateMeFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconColor = MaterialTheme.colorScheme.primary

    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        shape = CircleShape,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary,
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
    ) {
        Canvas(modifier = Modifier.size(22.dp)) { drawLocateMeIcon(color = iconColor) }
    }
}

@Composable
fun HomeLocationPermissionBanner(
    onGrantClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.extendedColors.categoryPremium.copy(alpha = 0.12f))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(R.string.home_location_permission_banner),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = stringResource(R.string.home_location_permission_action),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(start = 10.dp)
                .clickable(onClick = onGrantClick)
        )
    }
}

@Composable
fun HomeFilterChipRow(
    selectedCategory: VehicleCategory?,
    onCategorySelected: (VehicleCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.extendedColors
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HomeCategoryChip(
            label = stringResource(R.string.home_filter_all),
            selected = selectedCategory == null,
            dotColor = null,
            selectedColor = MaterialTheme.colorScheme.primary,
            onClick = { onCategorySelected(null) }
        )
        HomeCategoryChip(
            label = stringResource(R.string.home_filter_economic),
            selected = selectedCategory == VehicleCategory.Economic,
            dotColor = VehicleCategory.Economic.color(colors),
            selectedColor = VehicleCategory.Economic.color(colors),
            onClick = { onCategorySelected(VehicleCategory.Economic) }
        )
        HomeCategoryChip(
            label = stringResource(R.string.home_filter_comfort),
            selected = selectedCategory == VehicleCategory.Comfort,
            dotColor = VehicleCategory.Comfort.color(colors),
            selectedColor = VehicleCategory.Comfort.color(colors),
            onClick = { onCategorySelected(VehicleCategory.Comfort) }
        )
        HomeCategoryChip(
            label = stringResource(R.string.home_filter_suv),
            selected = selectedCategory == VehicleCategory.Suv,
            dotColor = VehicleCategory.Suv.color(colors),
            selectedColor = VehicleCategory.Suv.color(colors),
            onClick = { onCategorySelected(VehicleCategory.Suv) }
        )
    }
}

@Composable
private fun HomeCategoryChip(
    label: String,
    selected: Boolean,
    dotColor: Color?,
    selectedColor: Color,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text = label) },
        leadingIcon = if (dotColor != null) {
            { Canvas(modifier = Modifier.size(8.dp)) { drawCircle(color = dotColor) } }
        } else {
            null
        },
        shape = RoundedCornerShape(50),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedContainerColor = selectedColor,
            selectedLabelColor = Color.White
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = MaterialTheme.colorScheme.outline,
            selectedBorderColor = selectedColor
        )
    )
}

@Composable
fun HomeNearbyInfoCard(
    nearbyCount: Int,
    locationLabel: String,
    distanceLabel: String,
    selectedCategory: VehicleCategory?,
    onCategorySelected: (VehicleCategory?) -> Unit,
    onFindNearestClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Text(
            text = stringResource(R.string.home_nearby_count, nearbyCount),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "$locationLabel · $distanceLabel",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp)
        )

        HomeFilterChipRow(
            selectedCategory = selectedCategory,
            onCategorySelected = onCategorySelected,
            modifier = Modifier.padding(top = 14.dp)
        )

        Button(
            onClick = onFindNearestClicked,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(
                text = stringResource(R.string.home_find_nearest_button),
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}

private fun DrawScope.drawSearchIcon(color: Color) {
    val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
    val radius = size.minDimension * 0.32f
    val center = Offset(size.width * 0.42f, size.height * 0.42f)
    drawCircle(color = color, radius = radius, center = center, style = stroke)
    drawLine(
        color = color,
        start = Offset(center.x + radius * 0.7f, center.y + radius * 0.7f),
        end = Offset(size.width * 0.92f, size.height * 0.92f),
        strokeWidth = stroke.width,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawFilterIcon(color: Color) {
    val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
    val ys = listOf(size.height * 0.2f, size.height * 0.5f, size.height * 0.8f)
    val widths = listOf(0.9f, 0.65f, 0.4f)
    ys.forEachIndexed { index, y ->
        drawLine(
            color = color,
            start = Offset(size.width * (1f - widths[index]) / 2f, y),
            end = Offset(size.width * (1f + widths[index]) / 2f, y),
            strokeWidth = stroke.width,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawLocateMeIcon(color: Color) {
    val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
    val center = Offset(size.width / 2f, size.height / 2f)
    val outerRadius = size.minDimension * 0.36f
    val innerRadius = size.minDimension * 0.12f
    drawCircle(color = color, radius = outerRadius, center = center, style = stroke)
    drawCircle(color = color, radius = innerRadius, center = center)
    listOf(0f, 90f, 180f, 270f).forEach { angleDegrees ->
        val angle = Math.toRadians(angleDegrees.toDouble())
        val start = Offset(
            x = center.x + (outerRadius * 1.15f * cos(angle)).toFloat(),
            y = center.y + (outerRadius * 1.15f * sin(angle)).toFloat()
        )
        val end = Offset(
            x = center.x + (outerRadius * 1.5f * cos(angle)).toFloat(),
            y = center.y + (outerRadius * 1.5f * sin(angle)).toFloat()
        )
        drawLine(color = color, start = start, end = end, strokeWidth = stroke.width, cap = StrokeCap.Round)
    }
}
