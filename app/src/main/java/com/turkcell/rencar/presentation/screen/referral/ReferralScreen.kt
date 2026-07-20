package com.turkcell.rencar.presentation.screen.referral

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.rencar.R
import com.turkcell.rencar.domain.wallet.WalletTransaction
import com.turkcell.rencar.presentation.theme.RenCarTheme
import com.turkcell.rencar.presentation.theme.extendedColors
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ReferralRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReferralViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.onIntent(ReferralIntent.ScreenStarted)
        viewModel.effect.collect { effect ->
            when (effect) {
                ReferralEffect.NavigateBack -> onNavigateBack()
                is ReferralEffect.ShareReferralCode -> context.shareReferralCode(effect.code)
                is ReferralEffect.CopyReferralCode -> context.copyReferralCode(effect.code)
            }
        }
    }

    ReferralScreen(state = state, onIntent = viewModel::onIntent, modifier = modifier)
}

private fun Context.shareReferralCode(code: String) {
    val shareText = getString(R.string.referral_share_message, code)
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    startActivity(Intent.createChooser(sendIntent, getString(R.string.referral_share_title)))
}

private fun Context.copyReferralCode(code: String) {
    val clipboardManager = getSystemService(ClipboardManager::class.java)
    clipboardManager?.setPrimaryClip(ClipData.newPlainText(getString(R.string.referral_code_label), code))
}

@Composable
fun ReferralScreen(
    state: ReferralState,
    onIntent: (ReferralIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            ReferralHeader(onBackClick = { onIntent(ReferralIntent.BackClicked) })

            if (state.isLoading && !state.hasLoaded) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                Spacer(modifier = Modifier.height(20.dp))
                ReferralCodeCard(
                    code = state.referralCode,
                    onShareClick = { onIntent(ReferralIntent.ShareClicked) },
                    onCopyClick = { onIntent(ReferralIntent.CopyCodeClicked) }
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.referral_history_title),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))
                if (state.earnedTransactions.isEmpty()) {
                    Text(
                        text = stringResource(R.string.referral_history_empty),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
                            .padding(horizontal = 16.dp)
                    ) {
                        state.earnedTransactions.forEachIndexed { index, transaction ->
                            EarnedBonusRow(transaction = transaction)
                            if (index != state.earnedTransactions.lastIndex) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(MaterialTheme.colorScheme.outline)
                                )
                            }
                        }
                    }
                }

                state.errorMessage?.let { message ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.referral_retry),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.clickable { onIntent(ReferralIntent.RetryClicked) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReferralHeader(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(onClick = onBackClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_back),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(22.dp)
            )
        }
        Text(
            text = stringResource(R.string.referral_title),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 14.dp)
        )
    }
}

@Composable
private fun ReferralCodeCard(
    code: String?,
    onShareClick: () -> Unit,
    onCopyClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Text(
            text = stringResource(R.string.referral_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = code ?: stringResource(R.string.referral_code_loading),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.primary
            )
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable(enabled = code != null, onClick = onCopyClick),
                contentAlignment = Alignment.Center
            ) {
                ReferralIcon(
                    type = ReferralIconType.Copy,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = if (code != null) 1f else 0.55f))
                .clickable(enabled = code != null, onClick = onShareClick),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ReferralIcon(type = ReferralIconType.Share, tint = Color.White, modifier = Modifier.size(18.dp))
            Text(
                text = stringResource(R.string.referral_share_action),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
private fun EarnedBonusRow(transaction: WalletTransaction) {
    val colors = MaterialTheme.extendedColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(colors.successContainer),
            contentAlignment = Alignment.Center
        ) {
            ReferralIcon(type = ReferralIconType.Gift, tint = colors.success, modifier = Modifier.size(18.dp))
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = transaction.description,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
        Text(
            text = "+${transaction.amount.toTryPrice()}",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = colors.success
        )
    }
}

@Composable
private fun ReferralIcon(
    type: ReferralIconType,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        when (type) {
            ReferralIconType.Share -> drawShareIcon(tint)
            ReferralIconType.Copy -> drawCopyIcon(tint)
            ReferralIconType.Gift -> drawGiftIcon(tint)
        }
    }
}

private enum class ReferralIconType {
    Share,
    Copy,
    Gift
}

private fun DrawScope.iconStroke(width: Float = 1.8.dp.toPx()) = Stroke(
    width = width,
    cap = StrokeCap.Round
)

private fun DrawScope.drawShareIcon(color: Color) {
    val stroke = iconStroke()
    val nodeRadius = size.minDimension * 0.09f
    val top = Offset(size.width * 0.78f, size.height * 0.22f)
    val bottomLeft = Offset(size.width * 0.22f, size.height * 0.5f)
    val bottomRight = Offset(size.width * 0.78f, size.height * 0.78f)
    drawLine(color, bottomLeft, top, stroke.width, StrokeCap.Round)
    drawLine(color, bottomLeft, bottomRight, stroke.width, StrokeCap.Round)
    drawCircle(color, radius = nodeRadius, center = top, style = stroke)
    drawCircle(color, radius = nodeRadius, center = bottomLeft, style = stroke)
    drawCircle(color, radius = nodeRadius, center = bottomRight, style = stroke)
}

private fun DrawScope.drawCopyIcon(color: Color) {
    val stroke = iconStroke()
    drawRoundRect(
        color,
        topLeft = Offset(size.width * 0.3f, size.height * 0.16f),
        size = Size(size.width * 0.5f, size.height * 0.5f),
        cornerRadius = CornerRadius(size.width * 0.08f),
        style = stroke
    )
    drawRoundRect(
        color,
        topLeft = Offset(size.width * 0.2f, size.height * 0.34f),
        size = Size(size.width * 0.5f, size.height * 0.5f),
        cornerRadius = CornerRadius(size.width * 0.08f),
        style = stroke
    )
}

private fun DrawScope.drawGiftIcon(color: Color) {
    val stroke = iconStroke()
    drawRoundRect(
        color,
        topLeft = Offset(size.width * 0.2f, size.height * 0.42f),
        size = Size(size.width * 0.6f, size.height * 0.42f),
        cornerRadius = CornerRadius(size.width * 0.05f),
        style = stroke
    )
    drawLine(
        color,
        Offset(size.width * 0.5f, size.height * 0.42f),
        Offset(size.width * 0.5f, size.height * 0.84f),
        stroke.width,
        StrokeCap.Round
    )
    drawLine(
        color,
        Offset(size.width * 0.2f, size.height * 0.42f),
        Offset(size.width * 0.8f, size.height * 0.42f),
        stroke.width,
        StrokeCap.Round
    )
    drawArc(
        color,
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(size.width * 0.28f, size.height * 0.2f),
        size = Size(size.width * 0.2f, size.height * 0.22f),
        style = stroke
    )
    drawArc(
        color,
        startAngle = 0f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(size.width * 0.52f, size.height * 0.2f),
        size = Size(size.width * 0.2f, size.height * 0.22f),
        style = stroke
    )
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
private fun ReferralScreenPreview() {
    RenCarTheme(darkTheme = false) {
        ReferralScreen(
            state = ReferralState(
                referralCode = "REN-K7M2XQ",
                hasLoaded = true
            ),
            onIntent = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ReferralScreenDarkPreview() {
    RenCarTheme(darkTheme = true) {
        ReferralScreen(
            state = ReferralState(
                referralCode = "REN-K7M2XQ",
                hasLoaded = true
            ),
            onIntent = {}
        )
    }
}
