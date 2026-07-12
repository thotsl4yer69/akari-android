package com.akari.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akari.app.domain.DayRecord
import com.akari.app.ui.AppViewModel
import com.akari.app.ui.UiState
import com.akari.app.ui.components.Eyebrow
import com.akari.app.ui.components.clickableRole
import com.akari.app.ui.theme.AkariColors
import com.akari.app.ui.theme.AkariText
import com.akari.app.ui.theme.LocalLanternHue

@Composable
fun HistoryScreen(vm: AppViewModel, state: UiState, motion: Boolean) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp).padding(top = 16.dp, bottom = 24.dp),
    ) {
        Eyebrow("Your diary")
        Spacer(Modifier.height(2.dp))
        Text("History", style = AkariText.ScreenTitle, color = AkariColors.Sumi)
        Spacer(Modifier.height(18.dp))

        if (state.history.isEmpty()) {
            EmptyHistory(state.poetic)
            return@Column
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            state.history.forEachIndexed { i, day ->
                HistoryRow(day, state.openDay == i) { vm.toggleDay(i) }
            }
        }
    }
}

@Composable
private fun HistoryRow(day: DayRecord, open: Boolean, onTap: () -> Unit) {
    val summary = "${day.dow} ${day.date}. Slept ${day.sleep.label.lowercase()}, spent ${day.spent} of ${day.startBattery}, ${day.remaining} light left" +
        (if (day.pem) ", PEM flagged" else "") + if (open) ", expanded" else ""
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(AkariColors.Card)
            .border(1.dp, if (open) AkariColors.Line2 else AkariColors.Line, RoundedCornerShape(16.dp)),
    ) {
        Row(
            Modifier.fillMaxWidth()
                .clickableRole(onClick = onTap)
                .padding(horizontal = 15.dp, vertical = 13.dp)
                .semanticsLabel(summary),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            MiniLantern(day.remaining)
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(day.dow, style = AkariText.LabelMedium.copy(fontSize = 15.sp), color = AkariColors.Sumi)
                    Text(day.date, style = AkariText.Caption, color = AkariColors.Sumi3)
                }
                Text(
                    "Slept ${day.sleep.label.lowercase()} · spent ${day.spent} of ${day.startBattery}",
                    style = AkariText.Caption, color = AkariColors.Sumi2,
                )
            }
            if (day.pem) {
                Box(Modifier.clip(RoundedCornerShape(20.dp)).background(AkariColors.Clay).padding(horizontal = 9.dp, vertical = 3.dp)) {
                    Text("PEM", style = AkariText.Tag, color = Color.White)
                }
            }
            Text("${day.remaining}", style = AkariText.RowNumber, color = AkariColors.Sumi)
        }

        AnimatedVisibility(open, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
            Column(Modifier.padding(start = 59.dp, end = 15.dp, bottom = 14.dp)) {
                SplitBar(day.p, day.c, day.e, Modifier.fillMaxWidth().height(7.dp))
                Spacer(Modifier.height(9.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DetailFig(AkariColors.Clay, "Phys", day.p)
                    DetailFig(AkariColors.Ai, "Cog", day.c)
                    DetailFig(AkariColors.Plum, "Emo", day.e)
                }
            }
        }
    }
}

private fun Modifier.semanticsLabel(label: String): Modifier =
    this.then(Modifier.semantics { contentDescription = label })

@Composable
private fun DetailFig(color: Color, label: String, value: Int) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("▪", color = color, style = AkariText.Caption)
        Text("$label $value", style = AkariText.Caption, color = AkariColors.Sumi2)
    }
}

@Composable
private fun MiniLantern(remaining: Int) {
    val hue = LocalLanternHue.current
    Box(Modifier.size(30.dp, 38.dp)) {
        Box(
            Modifier.fillMaxSize()
                .clip(RoundedCornerShape(percent = 46))
                .background(Brush.radialGradient(listOf(Color(0xFFF7E6C4), Color(0xFFE6D3A6))))
                .border(1.dp, Color(0x1F785022), RoundedCornerShape(percent = 46)),
        ) {
            Box(
                Modifier.fillMaxSize()
                    .alpha((remaining / 100f).coerceIn(0f, 1f))
                    .background(Brush.radialGradient(0f to hue, 0.72f to hue.copy(alpha = 0f))),
            )
        }
    }
}

@Composable
private fun EmptyHistory(poetic: Boolean) {
    Column(
        Modifier.fillMaxSize().padding(top = 58.dp, start = 30.dp, end = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // unlit mini lantern with caps
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.width(22.dp).height(5.dp).clip(RoundedCornerShape(3.dp)).alpha(0.75f).background(AkariColors.Sumi))
            Box(
                Modifier.size(56.dp, 70.dp).clip(RoundedCornerShape(percent = 46))
                    .background(Brush.radialGradient(listOf(Color(0xFFF7E6C4), Color(0xFFE6D3A6))))
                    .border(1.dp, Color(0x1F785022), RoundedCornerShape(percent = 46)),
            ) {
                Box(Modifier.fillMaxSize().alpha(0.10f).background(Brush.radialGradient(0f to AkariColors.Akari, 0.72f to Color.Transparent)))
            }
            Box(Modifier.width(18.dp).height(4.dp).clip(RoundedCornerShape(2.dp)).alpha(0.6f).background(AkariColors.Sumi))
        }
        Spacer(Modifier.height(20.dp))
        Text("No days here yet", style = AkariText.poetic(20).copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Normal, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium), color = AkariColors.Sumi)
        Spacer(Modifier.height(7.dp))
        Text(
            if (poetic) "When tonight comes, today will settle here — your first lantern." else "Days appear here after your first evening.",
            style = AkariText.Label, color = AkariColors.Sumi2, textAlign = TextAlign.Center,
        )
    }
}

