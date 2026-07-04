package com.turkcell.rencar.presentation.component.navigation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turkcell.rencar.presentation.theme.RenCarTheme

enum class RenCarBottomNavItem(
    val label: String
) {
    Map(label = "Harita"),
    History(label = "Ge\u00e7mi\u015f"),
    Wallet(label = "C\u00fczdan"),
    Profile(label = "Profil")
}

@Composable
fun RenCarBottomNavBar(
    selectedItem: RenCarBottomNavItem,
    onItemSelected: (RenCarBottomNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = MaterialTheme.colorScheme.surface
    val selectedColor = MaterialTheme.colorScheme.primary
    val unselectedColor = MaterialTheme.colorScheme.outlineVariant

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(82.dp)
            .clip(
                RoundedCornerShape(
                    bottomStart = 32.dp,
                    bottomEnd = 32.dp
                )
            )
            .background(containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 10.dp, bottom = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            RenCarBottomNavItem.entries.forEach { item ->
                val selected = item == selectedItem
                RenCarBottomNavBarItem(
                    item = item,
                    selected = selected,
                    color = if (selected) selectedColor else unselectedColor,
                    onClick = { onItemSelected(item) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 10.dp)
                .width(126.dp)
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.56f))
        )
    }
}

@Composable
private fun RenCarBottomNavBarItem(
    item: RenCarBottomNavItem,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                role = Role.Tab,
                onClick = onClick
            )
            .semantics {
                this.selected = selected
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        RenCarBottomNavIcon(
            item = item,
            tint = color,
            modifier = Modifier.size(24.dp)
        )

        Text(
            text = item.label,
            color = color,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                letterSpacing = 0.sp
            ),
            modifier = Modifier.padding(top = 3.dp)
        )
    }
}

@Composable
private fun RenCarBottomNavIcon(
    item: RenCarBottomNavItem,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        when (item) {
            RenCarBottomNavItem.Map -> drawMapPinIcon(tint)
            RenCarBottomNavItem.History -> drawHistoryIcon(tint)
            RenCarBottomNavItem.Wallet -> drawWalletIcon(tint)
            RenCarBottomNavItem.Profile -> drawProfileIcon(tint)
        }
    }
}

private fun DrawScope.iconStroke(): Stroke {
    return Stroke(
        width = 1.8.dp.toPx(),
        cap = StrokeCap.Round,
        join = StrokeJoin.Round
    )
}

private fun DrawScope.drawMapPinIcon(color: Color) {
    val stroke = iconStroke()
    val centerX = size.width / 2f
    val path = Path().apply {
        moveTo(centerX, size.height * 0.9f)
        cubicTo(
            size.width * 0.22f,
            size.height * 0.6f,
            size.width * 0.2f,
            size.height * 0.16f,
            centerX,
            size.height * 0.12f
        )
        cubicTo(
            size.width * 0.8f,
            size.height * 0.16f,
            size.width * 0.78f,
            size.height * 0.6f,
            centerX,
            size.height * 0.9f
        )
    }

    drawPath(path = path, color = color, style = stroke)
    drawCircle(
        color = color,
        radius = size.minDimension * 0.11f,
        center = Offset(centerX, size.height * 0.38f),
        style = stroke
    )
}

private fun DrawScope.drawHistoryIcon(color: Color) {
    val stroke = iconStroke()
    val arcSize = Size(size.width * 0.62f, size.height * 0.62f)
    val arcTopLeft = Offset(size.width * 0.22f, size.height * 0.18f)

    drawArc(
        color = color,
        startAngle = 42f,
        sweepAngle = 295f,
        useCenter = false,
        topLeft = arcTopLeft,
        size = arcSize,
        style = stroke
    )
    drawLine(
        color = color,
        start = Offset(size.width * 0.25f, size.height * 0.3f),
        end = Offset(size.width * 0.25f, size.height * 0.08f),
        strokeWidth = stroke.width,
        cap = StrokeCap.Round
    )
    drawLine(
        color = color,
        start = Offset(size.width * 0.25f, size.height * 0.3f),
        end = Offset(size.width * 0.43f, size.height * 0.3f),
        strokeWidth = stroke.width,
        cap = StrokeCap.Round
    )
    drawLine(
        color = color,
        start = Offset(size.width * 0.5f, size.height * 0.48f),
        end = Offset(size.width * 0.5f, size.height * 0.3f),
        strokeWidth = stroke.width,
        cap = StrokeCap.Round
    )
    drawLine(
        color = color,
        start = Offset(size.width * 0.5f, size.height * 0.48f),
        end = Offset(size.width * 0.66f, size.height * 0.56f),
        strokeWidth = stroke.width,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawWalletIcon(color: Color) {
    val stroke = iconStroke()
    drawRoundRect(
        color = color,
        topLeft = Offset(size.width * 0.16f, size.height * 0.24f),
        size = Size(size.width * 0.68f, size.height * 0.52f),
        cornerRadius = CornerRadius(size.width * 0.08f, size.width * 0.08f),
        style = stroke
    )
    drawLine(
        color = color,
        start = Offset(size.width * 0.2f, size.height * 0.42f),
        end = Offset(size.width * 0.8f, size.height * 0.42f),
        strokeWidth = stroke.width,
        cap = StrokeCap.Round
    )
    drawCircle(
        color = color,
        radius = size.minDimension * 0.035f,
        center = Offset(size.width * 0.68f, size.height * 0.58f)
    )
}

private fun DrawScope.drawProfileIcon(color: Color) {
    val stroke = iconStroke()
    drawCircle(
        color = color,
        radius = size.minDimension * 0.16f,
        center = Offset(size.width * 0.5f, size.height * 0.28f),
        style = stroke
    )
    drawArc(
        color = color,
        startAngle = 205f,
        sweepAngle = 130f,
        useCenter = false,
        topLeft = Offset(size.width * 0.22f, size.height * 0.46f),
        size = Size(size.width * 0.56f, size.height * 0.5f),
        style = stroke
    )
}

@Preview(name = "Bottom Nav - Light", showBackground = true, widthDp = 360)
@Composable
private fun RenCarBottomNavBarLightPreview() {
    RenCarTheme(darkTheme = false) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
        ) {
            RenCarBottomNavBar(
                selectedItem = RenCarBottomNavItem.Map,
                onItemSelected = {}
            )
        }
    }
}

@Preview(name = "Bottom Nav - Dark", showBackground = true, widthDp = 360)
@Composable
private fun RenCarBottomNavBarDarkPreview() {
    RenCarTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
        ) {
            RenCarBottomNavBar(
                selectedItem = RenCarBottomNavItem.Map,
                onItemSelected = {}
            )
        }
    }
}
