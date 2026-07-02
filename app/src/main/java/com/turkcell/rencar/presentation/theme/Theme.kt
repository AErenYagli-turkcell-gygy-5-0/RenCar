package com.turkcell.rencar.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = RenCarPrimaryDark,
    onPrimary = RenCarOnPrimaryDark,
    primaryContainer = RenCarPrimaryContainerDark,
    onPrimaryContainer = RenCarOnPrimaryContainerDark,
    error = RenCarErrorDark,
    onError = RenCarOnError,
    background = RenCarBackgroundDark,
    onBackground = RenCarOnBackgroundDark,
    surface = RenCarSurfaceDark,
    onSurface = RenCarOnSurfaceDark,
    surfaceVariant = RenCarSurfaceVariantDark,
    onSurfaceVariant = RenCarOnSurfaceVariantDark,
    outline = RenCarOutlineDark,
    outlineVariant = RenCarOutlineVariantDark,
)

private val LightColorScheme = lightColorScheme(
    primary = RenCarPrimaryLight,
    onPrimary = RenCarOnPrimaryLight,
    primaryContainer = RenCarPrimaryContainerLight,
    onPrimaryContainer = RenCarOnPrimaryContainerLight,
    error = RenCarErrorLight,
    onError = RenCarOnError,
    background = RenCarBackgroundLight,
    onBackground = RenCarOnBackgroundLight,
    surface = RenCarSurfaceLight,
    onSurface = RenCarOnSurfaceLight,
    surfaceVariant = RenCarSurfaceVariantLight,
    onSurfaceVariant = RenCarOnSurfaceVariantLight,
    outline = RenCarOutlineLight,
    outlineVariant = RenCarOutlineVariantLight,
)

/**
 * Material3 ColorScheme rollerinin karşılamadığı, docs/design/00-color-system.md §4'te
 * tanımlanan başarı/uyarı/kategori/devre dışı renkleri taşır.
 */
data class RenCarExtendedColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val successAccent: Color,
    val warning: Color,
    val categoryEconomic: Color,
    val categoryPremium: Color,
    val categorySuv: Color,
    val categoryExtra: Color,
    val statusInUse: Color,
    val disabledContainer: Color,
    val disabledContent: Color,
)

private val LightExtendedColors = RenCarExtendedColors(
    success = RenCarSuccessLight,
    onSuccess = RenCarOnSuccess,
    successContainer = RenCarSuccessContainerLight,
    successAccent = RenCarSuccessAccent,
    warning = RenCarWarning,
    categoryEconomic = RenCarCategoryEconomic,
    categoryPremium = RenCarCategoryPremium,
    categorySuv = RenCarCategorySuv,
    categoryExtra = RenCarCategoryExtra,
    statusInUse = RenCarStatusInUse,
    disabledContainer = RenCarDisabledContainerLight,
    disabledContent = RenCarDisabledContentLight,
)

private val DarkExtendedColors = RenCarExtendedColors(
    success = RenCarSuccessDark,
    onSuccess = RenCarOnSuccess,
    successContainer = RenCarSuccessContainerDark,
    successAccent = RenCarSuccessAccent,
    warning = RenCarWarning,
    categoryEconomic = RenCarCategoryEconomic,
    categoryPremium = RenCarCategoryPremium,
    categorySuv = RenCarCategorySuv,
    categoryExtra = RenCarCategoryExtra,
    statusInUse = RenCarStatusInUse,
    disabledContainer = RenCarDisabledContainerDark,
    disabledContent = RenCarDisabledContentDark,
)

private val LocalRenCarExtendedColors = staticCompositionLocalOf { LightExtendedColors }

val MaterialTheme.extendedColors: RenCarExtendedColors
    @Composable
    get() = LocalRenCarExtendedColors.current

@Composable
fun RenCarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Marka kimliğini (Okyanus Mavisi) korumak için varsayılan olarak kapalıdır.
    // bkz. docs/decisions.md
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    CompositionLocalProvider(LocalRenCarExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
