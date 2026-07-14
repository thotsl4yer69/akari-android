package com.akari.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akari.app.domain.PacingEngine
import com.akari.app.domain.SleepQuality
import com.akari.app.domain.Zone
import com.akari.app.ui.AppViewModel
import com.akari.app.ui.UiState
import com.akari.app.ui.components.Eyebrow
import com.akari.app.ui.components.clickableRole
import com.akari.app.ui.components.pressScale
import com.akari.app.ui.theme.AkariColors
import com.akari.app.ui.theme.AkariText
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun MorningScreen(vm: AppViewModel, state: UiState, motion: Boolean) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 26.dp).padding(top = 16.dp, bottom = 30.dp),
    ) {
        Eyebrow(com.akari.app.data.DiaryDate.longLabel(LocalDate.now()))
        Spacer(Modifier.height(4.dp))
        Text(greeting(state.name), style = AkariText.MorningTitle, color = AkariColors.Sumi)
        Spacer(Modifier.height(6.dp))
        Text(
            "How full is the lantern right now? Be honest — a low day is not a failure.",
            style = AkariText.poetic(17), color = AkariColors.Sumi2,
        )
        Spacer(Modifier.height(28.dp))

        // traffic light
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ZoneCard("Steady", AkariColors.Sage, state.zone == Zone.GREEN, Modifier.weight(1f)) { vm.pickZone(Zone.GREEN) }
            ZoneCard("Careful", AkariColors.Akari, state.zone == Zone.AMBER, Modifier.weight(1f)) { vm.pickZone(Zone.AMBER) }
            ZoneCard("Low", AkariColors.Clay, state.zone == Zone.RED, Modifier.weight(1f)) { vm.pickZone(Zone.RED) }
        }
        Spacer(Modifier.height(22.dp))

        // fine-tune slider
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                .background(AkariColors.Card).border(1.dp, AkariColors.Line, RoundedCornerShape(18.dp))
                .padding(horizontal = 20.dp, vertical = 22.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Text("Fine-tune the battery", style = AkariText.Label, color = AkariColors.Sumi2)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("${state.battery}", style = AkariText.BatteryReadout, color = AkariColors.Sumi)
                    Text(" / 100", style = AkariText.Label, color = AkariColors.Sumi3)
                }
            }
            Spacer(Modifier.height(16.dp))
            BatterySlider(state.battery, zoneColor(state.zone)) { vm.setBattery(it) }
        }
        Spacer(Modifier.height(26.dp))

        Text("How did you sleep?", style = AkariText.Label, color = AkariColors.Sumi2)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            SleepQuality.entries.forEach { sq ->
                SleepPill(sq, state.sleep == sq, Modifier.weight(1f)) { vm.pickSleep(sq) }
            }
        }
        Spacer(Modifier.height(34.dp))

        DarkButton("Light the lantern", motion, onClick = { vm.lightLantern() })
    }
}

@Composable
private fun ZoneCard(label: String, dot: Color, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val tint = dot.copy(alpha = 0.14f)
    Column(
        modifier
            .pressScale(0.98f)
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) tint else AkariColors.Card)
            .border(1.5.dp, if (selected) dot else AkariColors.Line, RoundedCornerShape(16.dp))
            .clickableRole { onClick() }
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Box(
            Modifier.size(26.dp).clip(CircleShape).background(dot)
                .then(if (selected) Modifier.border(4.dp, dot.copy(alpha = 0.22f), CircleShape) else Modifier),
        )
        Text(label, style = AkariText.LabelMedium, color = AkariColors.Sumi)
    }
}

@Composable
private fun BatterySlider(battery: Int, thumbColor: Color, onChange: (Int) -> Unit) {
    var trackWidthPx by remember { mutableFloatStateOf(1f) }
    val density = LocalDensity.current
    val thumb = 24.dp

    Box(
        Modifier.fillMaxWidth().height(48.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = "Fine-tune the battery"
                progressBarRangeInfo = ProgressBarRangeInfo(battery.toFloat(), 0f..100f)
                setProgress { target ->
                    onChange(target.toInt().coerceIn(0, 100))
                    true
                }
            }
            .pointerInput(Unit) {
                trackWidthPx = size.width.toFloat()
                detectTapGestures { offset -> onChange(((offset.x / size.width) * 100f).toInt()) }
            }
            .pointerInput(Unit) {
                trackWidthPx = size.width.toFloat()
                detectDragGestures { change, _ ->
                    onChange(((change.position.x / size.width) * 100f).toInt())
                }
            },
        contentAlignment = Alignment.CenterStart,
    ) {
        // base track (dim gradient)
        Box(
            Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(5.dp))
                .background(
                    Brush.horizontalGradient(listOf(AkariColors.Clay, AkariColors.Akari, AkariColors.Sage)),
                    alpha = 0.32f,
                ),
        )
        // filled portion
        Box(
            Modifier.fillMaxWidth(battery / 100f).height(8.dp).clip(RoundedCornerShape(5.dp))
                .background(Brush.horizontalGradient(listOf(AkariColors.Clay, AkariColors.Akari, AkariColors.Sage))),
        )
        // thumb — clamped fully inside the track at 0 and 100
        val thumbPx = with(density) { thumb.toPx() }
        val travel = (trackWidthPx - thumbPx).coerceAtLeast(0f)
        val xPx = travel * (battery / 100f)
        Box(
            Modifier.offset { androidx.compose.ui.unit.IntOffset(xPx.toInt(), 0) }
                .size(thumb).clip(CircleShape).background(AkariColors.Washi)
                .border(2.dp, thumbColor, CircleShape),
        )
    }
}

@Composable
private fun SleepPill(sq: SleepQuality, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) AkariColors.Sumi else AkariColors.Card)
            .border(1.5.dp, if (selected) AkariColors.Sumi else AkariColors.Line2, RoundedCornerShape(14.dp))
            .clickableRole { onClick() }
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(sq.label, style = AkariText.LabelMedium, color = if (selected) AkariColors.Washi else AkariColors.Sumi2)
    }
}

private fun greeting(name: String): String {
    val h = LocalTime.now().hour
    val base = if (h < 12) "Good morning" else if (h < 18) "Good afternoon" else "Good evening"
    return if (name.isNotBlank()) "$base, $name" else base
}

internal fun zoneColor(z: Zone): Color = when (z) {
    Zone.GREEN -> AkariColors.Sage
    Zone.AMBER -> AkariColors.Akari
    Zone.RED -> AkariColors.Clay
}
