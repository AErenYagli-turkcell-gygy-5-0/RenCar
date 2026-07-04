package com.turkcell.rencar.presentation.screen.auth.license

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.rencar.R
import com.turkcell.rencar.presentation.theme.RenCarPrimaryLight
import com.turkcell.rencar.presentation.theme.RenCarTheme
import com.turkcell.rencar.presentation.theme.extendedColors

@Composable
fun LicenseUploadRoute(
    onNavigateBack: () -> Unit,
    onUploadCompleted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LicenseUploadViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                LicenseUploadEffect.NavigateBack -> onNavigateBack()
                LicenseUploadEffect.UploadCompleted -> onUploadCompleted()
            }
        }
    }

    LicenseUploadScreen(state = state, onIntent = viewModel::onIntent, modifier = modifier)
}

@Composable
fun LicenseUploadScreen(
    state: LicenseUploadState,
    onIntent: (LicenseUploadIntent) -> Unit,
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
                .padding(horizontal = 22.dp, vertical = 14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onIntent(LicenseUploadIntent.BackClicked) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_back),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(modifier = Modifier.padding(start = 14.dp)) {
                    Text(
                        text = stringResource(R.string.license_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.license_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 1.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LicenseStep(number = "1", label = stringResource(R.string.license_step_license), isActive = true)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(MaterialTheme.colorScheme.outline)
                )
                LicenseStep(number = "2", label = stringResource(R.string.license_step_selfie), isActive = false)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(MaterialTheme.colorScheme.outline)
                )
                LicenseStep(number = "3", label = stringResource(R.string.license_step_approval), isActive = false)
            }

            Column(
                modifier = Modifier
                    .padding(top = 20.dp)
                    .weight(1f)
            ) {
                LicenseUploadBox(
                    label = stringResource(R.string.license_front_label),
                    isUploaded = state.isFrontUploaded,
                    placeholderText = stringResource(R.string.license_front_placeholder),
                    onClick = { onIntent(LicenseUploadIntent.FrontUploadClicked) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                LicenseUploadBox(
                    label = stringResource(R.string.license_back_label),
                    isUploaded = state.isBackUploaded,
                    placeholderText = stringResource(R.string.license_back_placeholder),
                    onClick = { onIntent(LicenseUploadIntent.BackUploadClicked) }
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(13.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_info_outline),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = buildAnnotatedString {
                            append(stringResource(R.string.license_info_prefix))
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(stringResource(R.string.license_info_emphasis))
                            }
                            append(stringResource(R.string.license_info_suffix))
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 10.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(
                        elevation = 14.dp,
                        shape = RoundedCornerShape(18.dp),
                        ambientColor = RenCarPrimaryLight,
                        spotColor = RenCarPrimaryLight
                    )
                    .clip(RoundedCornerShape(18.dp))
                    .background(RenCarPrimaryLight)
                    .clickable { onIntent(LicenseUploadIntent.ContinueClicked) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.license_continue_button),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
private fun LicenseStep(
    number: String,
    label: String,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val circleColor = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.extendedColors.disabledContainer
    val numberColor = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.extendedColors.disabledContent
    val labelColor = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.extendedColors.disabledContent

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(circleColor),
            contentAlignment = Alignment.Center
        ) {
            Text(text = number, style = MaterialTheme.typography.labelSmall, color = numberColor)
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = labelColor,
            modifier = Modifier.padding(start = 6.dp)
        )
    }
}

@Composable
private fun LicenseUploadBox(
    label: String,
    isUploaded: Boolean,
    placeholderText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val outlineColor = MaterialTheme.colorScheme.outline
    val surfaceColor = MaterialTheme.colorScheme.surface

    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(118.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(surfaceColor)
                .then(if (isUploaded) Modifier else Modifier.dashedBorder(outlineColor))
                .clickable(onClick = onClick)
        ) {
            if (isUploaded) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(MaterialTheme.extendedColors.successAccent)
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_check),
                        contentDescription = null,
                        tint = MaterialTheme.extendedColors.onSuccess,
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = stringResource(R.string.license_uploaded),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.extendedColors.onSuccess,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            } else {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_camera),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Text(
                        text = placeholderText,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 7.dp)
                    )
                }
            }
        }
    }
}

private fun Modifier.dashedBorder(
    color: Color,
    cornerRadius: Dp = 18.dp,
    strokeWidth: Dp = 2.dp
): Modifier = drawBehind {
    drawRoundRect(
        color = color,
        style = Stroke(
            width = strokeWidth.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
        ),
        cornerRadius = CornerRadius(cornerRadius.toPx())
    )
}

@Preview(name = "License Upload - Light", showBackground = true)
@Composable
private fun LicenseUploadScreenLightPreview() {
    RenCarTheme(darkTheme = false) {
        LicenseUploadScreen(state = LicenseUploadState(), onIntent = {})
    }
}

@Preview(name = "License Upload - Dark", showBackground = true)
@Composable
private fun LicenseUploadScreenDarkPreview() {
    RenCarTheme(darkTheme = true) {
        LicenseUploadScreen(state = LicenseUploadState(), onIntent = {})
    }
}
