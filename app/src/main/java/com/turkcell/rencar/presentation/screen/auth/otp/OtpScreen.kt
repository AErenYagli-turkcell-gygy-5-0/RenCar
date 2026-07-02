package com.turkcell.rencar.presentation.screen.auth.otp

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
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
                text = "Telefonunu doğrula",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 22.dp)
            )

            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)) {
                        append(state.phoneNumber)
                    }
                    append(" numarasına gönderdiğimiz 6 haneli kodu gir.")
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )

            Row(modifier = Modifier.padding(top = 30.dp)) {
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

            Row(
                modifier = Modifier.padding(top = 22.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_clock_outline),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = buildAnnotatedString {
                        append("Kodu tekrar gönder · ")
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
                    .background(RenCarPrimaryLight)
                    .clickable { onIntent(OtpIntent.VerifyClicked) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Doğrula ve Devam Et",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Text(
                text = buildAnnotatedString {
                    append("Numara yanlış mı? ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)) {
                        append("Değiştir")
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
