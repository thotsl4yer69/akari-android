package com.akari.app.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextGeometricTransform
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/**
 * Named text styles built from the design's type scale (AkariType) with the
 * exact families, weights, tracking, and italics from the spec. Screens use
 * these instead of hand-rolling TextStyle each time.
 */
object AkariText {
    // --- serif (Newsreader) ---
    val Wordmark = TextStyle(fontFamily = Newsreader, fontWeight = FontWeight.Medium, fontSize = 44.sp)
    val MorningTitle = TextStyle(fontFamily = Newsreader, fontWeight = FontWeight.Medium, fontSize = 32.sp)
    val ScreenTitle = TextStyle(fontFamily = Newsreader, fontWeight = FontWeight.Medium, fontSize = 26.sp)
    val OnboardH2 = TextStyle(fontFamily = Newsreader, fontWeight = FontWeight.Medium, fontSize = 29.sp, lineHeight = 37.sp)
    val HomeGreeting = TextStyle(fontFamily = Newsreader, fontWeight = FontWeight.Medium, fontSize = 25.sp)
    val SheetTitle = TextStyle(fontFamily = Newsreader, fontWeight = FontWeight.Medium, fontSize = 23.sp)
    val StatFigure = TextStyle(fontFamily = Newsreader, fontWeight = FontWeight.Normal, fontSize = 30.sp)
    val LanternCount = TextStyle(fontFamily = Newsreader, fontWeight = FontWeight.Normal, fontSize = 52.sp)
    val BatteryReadout = TextStyle(fontFamily = Newsreader, fontWeight = FontWeight.Normal, fontSize = 24.sp)
    val RowNumber = TextStyle(fontFamily = Newsreader, fontWeight = FontWeight.Normal, fontSize = 20.sp)
    val InlineNumber = TextStyle(fontFamily = Newsreader, fontWeight = FontWeight.Normal, fontSize = 17.sp)

    /** The intimate voice — always italic serif. */
    fun poetic(size: Int = 16) = TextStyle(
        fontFamily = Newsreader, fontWeight = FontWeight.Normal,
        fontStyle = FontStyle.Italic, fontSize = size.sp, lineHeight = (size * 1.5f).sp,
    )

    // --- sans (Zen Kaku Gothic New) ---
    val Body = TextStyle(fontFamily = ZenKaku, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 25.sp)
    val Label = TextStyle(fontFamily = ZenKaku, fontWeight = FontWeight.Normal, fontSize = 13.sp)
    val LabelMedium = TextStyle(fontFamily = ZenKaku, fontWeight = FontWeight.Medium, fontSize = 13.sp)
    val Caption = TextStyle(fontFamily = ZenKaku, fontWeight = FontWeight.Normal, fontSize = 12.sp)
    val Button = TextStyle(fontFamily = ZenKaku, fontWeight = FontWeight.Medium, fontSize = 16.sp)

    /** UPPERCASE overline eyebrow — tracking .18–.22em, sumi3. Caller uppercases. */
    val Overline = TextStyle(
        fontFamily = ZenKaku, fontWeight = FontWeight.Normal, fontSize = 12.sp,
        letterSpacing = 0.2.em,
    )

    /** Small UPPERCASE tag, e.g. pace tags. */
    val Tag = TextStyle(
        fontFamily = ZenKaku, fontWeight = FontWeight.Medium, fontSize = 11.sp,
        letterSpacing = 0.08.em,
    )

    val TabularTime = TextStyle(
        fontFamily = ZenKaku, fontWeight = FontWeight.Normal, fontSize = 12.sp,
        textGeometricTransform = TextGeometricTransform.None,
    )
}
