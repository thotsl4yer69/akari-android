package com.akari.app.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.akari.app.R

// Newsreader is a variable font (opsz,wght); weights are driven with
// FontVariation.Settings so 400/500 both come from one TTF. Italic has its
// own variable TTF. FontVariation requires API 26+ (minSdk 28 — fine).
private fun newsreader(weight: FontWeight, italic: Boolean) = Font(
    resId = if (italic) R.font.newsreader_italic else R.font.newsreader_regular,
    weight = weight,
    style = if (italic) FontStyle.Italic else FontStyle.Normal,
    variationSettings = FontVariation.Settings(
        FontVariation.weight(weight.weight),
        FontVariation.opticalSizing(18.sp),
    ),
)

/** Serif — display, greetings, all big numbers, the poetic/italic voice. */
val Newsreader = FontFamily(
    newsreader(FontWeight.Normal, italic = false),
    newsreader(FontWeight.Medium, italic = false),
    newsreader(FontWeight.Normal, italic = true),
    newsreader(FontWeight.Medium, italic = true),
)

/** Sans — all UI: body, labels, buttons, tags, numeric data. */
val ZenKaku = FontFamily(
    Font(R.font.zenkakugothicnew_regular, FontWeight.Normal),
    Font(R.font.zenkakugothicnew_medium, FontWeight.Medium),
    Font(R.font.zenkakugothicnew_bold, FontWeight.Bold),
)
