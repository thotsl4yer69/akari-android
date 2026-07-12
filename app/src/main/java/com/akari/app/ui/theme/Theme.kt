// Akari — Compose theme starter
// Transcribed 1:1 from the design prototype (Akari.dc.html, the `--*` custom
// properties on the root div + inline styles). Treat as source of truth for
// color/type/shape/motion. Do NOT eyeball values from screenshots.
package com.akari.app.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ---------------------------------------------------------------- colors ---
object AkariColors {
    // Paper + ink (light, warm; the app has NO dark theme — crash mode is the
    // only dark surface and is its own palette below)
    val Washi  = Color(0xFFF1E7D4)   // app background
    val Washi2 = Color(0xFFE9DCC4)   // recessed fills (track, meter bg)
    val Card   = Color(0xFFF7F0E2)   // card surface
    val Sumi   = Color(0xFF2D2A22)   // primary ink + primary button bg
    val Sumi2  = Color(0xFF6D6353)   // secondary ink
    val Sumi3  = Color(0xFF877A62)   // tertiary ink / overlines
    val Line   = Color(0x1A2D2A22)   // hairline (sumi @ 10%)
    val Line2  = Color(0x2B2D2A22)   // stronger hairline (sumi @ 17%)

    // Accents
    val Akari  = Color(0xFFEBA950)   // lantern glow (default hue)
    val Ember  = Color(0xFFD9793F)   // flame core
    val Ai     = Color(0xFF45688A)   // indigo — cognitive / links / info
    val Sage   = Color(0xFF7E9A6E)   // ok / within pace / green zone
    val Clay   = Color(0xFFC4745A)   // warning / PEM / red zone (never pure red)
    val Plum   = Color(0xFF93627E)   // emotional dimension

    // Lantern warmth options (user-pickable; recolors EVERY lantern globally)
    val LanternHues = listOf(
        Color(0xFFEBA950),           // amber (default)
        Color(0xFFE7B85C),           // gold
        Color(0xFFE0896B),           // persimmon
        Color(0xFFB9C4D0),           // moonlight
    )
}

// Crash mode is a separate, near-black warm palette (radial bg, center → edge)
object CrashColors {
    val BgTop   = Color(0xFF241B14)
    val BgMid   = Color(0xFF140F0B)
    val BgDeep  = Color(0xFF0D0A07)
    val Text    = Color(0xFFE8D9C4)
    val TextDim = Color(0xFF9A8B78)
    val TileBg  = Color(0x0DE8D3C4)  // rgba(232,211,196,.05)
    val TileBd  = Color(0x24E8D3C4)  // rgba(232,211,196,.14)
    // done states (bg / border)
    val RestOn   = Color(0x2E7E9A6E); val RestOnBd = Color(0x807E9A6E)
    val PemOn    = Color(0x33C4745A); val PemOnBd  = Color(0x8CC4745A)
    val MedsOn   = Color(0x3345688A); val MedsOnBd = Color(0x8045688A)
}

// ------------------------------------------------------------------ type ---
// Serif: Newsreader (400/500 + italic) — display, greetings, all big numbers,
//        poetic lines (always italic).
// Sans:  Zen Kaku Gothic New (400/500/700) — body, labels, buttons, tags.
// Bundle TTFs in res/font/ (offline app — no downloadable fonts).
object AkariType {
    // serif roles                      size          weight  notes
    val LanternCount = 52.sp         // 500           home "light left" figure
    val OnboardTitle = 44.sp         // 500           "Akari" welcome only
    val MorningTitle = 32.sp         // 500
    val StatSerif    = 30.sp         // 400           trends stat figures
    val ScreenTitle  = 26.sp         // 500           Trends/History/Settings/HC
    val HomeGreeting = 25.sp         // 500
    val BatterySerif = 24.sp         // 400           slider readout
    val SheetTitle   = 23.sp         // 500           log sheets + crash h2
    val RowNumber    = 20.sp         // 400           history remaining-light
    val InlineNumber = 17.sp         // 400           "spent today" figure
    val Poetic       = 16.sp         // 400 italic    16–19sp, lh 1.5

    // sans roles
    val Body      = 15.sp            // 400, lineHeight 1.6–1.72
    val Label     = 13.sp            // 400–500
    val Caption   = 12.sp            // 400
    val Overline  = 12.sp            // 400, tracking .14–.22em, UPPERCASE
    val Tag       = 11.sp            // 500, tracking .08em, UPPERCASE
    val ButtonText= 15.sp            // 500
}

// ---------------------------------------------------------------- shapes ---
object AkariShapes {
    val Chip      = 11.dp            // settings icon squares
    val Control   = 13.dp            // severity dots, small inputs
    val Pill      = 14.dp            // HR chip
    val Card      = 16.dp            // buttons, zone cards, rows
    val CardLarge = 18.dp            // trends/battery cards
    val CrashTile = 22.dp
    val Badge     = 20.dp            // PEM pill (fully rounded)
}

// ------------------------------------------------------------------ dims ---
object AkariDims {
    val ButtonHeight   = 56.dp       // 56–58; never shorter
    val CrashTile      = 128.dp      // crash targets are HUGE on purpose
    val MinTouch       = 48.dp       // Android floor; design uses ≥44 visual
    val ScreenHPad     = 22.dp       // 26 on onboarding/morning
    val CardPad        = 16.dp       // 15–18
    val RowGap         = 10.dp
    val SectionGap     = 16.dp
}

// ---------------------------------------------------------------- motion ---
// Calm, slow, never springy. Respect "reduce motion" (see QA_CHECKLIST).
object AkariMotion {
    const val SoftInMs   = 500       // screen enter: fade + ~12dp rise, ease
    const val RiseMs     = 300       // inline expands (history day detail)
    const val StateMs    = 300       // color/size state transitions
    const val EmberMs    = 5000      // crash ember pulse, infinite, subtle
    const val PressScale = 0.98f     // buttons; tiles .97, swatches .92
    // No overshoot/bounce anywhere. One animation at a time.
}
