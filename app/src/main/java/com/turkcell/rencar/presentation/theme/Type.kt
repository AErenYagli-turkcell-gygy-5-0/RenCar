package com.turkcell.rencar.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.turkcell.rencar.R

// bkz. docs/design/00-color-system.md §7 ve docs/decisions.md — kaynak: Rencar.html
private val fontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private val SoraGoogleFont = GoogleFont("Sora")
private val PlusJakartaSansGoogleFont = GoogleFont("Plus Jakarta Sans")

// Başlıklar — Rencar.html'de font-family:'Sora' ile açıkça işaretlenen metinler
val SoraFontFamily = FontFamily(
    Font(googleFont = SoraGoogleFont, fontProvider = fontProvider, weight = FontWeight.Medium),
    Font(googleFont = SoraGoogleFont, fontProvider = fontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = SoraGoogleFont, fontProvider = fontProvider, weight = FontWeight.Bold),
    Font(googleFont = SoraGoogleFont, fontProvider = fontProvider, weight = FontWeight.ExtraBold),
)

// Gövde/etiket metni — Rencar.html'de konteynerin varsayılan yazı tipi
val PlusJakartaSansFontFamily = FontFamily(
    Font(googleFont = PlusJakartaSansGoogleFont, fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = PlusJakartaSansGoogleFont, fontProvider = fontProvider, weight = FontWeight.Medium),
    Font(googleFont = PlusJakartaSansGoogleFont, fontProvider = fontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = PlusJakartaSansGoogleFont, fontProvider = fontProvider, weight = FontWeight.Bold),
    Font(googleFont = PlusJakartaSansGoogleFont, fontProvider = fontProvider, weight = FontWeight.ExtraBold),
)

// RenCar tipografi ölçeği — docs/design/00-color-system.md §7'deki eşleme tablosuna göre kurulmuştur.
val Typography = Typography(
    displaySmall = TextStyle(
        fontFamily = SoraFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = (-1.5).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = SoraFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-1).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = SoraFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.6).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = SoraFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = SoraFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.4).sp
    ),
    titleMedium = TextStyle(
        fontFamily = SoraFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp
    ),
    titleSmall = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)
