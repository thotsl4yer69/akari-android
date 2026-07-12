package com.akari.app.ui.components

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

/**
 * App-wide reduce-motion signal. Provided once at the app root from
 * [rememberMotionEnabled]; every animated component (Lantern, pressScale,
 * AkariSwitch, screen transitions) reads this by default so reduce-motion is
 * honored everywhere without threading a flag through every call site.
 */
val LocalMotionEnabled = staticCompositionLocalOf { true }

/**
 * True when the system allows animation. When the user has set
 * `Settings.Global.ANIMATOR_DURATION_SCALE = 0` (or reduce-motion), all
 * breathe/flicker/ember/rise animations drop to instant — nothing flashes.
 * (QA_CHECKLIST non-negotiable.)
 */
@Composable
fun rememberMotionEnabled(): Boolean {
    val context = LocalContext.current
    return remember {
        val scale = Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        )
        scale != 0f
    }
}
