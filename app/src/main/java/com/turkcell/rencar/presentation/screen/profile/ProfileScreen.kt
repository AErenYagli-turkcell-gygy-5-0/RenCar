package com.turkcell.rencar.presentation.screen.profile

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.rencar.R
import com.turkcell.rencar.domain.license.LicenseReviewStatus
import com.turkcell.rencar.presentation.component.navigation.BottomNavBar
import com.turkcell.rencar.presentation.component.navigation.BottomNavItem
import com.turkcell.rencar.presentation.theme.RenCarTheme
import com.turkcell.rencar.presentation.theme.extendedColors

@Composable
fun ProfileRoute(
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onIntent(ProfileIntent.ScreenStarted)
        viewModel.effect.collect { effect ->
            when (effect) {
                ProfileEffect.NavigateToLogin -> onNavigateToLogin()
            }
        }
    }

    ProfileScreen(
        state = state,
        onIntent = viewModel::onIntent,
        onNavigateToHome = onNavigateToHome,
        modifier = modifier
    )
}

@Composable
fun ProfileScreen(
    state: ProfileState,
    onIntent: (ProfileIntent) -> Unit,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(38.dp))
            ProfileHeader(state = state)
            Spacer(modifier = Modifier.height(24.dp))

            if (state.isLoading && !state.hasLoaded) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(top = 48.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                LicenseStatusCard(status = state.licenseStatus)
                Spacer(modifier = Modifier.height(14.dp))
                ProfileMenuCard()
                Spacer(modifier = Modifier.height(14.dp))
                LogoutButton(
                    isLoading = state.isLoggingOut,
                    onClick = { onIntent(ProfileIntent.LogoutClicked) }
                )
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
                        text = stringResource(R.string.profile_retry),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.clickable { onIntent(ProfileIntent.RetryClicked) }
                    )
                }
            }
        }

        BottomNavBar(
            selectedItem = BottomNavItem.Profile,
            onItemSelected = { item ->
                if (item == BottomNavItem.Map) onNavigateToHome()
            }
        )
    }

    if (state.showLogoutConfirmation) {
        LogoutConfirmationDialog(
            onConfirm = { onIntent(ProfileIntent.LogoutConfirmed) },
            onDismiss = { onIntent(ProfileIntent.LogoutConfirmationDismissed) }
        )
    }
}

@Composable
private fun ProfileHeader(state: ProfileState) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = state.fullName.ifBlank { stringResource(R.string.profile_unknown_name) },
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = state.phone.toDisplayPhone().ifBlank { stringResource(R.string.profile_phone_missing) },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(42.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            ProfileIcon(
                type = ProfileIconType.Edit,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun LicenseStatusCard(status: LicenseReviewStatus?) {
    val colors = MaterialTheme.extendedColors
    val title = when (status) {
        LicenseReviewStatus.APPROVED -> stringResource(R.string.profile_license_verified)
        LicenseReviewStatus.UNDER_REVIEW -> stringResource(R.string.profile_license_under_review)
        LicenseReviewStatus.REJECTED -> stringResource(R.string.profile_license_rejected)
        LicenseReviewStatus.NOT_SUBMITTED,
        null -> stringResource(R.string.profile_license_not_submitted)
    }
    val subtitle = when (status) {
        LicenseReviewStatus.APPROVED -> stringResource(R.string.profile_license_approved_subtitle)
        LicenseReviewStatus.UNDER_REVIEW -> stringResource(R.string.profile_license_under_review_subtitle)
        LicenseReviewStatus.REJECTED -> stringResource(R.string.profile_license_rejected_subtitle)
        LicenseReviewStatus.NOT_SUBMITTED,
        null -> stringResource(R.string.profile_license_not_submitted_subtitle)
    }
    val badge = when (status) {
        LicenseReviewStatus.APPROVED -> stringResource(R.string.profile_license_approved_badge)
        LicenseReviewStatus.UNDER_REVIEW -> stringResource(R.string.profile_license_pending_badge)
        LicenseReviewStatus.REJECTED -> stringResource(R.string.profile_license_rejected_badge)
        LicenseReviewStatus.NOT_SUBMITTED,
        null -> stringResource(R.string.profile_license_missing_badge)
    }
    val badgeContainerColor =
        if (status == LicenseReviewStatus.APPROVED) colors.successContainer else MaterialTheme.colorScheme.outline
    val badgeContentColor =
        if (status == LicenseReviewStatus.APPROVED) colors.success else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(colors.successContainer),
            contentAlignment = Alignment.Center
        ) {
            ProfileIcon(
                type = ProfileIconType.Shield,
                tint = colors.success,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = badge,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = badgeContentColor,
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(badgeContainerColor)
                .padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun ProfileMenuCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        ProfileMenuRow(ProfileIconType.Card, stringResource(R.string.profile_payment_methods), showDivider = true)
        ProfileMenuRow(ProfileIconType.Settings, stringResource(R.string.profile_settings), showDivider = true)
        ProfileMenuRow(ProfileIconType.Help, stringResource(R.string.profile_help_support), showDivider = true)
        ProfileMenuRow(ProfileIconType.Invite, stringResource(R.string.profile_invite_earn), showDivider = false)
    }
}

@Composable
private fun ProfileMenuRow(
    iconType: ProfileIconType,
    label: String,
    showDivider: Boolean
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileIcon(
                type = iconType,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            ProfileIcon(
                type = ProfileIconType.Chevron,
                tint = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.size(16.dp)
            )
        }
        if (showDivider) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outline)
            )
        }
    }
}

