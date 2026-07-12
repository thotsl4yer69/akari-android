package com.akari.app.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.akari.app.ui.AppViewModel
import com.akari.app.ui.UiState
import com.akari.app.ui.components.AkariTextField
import com.akari.app.ui.components.Eyebrow
import com.akari.app.ui.components.Lantern
import com.akari.app.ui.components.LanternSize
import com.akari.app.ui.components.clickableRole
import com.akari.app.ui.components.pressScale
import com.akari.app.ui.theme.AkariColors
import com.akari.app.ui.theme.AkariText

@Composable
fun OnboardingScreen(vm: AppViewModel, state: UiState, motion: Boolean) {
    Column(Modifier.fillMaxSize().padding(horizontal = 26.dp).padding(top = 6.dp, bottom = 20.dp)) {
        Box(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())) {
            when (state.onbStep) {
                0 -> WelcomeCard(motion)
                1 -> HowItHelpsCard()
                else -> MakeItYoursCard(vm, state)
            }
        }

        // pager dots
        Row(
            Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp, Alignment.CenterHorizontally),
        ) {
            repeat(3) { i ->
                val w by animateDpAsState(if (state.onbStep == i) 22.dp else 6.dp, label = "dot")
                Box(Modifier.width(w).height(6.dp).clip(RoundedCornerShape(3.dp))
                    .background(if (state.onbStep == i) AkariColors.Sumi else AkariColors.Line2))
            }
        }

        DarkButton(
            text = if (state.onbStep >= 2) "Light your first day" else "Continue",
            motion = motion,
            onClick = { vm.onbNext() },
        )
        Box(Modifier.fillMaxWidth().height(32.dp).padding(top = 12.dp), contentAlignment = Alignment.Center) {
            if (state.onbStep < 2) {
                Text(
                    "Skip introduction", style = AkariText.Label, color = AkariColors.Sumi3,
                    modifier = Modifier.clip(RoundedCornerShape(8.dp))
                        .clickableRole { vm.skipOnboarding() }
                        .padding(4.dp),
                )
            }
        }
    }
}

@Composable
private fun WelcomeCard(motion: Boolean) {
    Column(
        Modifier.fillMaxSize().padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Lantern(energy = 90f, startEnergy = 100f, size = LanternSize.Onboard, motion = motion)
        Spacer(Modifier.height(30.dp))
        Text("明 か り", style = AkariText.poetic(15).copy(letterSpacing = 0.5.em), color = AkariColors.Sumi2)
        Spacer(Modifier.height(2.dp))
        Text("Akari", style = AkariText.Wordmark, color = AkariColors.Sumi)
        Spacer(Modifier.height(14.dp))
        Text(
            "A gentle place to pace your energy — and to stay within your own light.",
            style = AkariText.poetic(19), color = AkariColors.Sumi2, textAlign = TextAlign.Center,
            modifier = Modifier.width(280.dp),
        )
    }
}

@Composable
private fun HowItHelpsCard() {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
        Eyebrow("How it helps")
        Spacer(Modifier.height(16.dp))
        Text(
            "Your energy is a lantern. It glows in the morning and dims as the day is spent.",
            style = AkariText.OnboardH2, color = AkariColors.Sumi,
        )
        Spacer(Modifier.height(20.dp))
        Text(
            "The goal is never to do more — only to stay within the envelope, and prevent the crash that follows overreaching.",
            style = AkariText.Body, color = AkariColors.Sumi2,
        )
        Spacer(Modifier.height(22.dp))
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                .background(AkariColors.Card).padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                Modifier.size(30.dp).clip(CircleShape).background(AkariColors.Clay.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) { Text("⚑", color = AkariColors.Clay, fontSize = 15.sp) }
            Text(
                buildHighlight("One tap marks a crash.", " Because PEM is delayed 12–72 hours, Akari looks back over the 48 hours before — revealing the triggers you can't feel in the moment."),
                style = AkariText.Body.copy(fontSize = 14.sp), color = AkariColors.Sumi2,
            )
        }
    }
}

@Composable
private fun MakeItYoursCard(vm: AppViewModel, state: UiState) {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
        Eyebrow("Make it yours")
        Spacer(Modifier.height(16.dp))
        Text("A private diary. Only for you.", style = AkariText.OnboardH2, color = AkariColors.Sumi)
        Spacer(Modifier.height(22.dp))

        Text("What should Akari call you?", style = AkariText.Label, color = AkariColors.Sumi2)
        Spacer(Modifier.height(8.dp))
        InputBox {
            AkariTextField(
                value = state.name, onValueChange = vm::setName,
                placeholder = "Your name (optional)", modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(Modifier.height(18.dp))

        Text("Resting heart rate — sets your pacing ceiling", style = AkariText.Label, color = AkariColors.Sumi2)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            InputBox(Modifier.width(84.dp)) {
                AkariTextField(
                    value = state.restingHrText, onValueChange = vm::setResting,
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                    textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(),
                )
            }
            Text(
                buildAnnotatedCeiling(state.ceiling),
                style = AkariText.Body.copy(fontSize = 14.sp), color = AkariColors.Sumi3,
            )
        }
        Spacer(Modifier.height(20.dp))
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                .background(AkariColors.Ai.copy(alpha = 0.08f)).padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("⦿", color = AkariColors.Ai, fontSize = 15.sp)
            Text(
                "Everything stays on this phone. No account, no cloud — the app has no way to reach the internet.",
                style = AkariText.Label, color = AkariColors.Sumi2,
            )
        }
    }
}

@Composable
private fun InputBox(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(AkariColors.Card)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) { content() }
}

@Composable
fun DarkButton(text: String, motion: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .fillMaxWidth()
            .height(56.dp)
            .pressScale(0.98f, motion)
            .clip(RoundedCornerShape(16.dp))
            .background(AkariColors.Sumi)
            .clickableRole(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = AkariText.Button, color = AkariColors.Washi)
    }
}

private fun buildHighlight(bold: String, rest: String) = androidx.compose.ui.text.buildAnnotatedString {
    pushStyle(androidx.compose.ui.text.SpanStyle(color = AkariColors.Sumi, fontWeight = FontWeight.Medium))
    append(bold)
    pop()
    append(rest)
}

private fun buildAnnotatedCeiling(ceiling: Int) = androidx.compose.ui.text.buildAnnotatedString {
    append("bpm · ceiling ")
    pushStyle(androidx.compose.ui.text.SpanStyle(color = AkariColors.Ai, fontWeight = FontWeight.Medium))
    append("$ceiling")
    pop()
    append(" (resting + 15)")
}
