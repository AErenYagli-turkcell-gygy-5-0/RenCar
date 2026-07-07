package com.turkcell.rencar.presentation.component.navigation

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun RenCarBottomNavIcon(
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
