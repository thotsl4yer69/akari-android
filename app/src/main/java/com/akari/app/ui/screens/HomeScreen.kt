package com.akari.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akari.app.data.DiaryDate
import com.akari.app.domain.DiaryEntry
import com.akari.app.domain.EntryKind
import com.akari.app.domain.HrState
import com.akari.app.domain.PacingEngine
import com.akari.app.ui.AppViewModel
import com.akari.app.ui.Screen
import com.akari.app.ui.UiState
import com.akari.app.ui.components.Eyebrow
import com.akari.app.ui.components.Lantern
import com.akari.app.ui.components.LanternSize
import com.akari.app.ui.components.PathGlyph
import com.akari.app.ui.components.clickableRole
import com.akari.app.ui.components.pressScale
import com.akari.app.ui.theme.AkariColors
import com.akari.app.ui.theme.AkariText
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun HomeScreen(vm: AppViewModel, state: UiState, motion: Boolean) {
    // Poll HR while Home is resumed (only meaningful when Health Connect is on).
    if (state.hcConnected) {
        LaunchedEffect(Unit) {
            while (true) {
                vm.refreshHeartRate()
                kotlinx.coroutines.delay(15_000)
            }
        }
    }

    val energy = state.energy
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp).padding(top = 14.dp, bottom = 22.dp),
    ) {
        // header
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column {
                Eyebrow(DiaryDate.longLabel(LocalDate.now()))
                Text(homeGreeting(state.name), style = AkariText.HomeGreeting, color = AkariColors.Sumi)
            }
            Box(
                Modifier.size(40.dp).clip(CircleShape).background(AkariColors.Card)
                    .border(1.dp, AkariColors.Line2, CircleShape)
                    .clickableRole(onClick = { vm.enterCrash() }),
                contentAlignment = Alignment.Center,
            ) {
                PathGlyph("M21 12.8A8.5 8.5 0 1 1 11.2 3a6.7 6.7 0 0 0 9.8 9.8Z", 18.dp, AkariColors.Sumi2, filled = true)
            }
        }
        Spacer(Modifier.height(6.dp))

        // lantern hero
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Lantern(energy.toFloat(), state.startBattery.toFloat(), size = LanternSize.Hero, motion = motion)
        }
        Spacer(Modifier.height(4.dp))

        // mood + figure
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(PacingEngine.moodLine(energy, state.poetic), style = AkariText.poetic(16), color = AkariColors.Sumi2)
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                Text("$energy", style = AkariText.LanternCount, color = AkariColors.Sumi)
                Text(
                    "of ${state.startBattery}\nLIGHT LEFT",
                    style = AkariText.Caption.copy(letterSpacing = androidx.compose.ui.unit.TextUnit(0.14f, androidx.compose.ui.unit.TextUnitType.Em)),
                    color = AkariColors.Sumi3,
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        // HR chip when connected; otherwise the honest dashed no-wearable row
        if (state.hcConnected) HrChip(state) else NoWearableRow(vm, motion)
        Spacer(Modifier.height(12.dp))

        SpentTodayCard(state)
        Spacer(Modifier.height(18.dp))

        if (state.pem) {
            PemBanner(state.pemTime)
            Spacer(Modifier.height(18.dp))
        }

        // Today section
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Eyebrow("Today")
            Row(
                Modifier.clip(RoundedCornerShape(20.dp))
                    .border(1.dp, AkariColors.Line2, RoundedCornerShape(20.dp))
                    .clickableRole(onClick = { vm.flagPem() })
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically,
            ) {
                val c = if (state.pem) AkariColors.Clay else AkariColors.Sumi2
                Text("⚑", fontSize = 12.sp, color = c)
                Text(if (state.pem) "PEM flagged" else "Flag PEM", style = AkariText.LabelMedium, color = c)
            }
        }
        Spacer(Modifier.height(10.dp))

        Column(Modifier.fillMaxWidth()) {
            state.entries.reversed().forEach { ev -> TimelineRow(ev) }
        }
        Spacer(Modifier.height(20.dp))
        Text(
            if (state.poetic) "Rest is not idleness. Savasana counts." else "Rest is part of the day.",
            style = AkariText.poetic(14), color = AkariColors.Sumi3,
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun HrChip(state: UiState) {
    val hr = state.hr
    val hrState = hr?.let { PacingEngine.hrState(it, state.restingHr) }
    val over = hrState == HrState.ABOVE
    val tagColor = when (hrState) {
        HrState.ABOVE -> AkariColors.Clay; HrState.NEAR -> AkariColors.Akari
        HrState.WITHIN -> AkariColors.Sage; null -> AkariColors.Sumi3
    }
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
            .background(if (over) AkariColors.Clay.copy(alpha = 0.10f) else AkariColors.Card)
            .border(1.dp, if (over) AkariColors.Clay.copy(alpha = 0.32f) else AkariColors.Line, RoundedCornerShape(14.dp))
            .padding(horizontal = 15.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        PathGlyph("M12 21s-7.5-4.9-10-9.2C.3 8.6 1.7 5 5.2 5 7.3 5 8.7 6.2 12 9c3.3-2.8 4.7-4 6.8-4 3.5 0 4.9 3.6 3.2 6.8C19.5 16.1 12 21 12 21Z", 17.dp, if (over) AkariColors.Clay else AkariColors.Ember, filled = true)
        Row(Modifier.weight(1f), verticalAlignment = Alignment.Bottom) {
            Text(hr?.toString() ?: "—", style = AkariText.LabelMedium.copy(fontSize = 15.sp), color = AkariColors.Sumi)
            Text(" bpm · resting ${state.restingHr} · ceiling ", style = AkariText.Label, color = AkariColors.Sumi2)
            Text("${state.ceiling}", style = AkariText.LabelMedium, color = AkariColors.Sumi)
        }
        Text((hrState?.tag ?: "reading…").uppercase(), style = AkariText.Tag, color = tagColor)
    }
}

@Composable
private fun NoWearableRow(vm: AppViewModel, motion: Boolean) {
    Row(
        Modifier.fillMaxWidth().pressScale(0.98f, motion).clip(RoundedCornerShape(14.dp))
            .border(1.dp, AkariColors.Line2, RoundedCornerShape(14.dp))
            .clickableRole(onClick = { vm.goHealthConnect() })
            .padding(horizontal = 15.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        PathGlyph("M12 21s-7.5-4.9-10-9.2C.3 8.6 1.7 5 5.2 5 7.3 5 8.7 6.2 12 9c3.3-2.8 4.7-4 6.8-4 3.5 0 4.9 3.6 3.2 6.8C19.5 16.1 12 21 12 21Z", 17.dp, AkariColors.Sumi3)
        Text("No wearable linked — pacing by feel", style = AkariText.Label, color = AkariColors.Sumi3, modifier = Modifier.weight(1f))
        Text("CONNECT", style = AkariText.Tag, color = AkariColors.Ai)
    }
}

@Composable
private fun SpentTodayCard(state: UiState) {
    val split = PacingEngine.split(state.entries)
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(AkariColors.Card)
            .border(1.dp, AkariColors.Line, RoundedCornerShape(16.dp)).padding(16.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            Eyebrow("Spent today")
            Row(verticalAlignment = Alignment.Bottom) {
                Text("${split.total}", style = AkariText.InlineNumber, color = AkariColors.Sumi)
                Text(" of ${state.startBattery}", style = AkariText.Label, color = AkariColors.Sumi2)
            }
        }
        Spacer(Modifier.height(11.dp))
        if (split.total > 0) {
            SplitBar(split.p, split.c, split.e, Modifier.fillMaxWidth().height(8.dp))
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                LegendDot(AkariColors.Clay, "Physical ${split.p}")
                LegendDot(AkariColors.Ai, "Cognitive ${split.c}")
                LegendDot(AkariColors.Plum, "Emotional ${split.e}")
            }
        } else {
            Text(
                if (state.poetic) "Nothing spent yet — the day is still whole." else "Nothing logged yet.",
                style = AkariText.poetic(14), color = AkariColors.Sumi3,
            )
        }
    }
}

