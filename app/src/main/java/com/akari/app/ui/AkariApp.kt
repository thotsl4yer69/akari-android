package com.akari.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.akari.app.ui.components.PathGlyph
import com.akari.app.ui.components.clickableRole
import com.akari.app.ui.components.pressScale
import com.akari.app.ui.components.rememberMotionEnabled
import com.akari.app.ui.screens.CrashScreen
import com.akari.app.ui.screens.HealthConnectScreen
import com.akari.app.ui.screens.HistoryScreen
import com.akari.app.ui.screens.HomeScreen
import com.akari.app.ui.screens.LogSheet
import com.akari.app.ui.screens.MorningScreen
import com.akari.app.ui.screens.OnboardingScreen
import com.akari.app.ui.screens.SettingsScreen
import com.akari.app.ui.screens.TrendsScreen
import com.akari.app.ui.theme.AkariColors
import com.akari.app.ui.theme.AkariText

@Composable
fun AkariApp(vm: AppViewModel, state: UiState) {
    val motion = rememberMotionEnabled()
    val showTabs = state.sheet == Sheet.None &&
        state.screen in setOf(Screen.Home, Screen.Trends, Screen.History, Screen.Settings)

    Box(Modifier.fillMaxSize().background(AkariColors.Washi)) {
        Column(Modifier.fillMaxSize()) {
            // status-bar inset spacer
            Box(Modifier.fillMaxWidth().padding(WindowInsets.statusBars.asPaddingValues()))

            Box(Modifier.weight(1f).fillMaxWidth()) {
                when (state.screen) {
                    Screen.Onboarding -> OnboardingScreen(vm, state, motion)
                    Screen.Morning -> MorningScreen(vm, state, motion)
                    Screen.Home -> HomeScreen(vm, state, motion)
                    Screen.Trends -> TrendsScreen(vm, state, motion)
                    Screen.History -> HistoryScreen(vm, state, motion)
                    Screen.Settings -> SettingsScreen(vm, state, motion)
                    Screen.HealthConnect -> HealthConnectScreen(vm, state)
                    Screen.Crash -> Unit // rendered as a full overlay below
                }
            }

            if (showTabs) TabBar(state.screen, motion, vm)
        }

        // Log sheet overlay
        if (state.sheet != Sheet.None && state.screen != Screen.Crash) {
            LogSheet(vm, state, motion)
        }

        // Crash mode covers everything, including the status bar.
        if (state.screen == Screen.Crash) {
            CrashScreen(vm, state, motion)
        }

        // Toast
        AnimatedVisibility(
            visible = state.toast != null,
            enter = fadeIn() + slideInVertically { it / 4 },
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 104.dp),
        ) {
            Box(
                Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(AkariColors.Sumi)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            ) {
                Text(state.toast ?: "", style = AkariText.Label, color = AkariColors.Washi)
            }
        }
    }
}

@Composable
private fun TabBar(screen: Screen, motion: Boolean, vm: AppViewModel) {
    val navPad = WindowInsets.navigationBars.asPaddingValues()
    Column(
        Modifier
            .fillMaxWidth()
            .background(AkariColors.Card),
    ) {
        Box(Modifier.fillMaxWidth().size(1.dp).background(AkariColors.Line))
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, start = 18.dp, end = 18.dp)
                .padding(bottom = 6.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Tab("Today", "M15.4 12a3.4 3.4 0 1 1-6.8 0 3.4 3.4 0 0 1 6.8 0z M12 3.5v2M12 18.5v2M4.7 12h-2M21.3 12h-2M6.6 6.6 5.2 5.2M18.8 18.8l-1.4-1.4M17.4 6.6l1.4-1.4M5.2 18.8l1.4-1.4", screen == Screen.Home, Modifier.weight(1f)) { vm.go(Screen.Home) }
            Tab("Trends", "M4 18l5-6 4 3 6-8M4 20h16", screen == Screen.Trends, Modifier.weight(1f)) { vm.go(Screen.Trends) }
            // center raised FAB
            Box(Modifier.width(60.dp), contentAlignment = Alignment.TopCenter) {
                Box(
                    Modifier
                        .offset(y = (-16).dp)
                        .size(54.dp)
                        .pressScale(0.9f, motion)
                        .clip(CircleShape)
                        .background(AkariColors.Sumi)
                        .clickableRole(onClick = { vm.openLog() }),
                    contentAlignment = Alignment.Center,
                ) {
                    PathGlyph("M12 5v14M5 12h14", 24.dp, AkariColors.Washi, strokeWidth = 2.dp)
                }
            }
            Tab("History", "M12 3a9 9 0 1 0 0 18 9 9 0 0 0 0-18z M12 7v5l3 2", screen == Screen.History, Modifier.weight(1f)) { vm.go(Screen.History) }
            Tab("Settings", "M5 7h9M18 7h1M5 12h1M10 12h9M5 17h6M15 17h4M13 5v4M7 10v4M13 15v4", screen == Screen.Settings, Modifier.weight(1f)) { vm.go(Screen.Settings) }
        }
        Box(Modifier.fillMaxWidth().padding(navPad))
    }
}

@Composable
private fun Tab(
    label: String,
    iconPath: String,
    active: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val color = if (active) AkariColors.Sumi else AkariColors.Sumi3
    Column(
        modifier.clickableRole(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        PathGlyph(iconPath, 22.dp, color, strokeWidth = 1.7.dp)
        Text(label, style = AkariText.Caption.copy(textAlign = TextAlign.Center), color = color)
    }
}
