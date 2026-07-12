package com.akari.app.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akari.app.ui.AppViewModel
import com.akari.app.ui.UiState
import com.akari.app.ui.components.PathGlyph
import com.akari.app.ui.components.clickableRole
import com.akari.app.ui.components.pressScale
import com.akari.app.ui.theme.AkariText
import com.akari.app.ui.theme.CrashColors

@Composable
fun CrashScreen(vm: AppViewModel, state: UiState, motion: Boolean) {
    val anyDone = state.crashRest || state.crashPem || state.crashMeds
    Column(
        Modifier.fillMaxSize()
            .background(
                Brush.radialGradient(
                    0f to CrashColors.BgTop, 0.55f to CrashColors.BgMid, 1f to CrashColors.BgDeep,
                    center = Offset(0.5f, 0.3f) * 2000f, radius = 2000f,
                ),
            )
            .padding(horizontal = 26.dp).padding(top = 40.dp, bottom = 30.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(top = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            CoalLantern(motion)
            Spacer(Modifier.height(26.dp))
            Text("You're in a crash.", style = AkariText.SheetTitle, color = CrashColors.Text)
            Spacer(Modifier.height(8.dp))
            Text("This is enough. Nothing else is asked of you.", style = AkariText.poetic(16), color = CrashColors.TextDim)
        }

        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    CrashTile(
                        label = if (state.crashRest) "Resting ✓" else "I'm resting",
                        done = state.crashRest, doneBg = Color(0x2E7E9A6E), doneBd = Color(0x807E9A6E),
                        motion = motion, modifier = Modifier.weight(1f),
                        icon = { PathGlyph("M20 14a8 8 0 1 1-9-11 6 6 0 0 0 9 11z", 30.dp, CrashColors.Text, strokeWidth = 1.5.dp) },
                    ) { vm.crashRest() }
                    CrashTile(
                        label = if (state.crashPem) "Flagged ✓" else "Flag PEM",
                        done = state.crashPem, doneBg = Color(0x33C4745A), doneBd = Color(0x8CC4745A),
                        motion = motion, modifier = Modifier.weight(1f),
                        icon = { Text("⚑", fontSize = 28.sp, color = CrashColors.Text) },
                    ) { vm.crashPemAction() }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    CrashTile(
                        label = if (state.crashMeds) "Meds ✓" else "Took meds",
                        done = state.crashMeds, doneBg = Color(0x3345688A), doneBd = Color(0x8045688A),
                        motion = motion, modifier = Modifier.weight(1f),
                        icon = { PathGlyph("M3 8h18v8H3zM12 8v8", 30.dp, CrashColors.Text, strokeWidth = 1.5.dp) },
                    ) { vm.crashMeds() }
                    CrashTile(
                        label = "Back to light",
                        done = false, doneBg = Color.Transparent, doneBd = Color.Transparent,
                        motion = motion, modifier = Modifier.weight(1f), dim = true,
                        icon = { PathGlyph("M12 3v9M6.5 7A8 8 0 1 0 18 8", 30.dp, CrashColors.TextDim, strokeWidth = 1.5.dp) },
                    ) { vm.exitCrash() }
                }
            }
        }

        Box(Modifier.fillMaxWidth().height(20.dp), contentAlignment = Alignment.Center) {
            Text(
                if (anyDone) "logged, quietly" else "no sound, no pressure",
                style = AkariText.Caption, color = Color(0xFF6A5D4C),
            )
        }
    }
}

@Composable
private fun CoalLantern(motion: Boolean) {
    val infinite = rememberInfiniteTransition(label = "ember")
    val pulse = if (motion) infinite.animateFloat(
        1f, 1.08f, infiniteRepeatable(tween(2500), RepeatMode.Reverse), label = "pulse",
    ).value else 1f
    val glow = if (motion) infinite.animateFloat(
        0.5f, 0.8f, infiniteRepeatable(tween(2500), RepeatMode.Reverse), label = "glow",
    ).value else 0.6f

    Box(Modifier.size(70.dp, 88.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(150.dp).graphicsLayer { scaleX = pulse; scaleY = pulse; alpha = glow }) {
            drawCircle(
                Brush.radialGradient(
                    0f to Color(0x8CD9793F), 0.7f to Color.Transparent,
                    center = center, radius = this.size.minDimension / 2f,
                ),
            )
        }
        Box(
            Modifier.size(70.dp, 88.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(percent = 46))
                .background(Brush.radialGradient(listOf(Color(0xFF6E3F1C), Color(0xFF2A1C11)))),
        )
    }
}

@Composable
private fun CrashTile(
    label: String,
    done: Boolean,
    doneBg: Color,
    doneBd: Color,
    motion: Boolean,
    modifier: Modifier,
    dim: Boolean = false,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
) {
    Column(
        modifier
            .height(128.dp)
            .pressScale(0.98f, motion)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(22.dp))
            .background(if (done) doneBg else Color(0x0DE8D3C4))
            .border(1.dp, if (done) doneBd else Color(0x24E8D3C4), androidx.compose.foundation.shape.RoundedCornerShape(22.dp))
            .clickableRole(onClick = onClick)
            .padding(10.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        icon()
        Spacer(Modifier.height(10.dp))
        Text(
            label, style = AkariText.Button.copy(fontSize = 17.sp), textAlign = TextAlign.Center,
            color = if (dim) CrashColors.TextDim else Color(0xFFE2D3BD),
        )
    }
}