@Composable
private fun LogoutButton(
    isLoading: Boolean,
    onClick: () -> Unit
) {
    val errorColor = MaterialTheme.colorScheme.error
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .clickable(enabled = !isLoading) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = errorColor,
                    strokeWidth = 2.dp
                )
            } else {
                ProfileIcon(
                    type = ProfileIconType.Logout,
                    tint = errorColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isLoading) stringResource(R.string.profile_logout_loading) else stringResource(R.string.profile_logout),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = errorColor
            )
        }
    }
}

@Composable
private fun LogoutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.profile_logout_confirm_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Text(
                text = stringResource(R.string.profile_logout_confirm_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.profile_logout_confirm_action),
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.profile_logout_cancel_action))
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun ProfileIcon(
    type: ProfileIconType,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        when (type) {
            ProfileIconType.Shield -> drawShieldIcon(tint)
            ProfileIconType.Edit -> drawEditIcon(tint)
            ProfileIconType.Card -> drawCardIcon(tint)
            ProfileIconType.Settings -> drawSettingsIcon(tint)
            ProfileIconType.Help -> drawHelpIcon(tint)
            ProfileIconType.Invite -> drawInviteIcon(tint)
            ProfileIconType.Chevron -> drawChevronIcon(tint)
            ProfileIconType.Logout -> drawLogoutIcon(tint)
        }
    }
}

private enum class ProfileIconType {
    Shield,
    Edit,
    Card,
    Settings,
    Help,
    Invite,
    Chevron,
    Logout
}

private fun String.toDisplayPhone(): String {
    val digits = filter(Char::isDigit)
    if (digits.isBlank()) return ""
    val localDigits = digits.removePrefix("90")
    return if (localDigits.length == 10) {
        "+90 ${localDigits.substring(0, 3)} ${localDigits.substring(3, 6)} ${localDigits.substring(6, 8)} ${localDigits.substring(8)}"
    } else {
        this
    }
}

private fun DrawScope.iconStroke(width: Float = 1.8.dp.toPx()) = Stroke(
    width = width,
    cap = StrokeCap.Round
)

private fun DrawScope.drawShieldIcon(color: Color) {
    val stroke = iconStroke()
    val path = Path().apply {
        moveTo(size.width * 0.5f, size.height * 0.08f)
        lineTo(size.width * 0.82f, size.height * 0.2f)
        lineTo(size.width * 0.76f, size.height * 0.58f)
        quadraticTo(size.width * 0.5f, size.height * 0.9f, size.width * 0.24f, size.height * 0.58f)
        lineTo(size.width * 0.18f, size.height * 0.2f)
        close()
    }
    drawPath(path, color, style = stroke)
    drawLine(color, Offset(size.width * 0.36f, size.height * 0.5f), Offset(size.width * 0.47f, size.height * 0.62f), stroke.width, StrokeCap.Round)
    drawLine(color, Offset(size.width * 0.47f, size.height * 0.62f), Offset(size.width * 0.66f, size.height * 0.38f), stroke.width, StrokeCap.Round)
}

private fun DrawScope.drawEditIcon(color: Color) {
    val stroke = iconStroke()
    drawLine(color, Offset(size.width * 0.28f, size.height * 0.72f), Offset(size.width * 0.72f, size.height * 0.28f), stroke.width, StrokeCap.Round)
    drawLine(color, Offset(size.width * 0.62f, size.height * 0.22f), Offset(size.width * 0.78f, size.height * 0.38f), stroke.width, StrokeCap.Round)
    drawLine(color, Offset(size.width * 0.24f, size.height * 0.78f), Offset(size.width * 0.4f, size.height * 0.74f), stroke.width, StrokeCap.Round)
}