@Composable
internal fun SplitBar(p: Int, c: Int, e: Int, modifier: Modifier) {
    Row(modifier.clip(RoundedCornerShape(5.dp)).background(AkariColors.Washi2)) {
        if (p > 0) Box(Modifier.fillMaxHeight().weight(p.toFloat()).background(AkariColors.Clay))
        if (c > 0) Box(Modifier.fillMaxHeight().weight(c.toFloat()).background(AkariColors.Ai))
        if (e > 0) Box(Modifier.fillMaxHeight().weight(e.toFloat()).background(AkariColors.Plum))
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(color))
        Text(label, style = AkariText.Caption, color = AkariColors.Sumi2)
    }
}

@Composable
private fun PemBanner(time: String?) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .background(AkariColors.Clay.copy(alpha = 0.12f))
            .border(1.dp, AkariColors.Clay.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("⚑", color = AkariColors.Clay, fontSize = 16.sp)
        Column {
            Text("PEM flagged · ${time ?: ""}", style = AkariText.LabelMedium.copy(fontSize = 14.sp), color = AkariColors.Sumi)
            Text("Rest is the work today. Akari will look back 48 h for what led here.", style = AkariText.Label, color = AkariColors.Sumi2)
        }
    }
}

@Composable
private fun TimelineRow(ev: DiaryEntry) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        Text(ev.timeLabel, style = AkariText.TabularTime, color = AkariColors.Sumi3, modifier = Modifier.width(42.dp))
        Box(Modifier.padding(top = 5.dp).size(9.dp).clip(CircleShape).background(dotColor(ev.kind)))
        Column(Modifier.weight(1f)) {
            Text(ev.name, style = AkariText.Body.copy(fontSize = 14.sp), color = AkariColors.Sumi)
            ev.sub?.let { Text(it, style = AkariText.Caption.copy(), color = AkariColors.Sumi3, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic) }
        }
        RightBadge(ev)
    }
}

