package com.turkcell.rencar.presentation.component.dialog

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

enum class ConfirmDialogTone {
    Destructive,
    Neutral
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    dismissText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    tone: ConfirmDialogTone = ConfirmDialogTone.Destructive,
    isConfirmLoading: Boolean = false,
    isConfirmEnabled: Boolean = true,
    extraContent: (@Composable () -> Unit)? = null
) {
    val accentColor = when (tone) {
        ConfirmDialogTone.Destructive -> MaterialTheme.colorScheme.error
        ConfirmDialogTone.Neutral -> MaterialTheme.colorScheme.primary
    }
    val accentOnColor = when (tone) {
        ConfirmDialogTone.Destructive -> MaterialTheme.colorScheme.onError
        ConfirmDialogTone.Neutral -> MaterialTheme.colorScheme.onPrimary
    }

    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        icon = {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(color = accentColor.copy(alpha = 0.12f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(22.dp)) {
                    drawAlertIcon(accentColor)
                }
            }
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                extraContent?.let { content ->
                    Box(modifier = Modifier.padding(top = 8.dp)) { content() }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = isConfirmEnabled && !isConfirmLoading,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = accentOnColor)
            ) {
                if (isConfirmLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = accentOnColor,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(text = confirmText, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isConfirmLoading) {
                Text(text = dismissText)
            }
        }
    )
}

private fun DrawScope.drawAlertIcon(color: Color) {
    val centerX = size.width / 2f
    drawLine(
        color = color,
        start = Offset(centerX, size.height * 0.12f),
        end = Offset(centerX, size.height * 0.62f),
        strokeWidth = 2.2.dp.toPx(),
        cap = StrokeCap.Round
    )
    drawCircle(
        color = color,
        radius = size.minDimension * 0.07f,
        center = Offset(centerX, size.height * 0.86f)
    )
}