private fun DrawScope.drawCardIcon(color: Color) {
    val stroke = iconStroke()
    drawRoundRect(color, topLeft = Offset(size.width * 0.14f, size.height * 0.24f), size = Size(size.width * 0.72f, size.height * 0.52f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.width * 0.08f), style = stroke)
    drawLine(color, Offset(size.width * 0.18f, size.height * 0.42f), Offset(size.width * 0.82f, size.height * 0.42f), stroke.width, StrokeCap.Round)
}

private fun DrawScope.drawSettingsIcon(color: Color) {
    val stroke = iconStroke()
    drawCircle(color, radius = size.minDimension * 0.18f, center = Offset(size.width * 0.5f, size.height * 0.5f), style = stroke)
    repeat(8) { index ->
        val angle = Math.toRadians((index * 45).toDouble())
        val start = Offset(
            x = size.width * 0.5f + kotlin.math.cos(angle).toFloat() * size.minDimension * 0.32f,
            y = size.height * 0.5f + kotlin.math.sin(angle).toFloat() * size.minDimension * 0.32f
        )
        val end = Offset(
            x = size.width * 0.5f + kotlin.math.cos(angle).toFloat() * size.minDimension * 0.42f,
            y = size.height * 0.5f + kotlin.math.sin(angle).toFloat() * size.minDimension * 0.42f
        )
        drawLine(color, start, end, stroke.width, StrokeCap.Round)
    }
}

private fun DrawScope.drawHelpIcon(color: Color) {
    val stroke = iconStroke()
    drawCircle(color, radius = size.minDimension * 0.38f, center = Offset(size.width * 0.5f, size.height * 0.5f), style = stroke)
    drawArc(color, startAngle = 205f, sweepAngle = 230f, useCenter = false, topLeft = Offset(size.width * 0.34f, size.height * 0.24f), size = Size(size.width * 0.32f, size.height * 0.34f), style = stroke)
    drawCircle(color, radius = size.minDimension * 0.035f, center = Offset(size.width * 0.5f, size.height * 0.72f))
}

private fun DrawScope.drawInviteIcon(color: Color) {
    val stroke = iconStroke()
    drawArc(color, startAngle = 120f, sweepAngle = 300f, useCenter = false, topLeft = Offset(size.width * 0.2f, size.height * 0.2f), size = Size(size.width * 0.56f, size.height * 0.56f), style = stroke)
    drawLine(color, Offset(size.width * 0.2f, size.height * 0.5f), Offset(size.width * 0.36f, size.height * 0.34f), stroke.width, StrokeCap.Round)
    drawLine(color, Offset(size.width * 0.2f, size.height * 0.5f), Offset(size.width * 0.36f, size.height * 0.66f), stroke.width, StrokeCap.Round)
}

private fun DrawScope.drawChevronIcon(color: Color) {
    val stroke = iconStroke()
    drawLine(color, Offset(size.width * 0.36f, size.height * 0.22f), Offset(size.width * 0.64f, size.height * 0.5f), stroke.width, StrokeCap.Round)
    drawLine(color, Offset(size.width * 0.64f, size.height * 0.5f), Offset(size.width * 0.36f, size.height * 0.78f), stroke.width, StrokeCap.Round)
}

private fun DrawScope.drawLogoutIcon(color: Color) {
    val stroke = iconStroke()
    drawLine(color, Offset(size.width * 0.52f, size.height * 0.5f), Offset(size.width * 0.18f, size.height * 0.5f), stroke.width, StrokeCap.Round)
    drawLine(color, Offset(size.width * 0.18f, size.height * 0.5f), Offset(size.width * 0.32f, size.height * 0.36f), stroke.width, StrokeCap.Round)
    drawLine(color, Offset(size.width * 0.18f, size.height * 0.5f), Offset(size.width * 0.32f, size.height * 0.64f), stroke.width, StrokeCap.Round)
    drawArc(color, startAngle = 110f, sweepAngle = 290f, useCenter = false, topLeft = Offset(size.width * 0.36f, size.height * 0.22f), size = Size(size.width * 0.46f, size.height * 0.56f), style = stroke)
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    RenCarTheme(darkTheme = false) {
        ProfileScreen(
            state = ProfileState(
                fullName = "Deniz Yilmaz",
                phone = "+905320000000",
                licenseStatus = LicenseReviewStatus.APPROVED,
                hasLoaded = true
            ),
            onIntent = {},
            onNavigateToHome = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenDarkPreview() {
    RenCarTheme(darkTheme = true) {
        ProfileScreen(
            state = ProfileState(
                fullName = "Deniz Yilmaz",
                phone = "+905320000000",
                licenseStatus = LicenseReviewStatus.APPROVED,
                hasLoaded = true
            ),
            onIntent = {},
            onNavigateToHome = {}
        )
    }
}
