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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.akari.app.domain.PacingEngine
import com.akari.app.ui.AppViewModel
import com.akari.app.ui.UiState
import com.akari.app.ui.components.Eyebrow
import com.akari.app.ui.theme.AkariColors
import com.akari.app.ui.theme.AkariText

private data class Bar(val light: Int, val pem: Boolean)

@Composable
fun TrendsScreen(vm: AppViewModel, state: UiState, motion: Boolean) {
    // oldest → newest, last bar is today
    val past = state.history.reversed().takeLast(13).map { Bar(it.remaining, it.pem) }
    val bars = past + Bar(state.energy, state.pem)
    val ready = state.history.size >= 2

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp).padding(top = 16.dp, bottom = 24.dp),
    ) {
        Eyebrow("Last 14 days")
        Spacer(Modifier.height(2.dp))
        Text("Trends", style = AkariText.ScreenTitle, color = AkariColors.Sumi)
        Spacer(Modifier.height(18.dp))

        if (!ready) {
            EmptyTrends(state.poetic)
            return@Column
        }

        EnvelopeCard(bars)
        Spacer(Modifier.height(16.dp))

        val crashes = state.history.count { it.pem } + if (state.pem) 1 else 0
        val avgSpent = PacingEngine.avgSpentPerDay(state.history)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("$crashes", "crashes flagged", AkariColors.Clay, AkariColors.Clay.copy(alpha = 0.10f), AkariColors.Clay.copy(alpha = 0.24f), Modifier.weight(1f))
            StatCard("$avgSpent", "avg spent / day", AkariColors.Sumi, AkariColors.Card, AkariColors.Line, Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))

        TriggersCard(PacingEngine.detectTriggers(state.allEntries))
        Spacer(Modifier.height(16.dp))
        Text(
            "A personal diary, not medical advice. Share the export with your doctor.",
            style = AkariText.Caption, color = AkariColors.Sumi3,
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun EnvelopeCard(bars: List<Bar>) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(AkariColors.Card)
            .border(1.dp, AkariColors.Line, RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp, vertical = 18.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Eyebrow("Energy envelope")
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Box(Modifier.size(5.dp).clip(RoundedCornerShape(3.dp)).background(AkariColors.Clay))
                Text("crash day", style = AkariText.Tag.copy(letterSpacing = androidx.compose.ui.unit.TextUnit(0f, androidx.compose.ui.unit.TextUnitType.Em)), color = AkariColors.Sumi3)
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth().height(92.dp), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.Bottom) {
            bars.forEach { b ->
                val level = PacingEngine.barLevel(b.light)
                val color = when (level) {
                    PacingEngine.BarLevel.GREEN -> AkariColors.Sage
                    PacingEngine.BarLevel.AMBER -> AkariColors.Akari
                    PacingEngine.BarLevel.CLAY -> AkariColors.Clay
                }
                Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.Bottom, horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier.fillMaxWidth()
                            .fillMaxHeight(b.light.coerceIn(6, 100) / 100f)
                            .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp, bottomStart = 2.dp, bottomEnd = 2.dp))
                            .alpha(PacingEngine.barOpacity(b.light))
                            .background(color),
                    )
                    if (b.pem) {
                        Spacer(Modifier.height(4.dp))
                        Box(Modifier.size(5.dp).clip(RoundedCornerShape(3.dp)).background(AkariColors.Clay))
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("2 wks ago", style = AkariText.Caption, color = AkariColors.Sumi3)
            Text("avg ${PacingEngine.avgLight(bars.map { it.light })} light", style = AkariText.Caption, color = AkariColors.Sumi3)
            Text("today", style = AkariText.Caption, color = AkariColors.Sumi3)
        }
    }
}

@Composable
private fun StatCard(figure: String, label: String, figColor: Color, bg: Color, border: Color, modifier: Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(16.dp)).background(bg).border(1.dp, border, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 15.dp),
    ) {
        Text(figure, style = AkariText.StatFigure, color = figColor)
        Spacer(Modifier.height(4.dp))
        Text(label, style = AkariText.Caption, color = AkariColors.Sumi2)
    }
}

@Composable
private fun TriggersCard(triggers: List<PacingEngine.Trigger>) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(AkariColors.Card)
            .border(1.dp, AkariColors.Line, RoundedCornerShape(18.dp)).padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(AkariColors.Ai))
            Eyebrow("Possible triggers")
        }
        Spacer(Modifier.height(6.dp))
        Text(
            "Activities most often logged in the 48 hours before a crash. Patterns, not proof — the delay hides them from feeling.",
            style = AkariText.Label, color = AkariColors.Sumi2,
        )
        Spacer(Modifier.height(16.dp))
        if (triggers.isEmpty()) {
            Text("No crashes flagged yet — nothing to correlate.", style = AkariText.Label, color = AkariColors.Sumi3)
        } else {
            val maxC = triggers.maxOf { it.count }
            triggers.forEach { t ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Text(t.name, style = AkariText.Label, color = AkariColors.Sumi)
                    Text("${t.count}× before crashes", style = AkariText.Caption, color = AkariColors.Sumi3)
                }
                Spacer(Modifier.height(5.dp))
                Box(Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(4.dp)).background(AkariColors.Washi2)) {
                    Box(
                        Modifier.fillMaxWidth(t.count.toFloat() / maxC).height(7.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .alpha((0.5f + t.count.toFloat() / maxC * 0.5f))
                            .background(AkariColors.Ai),
                    )
                }
                Spacer(Modifier.height(13.dp))
            }
        }
    }
}

@Composable
private fun EmptyTrends(poetic: Boolean) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(AkariColors.Card)
            .border(1.dp, AkariColors.Line, RoundedCornerShape(18.dp))
            .padding(horizontal = 20.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(Modifier.height(56.dp), horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.Bottom) {
            GhostBar(0.34f, AkariColors.Washi2, 1f)
            GhostBar(0.58f, AkariColors.Washi2, 1f)
            GhostBar(0.42f, AkariColors.Washi2, 1f)
            GhostBar(0.72f, AkariColors.Akari, 0.35f)
        }
        Spacer(Modifier.height(18.dp))
        Text("The envelope needs a few days", style = AkariText.poetic(19).copy(fontWeight = FontWeight.Medium, fontStyle = androidx.compose.ui.text.font.FontStyle.Normal), color = AkariColors.Sumi)
        Spacer(Modifier.height(7.dp))
        Text(
            if (poetic) "After two or three lit lanterns, your pattern begins to show." else "Charts appear after a few days of entries.",
            style = AkariText.Label, color = AkariColors.Sumi2, textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun GhostBar(heightFrac: Float, color: Color, alpha: Float) {
    Box(
        Modifier.width(14.dp).fillMaxHeight(heightFrac)
            .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp, bottomStart = 2.dp, bottomEnd = 2.dp))
            .alpha(alpha).background(color),
    )
}
