package com.akari.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp

/**
 * The lantern warmth (`lanternHue`) flows from DataStore through this
 * CompositionLocal so EVERY lantern recolors — home, onboarding, and the
 * History mini-lanterns all follow (the v2 fix). Default is amber.
 */
val LocalLanternHue = staticCompositionLocalOf { AkariColors.Akari }

/** Whether the gentle, poetic voice is on (vs plain words). */
val LocalPoeticVoice = staticCompositionLocalOf { true }

// Material 3 color scheme themed entirely from the washi tokens — there is NO
// default M3 purple anywhere, and no dark scheme (crash mode is its own
// surface). Every role maps to a warm-paper token.
private val AkariColorScheme = lightColorScheme(
    primary = AkariColors.Sumi,
    onPrimary = AkariColors.Washi,
    secondary = AkariColors.Sumi2,
    onSecondary = AkariColors.Washi,
    tertiary = AkariColors.Ai,
    onTertiary = AkariColors.Washi,
    background = AkariColors.Washi,
    onBackground = AkariColors.Sumi,
    surface = AkariColors.Card,
    onSurface = AkariColors.Sumi,
    surfaceVariant = AkariColors.Washi2,
    onSurfaceVariant = AkariColors.Sumi2,
    outline = AkariColors.Line2,
    outlineVariant = AkariColors.Line,
    error = AkariColors.Clay,
    onError = Color.White,
    scrim = Color(0x572A1E0E),
)

private val defaultLineHeight = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

// Typography maps the design's roles onto M3 slots. We mostly style text
// directly per-use, but seeding the slots keeps M3 components on-brand.
private val AkariTypography = Typography(
    displayLarge = TextStyle(fontFamily = Newsreader, fontWeight = FontWeight.Medium, fontSize = 44.sp),
    headlineMedium = TextStyle(fontFamily = Newsreader, fontWeight = FontWeight.Medium, fontSize = 26.sp),
    titleLarge = TextStyle(fontFamily = Newsreader, fontWeight = FontWeight.Medium, fontSize = 23.sp),
    bodyLarge = TextStyle(fontFamily = ZenKaku, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 24.sp, lineHeightStyle = defaultLineHeight),
    bodyMedium = TextStyle(fontFamily = ZenKaku, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 22.sp, lineHeightStyle = defaultLineHeight),
    labelLarge = TextStyle(fontFamily = ZenKaku, fontWeight = FontWeight.Medium, fontSize = 15.sp),
    labelMedium = TextStyle(fontFamily = ZenKaku, fontWeight = FontWeight.Normal, fontSize = 13.sp),
    labelSmall = TextStyle(fontFamily = ZenKaku, fontWeight = FontWeight.Normal, fontSize = 12.sp),
)

@Composable
fun AkariTheme(
    lanternHue: Color = AkariColors.Akari,
    poeticVoice: Boolean = true,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalLanternHue provides lanternHue,
        LocalPoeticVoice provides poeticVoice,
    ) {
        MaterialTheme(
            colorScheme = AkariColorScheme,
            typography = AkariTypography,
            content = content,
        )
    }
}
