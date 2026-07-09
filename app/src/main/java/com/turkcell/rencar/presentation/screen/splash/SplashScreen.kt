package com.turkcell.rencar.presentation.screen.splash

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.rencar.R
import com.turkcell.rencar.presentation.theme.RenCarPrimaryLight
import kotlinx.coroutines.flow.distinctUntilChanged

private val IconGradientStartLight = Color(0xFF1E7FE0)
private val IconGradientStartDark = Color(0xFF3B8EF0)

private data class OnboardingPage(
    val iconRes: Int,
    val titleRes: Int,
    val descriptionRes: Int,
    val iconContentDescriptionRes: Int
)

private val onboardingPages = listOf(
    OnboardingPage(
        iconRes = R.drawable.onboarding_find_car,
        titleRes = R.string.splash_page_find_car_title,
        descriptionRes = R.string.splash_page_find_car_description,
        iconContentDescriptionRes = R.string.splash_page_find_car_icon_description
    ),
    OnboardingPage(
        iconRes = R.drawable.onboarding_quick_rental,
        titleRes = R.string.splash_page_quick_rental_title,
        descriptionRes = R.string.splash_page_quick_rental_description,
        iconContentDescriptionRes = R.string.splash_page_quick_rental_icon_description
    ),
    OnboardingPage(
        iconRes = R.drawable.onboarding_safe_trip,
        titleRes = R.string.splash_page_safe_trip_title,
        descriptionRes = R.string.splash_page_safe_trip_description,
        iconContentDescriptionRes = R.string.splash_page_safe_trip_icon_description
    )
)

@Composable
fun SplashRoute(
    modifier: Modifier = Modifier,
    viewModel: SplashViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit,
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
    modifier: Modifier = Modifier,
    onIntent: (SplashIntent) -> Unit,
) {
    val isDark = isSystemInDarkTheme()
    val pagerState = rememberPagerState(
        initialPage = state.currentPage,
        pageCount = { SplashState.PAGE_COUNT }
    )

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { page -> onIntent(SplashIntent.PageChanged(page)) }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("splash_pager"),
                verticalAlignment = Alignment.CenterVertically
            ) { pageIndex ->
                if (pageIndex == 0) {
                    OnboardingPageBody(
                        iconRes = R.drawable.ic_rencar_car,
                        iconContentDescription = stringResource(R.string.splash_page_welcome_icon_description),
                        title = stringResource(R.string.splash_page_welcome_title),
                        description = stringResource(R.string.splash_page_welcome_tagline),
                        isDark = isDark
                    )
                } else {
                    val page = onboardingPages[pageIndex - 1]
                    OnboardingPageBody(
                        iconRes = page.iconRes,
                        iconContentDescription = stringResource(page.iconContentDescriptionRes),
                        title = stringResource(page.titleRes),
                        description = stringResource(page.descriptionRes),
                        isDark = isDark
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
                    repeat(SplashState.PAGE_COUNT) { index ->
                        val isSelected = index == state.currentPage
                        Box(
                            modifier = Modifier
                                .padding(start = if (index == 0) 0.dp else 7.dp)
                                .then(
                                    if (isSelected) {
                                        Modifier.width(22.dp).height(7.dp)
                                    } else {
                                        Modifier.size(7.dp)
                                    }
                                )
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.outline
                                    }
                                )
                                .semantics { selected = isSelected }
                                .testTag("splash_page_indicator_$index")
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

@Composable
private fun OnboardingPageBody(
    iconRes: Int,
    iconContentDescription: String,
    title: String,
    description: String,
    isDark: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
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
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = iconContentDescription,
                    modifier = Modifier.size(50.dp)
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 28.dp)
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 10.dp)
            )
        }
    }
}