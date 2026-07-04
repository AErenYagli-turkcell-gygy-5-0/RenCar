package com.turkcell.rencar.presentation.screen.auth.otp

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.rencar.R
import com.turkcell.rencar.presentation.theme.RenCarPrimaryLight
import com.turkcell.rencar.presentation.theme.RenCarTheme

@Composable
fun OtpRoute(
    onNavigateBack: () -> Unit,
    onVerified: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OtpViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                OtpEffect.NavigateBack -> onNavigateBack()
                OtpEffect.VerificationCompleted -> onVerified()
            }
        }
    }

    OtpVerificationScreen(state = state, onIntent = viewModel::onIntent, modifier = modifier)
}

@Composable
fun OtpVerificationScreen(
    state: OtpState,
    onIntent: (OtpIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSystemInDarkTheme()) {
        MaterialTheme.colorScheme.background
    } else {
        MaterialTheme.colorScheme.surface
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onIntent(OtpIntent.BackClicked) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp)
                )
            }

            Box(
                modifier = Modifier
                    .padding(top = 26.dp)
                    .size(60.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_shield_check),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(30.dp)
                )
            }

            Text(
                text = stringResource(R.string.otp_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 22.dp)
            )

            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)) {
                        append(state.phoneNumber)
                    }
                    append(" ")
                    append(stringResource(R.string.otp_instruction_suffix))
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )

            Box(modifier = Modifier.padding(top = 30.dp)) {
                Row {
                    repeat(OTP_LENGTH) { index ->
                        val digit = state.digits.getOrNull(index)
                        val isActive = digit == null && index == state.digits.length

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = if (index == 0) 0.dp else 9.dp)
                                .height(62.dp)
                                .then(
                                    if (isActive) {
                                        Modifier
                                            .border(
                                                width = 2.dp,
                                                color = MaterialTheme.colorScheme.primary,
                                                shape = RoundedCornerShape(15.dp)
                                            )
                                            .background(
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                shape = RoundedCornerShape(15.dp)
                                            )
                                    } else {
                                        Modifier.border(
                                            width = 1.5.dp,
                                            color = MaterialTheme.colorScheme.outline,
                                            shape = RoundedCornerShape(15.dp)
                                        )
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                digit != null -> Text(
                                    text = digit.toString(),
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                isActive -> Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height(26.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                            }
                        }
                    }
                }

                BasicTextField(
                    value = state.digits,
                    onValueChange = { onIntent(OtpIntent.DigitsChanged(it)) },
                    enabled = !state.isVerifying,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    textStyle = MaterialTheme.typography.headlineSmall.copy(color = Color.Transparent),
                    cursorBrush = SolidColor(Color.Transparent),
                    modifier = Modifier
                        .matchParentSize()
                        .padding(horizontal = 0.dp)
                )
            }

            state.errorMessage?.let { errorMessage ->
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            Row(
                modifier = Modifier
                    .padding(top = 22.dp)
                    .clickable(
                        enabled = state.remainingSeconds == 0 && !state.isResending,
                        onClick = { onIntent(OtpIntent.ResendClicked) }
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_clock_outline),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.size(16.dp)
                )
                if (state.remainingSeconds > 0) {
                    Text(
                        text = buildAnnotatedString {
                            append(stringResource(R.string.otp_resend_prefix))
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                                val minutes = state.remainingSeconds / 60
                                val seconds = (state.remainingSeconds % 60).toString().padStart(2, '0')
                                append("$minutes:$seconds")
                            }
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(start = 7.dp)
                    )
                } else {
                    Text(
                        text = stringResource(R.string.otp_resend_action),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 7.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
                    .height(56.dp)
                    .shadow(
                        elevation = 14.dp,
                        shape = RoundedCornerShape(18.dp),
                        ambientColor = RenCarPrimaryLight,
                        spotColor = RenCarPrimaryLight
                    )
                    .clip(RoundedCornerShape(18.dp))
                    .background(RenCarPrimaryLight.copy(alpha = if (state.isVerifying) 0.55f else 1f))
                    .clickable(enabled = !state.isVerifying) { onIntent(OtpIntent.VerifyClicked) },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (state.isVerifying) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = if (state.isVerifying) {
                            stringResource(R.string.otp_verify_loading)
                        } else {
                            stringResource(R.string.otp_verify_button)
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(start = if (state.isVerifying) 8.dp else 0.dp)
                    )
                }
            }

            Text(
                text = buildAnnotatedString {
                    append(stringResource(R.string.otp_change_phone_prefix))
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)) {
                        append(stringResource(R.string.otp_change_phone_action))
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp)
                    .clickable { onIntent(OtpIntent.ChangeNumberClicked) }
            )
        }
    }
}

@Preview(name = "OTP - Light", showBackground = true)
@Composable
private fun OtpVerificationScreenLightPreview() {
    RenCarTheme(darkTheme = false) {
        OtpVerificationScreen(state = OtpState(), onIntent = {})
    }
}

@Preview(name = "OTP - Dark", showBackground = true)
@Composable
private fun OtpVerificationScreenDarkPreview() {
    RenCarTheme(darkTheme = true) {
        OtpVerificationScreen(state = OtpState(), onIntent = {})
    }
}