@Composable
private fun RightBadge(ev: DiaryEntry) {
    when (ev.kind) {
        EntryKind.ACTIVITY -> Text("−${ev.total}", style = AkariText.LabelMedium, color = AkariColors.Ember, modifier = Modifier.align0())
        EntryKind.REST -> Badge("rest", Color(0xFF4F6942), AkariColors.Sage.copy(alpha = 0.16f))
        EntryKind.PEM -> Badge("PEM", Color.White, AkariColors.Clay)
        EntryKind.MEDS -> Badge("meds", AkariColors.Ai, AkariColors.Ai.copy(alpha = 0.14f))
        EntryKind.SYMPTOM -> Badge("symptom", Color(0xFF7A5068), AkariColors.Plum.copy(alpha = 0.16f))
        EntryKind.VITALS -> Badge("vitals", AkariColors.Ai, AkariColors.Ai.copy(alpha = 0.14f))
        EntryKind.NOTE -> Badge("note", AkariColors.Sumi2, AkariColors.Washi2)
        else -> Unit
    }
}

private fun Modifier.align0() = this // right badges align via Row baseline; number needs no chip

@Composable
private fun Badge(text: String, fg: Color, bg: Color) {
    Box(
        Modifier.clip(RoundedCornerShape(20.dp)).background(bg).padding(horizontal = 10.dp, vertical = 3.dp),
    ) { Text(text, style = AkariText.Tag.copy(letterSpacing = androidx.compose.ui.unit.TextUnit(0f, androidx.compose.ui.unit.TextUnitType.Em)), color = fg) }
}

private fun dotColor(kind: EntryKind): Color = when (kind) {
    EntryKind.ACTIVITY -> AkariColors.Ember
    EntryKind.REST -> AkariColors.Sage
    EntryKind.PEM -> AkariColors.Clay
    EntryKind.MEDS -> AkariColors.Ai
    EntryKind.WAKE -> AkariColors.Sumi3
    EntryKind.INTENTION -> AkariColors.Akari
    EntryKind.SYMPTOM -> AkariColors.Plum
    EntryKind.VITALS -> AkariColors.Ai
    EntryKind.NOTE -> AkariColors.Sumi3
}

private fun homeGreeting(name: String): String {
    val h = LocalTime.now().hour
    val base = if (h < 12) "good morning" else if (h < 18) "good afternoon" else "good evening"
    return if (name.isNotBlank()) "$name, $base" else base
}
