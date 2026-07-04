package com.turkcell.rencar.presentation.screen.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.LaunchedEffect

private val IconGradientStartLight = Color(0xFF1E7FE0)
private val IconGradientStartDark = Color(0xFF3B8EF0)

@Composable
fun SplashRoute(
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                SplashEffect.NavigateToLogin -> onNavigateToLogin()
            }
        }
    }

    SplashOnboardingScreen(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier
    )
}

@Composable
fun SplashOnboardingScreen(
    state: SplashState,
    onIntent: (SplashIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 36.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.20f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0f)
                                )
                            )
                        )
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(98.dp)
                            .shadow(
                                elevation = 20.dp,
                                shape = RoundedCornerShape(30.dp),
                                ambientColor = RenCarPrimaryLight,
                                spotColor = RenCarPrimaryLight
                            )
                            .clip(RoundedCornerShape(30.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        if (isDark) IconGradientStartDark else IconGradientStartLight,
                                        RenCarPrimaryLight
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_rencar_car),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(50.dp)
                        )
                    }

                    Text(
                        text = stringResource(R.string.splash_title),
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 28.dp)
                    )

                    Text(
                        text = stringResource(R.string.splash_subtitle),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp)
                    .padding(bottom = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(modifier = Modifier.padding(bottom = 24.dp)) {
                    Box(
                        modifier = Modifier
                            .width(22.dp)
                            .height(7.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Box(
                        modifier = Modifier
                            .padding(start = 7.dp)
                            .size(7.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.outline)
                    )
                    Box(
                        modifier = Modifier
                            .padding(start = 7.dp)
                            .size(7.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.outline)
                    )
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
                        .clickable { onIntent(SplashIntent.GetStartedClicked) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.splash_cta_button),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Text(
                    text = buildAnnotatedString {
                        append(stringResource(R.string.splash_login_prompt_prefix))
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)) {
                            append(stringResource(R.string.splash_login_prompt_action))
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(top = 18.dp)
                        .clickable { onIntent(SplashIntent.LoginClicked) }
                )
            }
        }
    }
}

@Preview(name = "Splash - Light", showBackground = true)
@Composable
private fun SplashOnboardingScreenLightPreview() {
    RenCarTheme(darkTheme = false) {
        SplashOnboardingScreen(state = SplashState, onIntent = {})
    }
}

@Preview(name = "Splash - Dark", showBackground = true)
@Composable
private fun SplashOnboardingScreenDarkPreview() {
    RenCarTheme(darkTheme = true) {
        SplashOnboardingScreen(state = SplashState, onIntent = {})
    }
}
