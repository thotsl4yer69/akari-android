package com.akari.app.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.akari.app.ui.AppViewModel
import com.akari.app.ui.DesignData
import com.akari.app.ui.UiState
import com.akari.app.ui.components.AkariSwitch
import com.akari.app.ui.components.AkariTextField
import com.akari.app.ui.components.Eyebrow
import com.akari.app.ui.components.PathGlyph
import com.akari.app.ui.components.clickableRole
import com.akari.app.ui.components.pressScale
import com.akari.app.ui.theme.AkariColors
import com.akari.app.ui.theme.AkariText

@Composable
fun SettingsScreen(vm: AppViewModel, state: UiState, motion: Boolean) {
    var showClearConfirmation by rememberSaveable { mutableStateOf(false) }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp).padding(top = 16.dp, bottom = 24.dp),
    ) {
        Eyebrow("You & your data")
        Spacer(Modifier.height(2.dp))
        Text("Settings", style = AkariText.ScreenTitle, color = AkariColors.Sumi)
        Spacer(Modifier.height(18.dp))

        // ---- Profile ----
        Section("Profile") {
            CardBox {
                Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Your name", style = AkariText.Label, color = AkariColors.Sumi2, modifier = Modifier.width(96.dp))
                    AkariTextField(
                        value = state.name, onValueChange = vm::setName, placeholder = "Optional",
                        textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth(),
                    )
                }
                Divider()
                Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Resting HR", style = AkariText.Label, color = AkariColors.Sumi2, modifier = Modifier.weight(1f))
                    Box(Modifier.width(56.dp).clip(RoundedCornerShape(9.dp)).background(AkariColors.Washi2).padding(8.dp)) {
                        AkariTextField(
                            value = state.restingHrText, onValueChange = vm::setResting,
                            keyboardType = KeyboardType.Number, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Row {
                        Text("ceiling ", style = AkariText.Label, color = AkariColors.Sumi3)
                        Text("${state.ceiling}", style = AkariText.LabelMedium, color = AkariColors.Ai)
                    }
                }
            }
        }

        // ---- Wearable ----
        Section("Wearable") {
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(AkariColors.Card)
                    .border(1.dp, AkariColors.Line, RoundedCornerShape(16.dp))
                    .clickableRole(onClick = { vm.goHealthConnect() })
                    .padding(horizontal = 16.dp, vertical = 15.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(13.dp),
            ) {
                Box(
                    Modifier.size(38.dp).clip(RoundedCornerShape(11.dp))
                        .background(if (state.hcConnected) AkariColors.Sage.copy(alpha = 0.16f) else AkariColors.Washi2),
                    contentAlignment = Alignment.Center,
                ) {
                    PathGlyph("M3 12h4l2 5 3-11 2 6h4", 20.dp, if (state.hcConnected) Color(0xFF5F7A51) else AkariColors.Sumi3)
                }
                Column(Modifier.weight(1f)) {
                    Text("Health Connect", style = AkariText.LabelMedium.copy(fontSize = androidx.compose.ui.unit.TextUnit(15f, androidx.compose.ui.unit.TextUnitType.Sp)), color = AkariColors.Sumi)
                    Text(
                        if (state.hcConnected) "Connected · reading your wearable" else "Not connected — tap to set up",
                        style = AkariText.Caption, color = if (state.hcConnected) AkariColors.Sage else AkariColors.Sumi3,
                    )
                }
                PathGlyph("M9 6l6 6-6 6", 18.dp, AkariColors.Sumi3, strokeWidth = 1.8.dp)
            }
        }

        // ---- The light ----
        Section("The light") {
            CardBox {
                Text("Lantern warmth", style = AkariText.Label, color = AkariColors.Sumi2, modifier = Modifier.padding(vertical = 4.dp))
                Row(Modifier.padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DesignData.hues.forEach { hex ->
                        val color = Color(hex)
                        val selected = state.lanternHue.value == color.value
                        Box(
                            Modifier.size(44.dp).pressScale(0.92f, motion).clip(CircleShape)
                                .background(Brush.radialGradient(0f to color, 0.55f to color, 1f to Color(0x14000000)))
                                .border(2.dp, if (selected) AkariColors.Sumi else Color(0x1F2D2A22), CircleShape)
                                .clickableRole(onClick = { vm.setHue(color) }),
                        )
                    }
                }
                Divider(top = 14.dp)
                Row(Modifier.fillMaxWidth().padding(top = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Gentle, poetic voice", style = AkariText.Body.copy(fontSize = androidx.compose.ui.unit.TextUnit(14f, androidx.compose.ui.unit.TextUnitType.Sp)), color = AkariColors.Sumi)
                        Text("“savasana counts” vs plain words", style = AkariText.Caption, color = AkariColors.Sumi3)
                    }
                    AkariSwitch(state.poetic, { vm.togglePoetic() }, motion)
                }
            }
        }

        // ---- Data ----
        Section("Data — stays on this phone") {
            CardBox {
                DataRow("M12 3v12M8 11l4 4 4-4M5 21h14", "Export for doctor (CSV)") { vm.exportCsv() }
                Divider()
                DataRow("M4 7h16v13H4zM4 7l3-3h10l3 3M9 12h6", "Back up everything (JSON)") { vm.backup() }
                Divider()
                DataRow("M4 12a8 8 0 1 0 2.3-5.6M4 4v3h3", "Restore from a backup") { vm.restore() }
                Divider()
                DataRow("M4 6h16v13H4zM4 6l3-3h10l3 3M12 10v6M9 13h6", "Clear everything") {
                    showClearConfirmation = true
                }
            }
        }

        Text(
            "Akari · a personal diary, not medical advice.\nNo account. No cloud. No internet.",
            style = AkariText.Caption, color = AkariColors.Sumi3,
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        )
    }

    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            containerColor = AkariColors.Card,
            titleContentColor = AkariColors.Sumi,
            textContentColor = AkariColors.Sumi2,
            title = { Text("Clear everything?", style = AkariText.OnboardH2) },
            text = {
                Text(
                    "This erases your diary, profile, and settings from this phone. You will return to the welcome screen.",
                    style = AkariText.Body,
                )
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmation = false }) {
                    Text("Keep my data", color = AkariColors.Sumi2)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearConfirmation = false
                        vm.clearAllData()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AkariColors.Clay,
                        contentColor = AkariColors.Washi,
                    ),
                ) {
                    Text("Clear everything")
                }
            },
        )
    }
}

@Composable
private fun Section(label: String, content: @Composable () -> Unit) {
    Eyebrow(label, modifier = Modifier.padding(start = 2.dp, bottom = 8.dp))
    content()
    Spacer(Modifier.height(20.dp))
}

@Composable
private fun CardBox(content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(AkariColors.Card)
            .border(1.dp, AkariColors.Line, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 4.dp),
    ) { content() }
}

@Composable
private fun Divider(top: Dp = 0.dp) {
    Box(Modifier.fillMaxWidth().padding(top = top).height(1.dp).background(AkariColors.Line))
}

@Composable
private fun DataRow(iconPath: String, label: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickableRole(onClick = onClick).padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PathGlyph(iconPath, 19.dp, AkariColors.Sumi2)
        Text(label, style = AkariText.Body.copy(fontSize = androidx.compose.ui.unit.TextUnit(14f, androidx.compose.ui.unit.TextUnitType.Sp)), color = AkariColors.Sumi)
    }
}

private typealias Dp = androidx.compose.ui.unit.Dp
