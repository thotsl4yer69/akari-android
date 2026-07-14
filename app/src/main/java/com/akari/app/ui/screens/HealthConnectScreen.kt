package com.akari.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.PermissionController
import com.akari.app.health.HealthConnectRepository
import com.akari.app.ui.AppViewModel
import com.akari.app.ui.UiState
import com.akari.app.ui.components.AkariSwitch
import com.akari.app.ui.components.Eyebrow
import com.akari.app.ui.components.PathGlyph
import com.akari.app.ui.components.clickableRole
import com.akari.app.ui.theme.AkariColors
import com.akari.app.ui.theme.AkariText

@Composable
fun HealthConnectScreen(vm: AppViewModel, state: UiState) {
    val available = vm.health.availability() == HealthConnectRepository.Availability.AVAILABLE
    val launcher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract(),
    ) { granted ->
        if (granted.containsAll(vm.health.permissions)) vm.setHcConnected(true)
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp).padding(top = 14.dp, bottom = 24.dp),
    ) {
        Row(
            Modifier.clickableRole(onClick = { vm.go(com.akari.app.ui.Screen.Settings) }).padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            PathGlyph("M15 6l-6 6 6 6", 18.dp, AkariColors.Sumi2, strokeWidth = 1.8.dp)
            Text("Settings", style = AkariText.Body.copy(fontSize = 14.sp), color = AkariColors.Sumi2)
        }
        Spacer(Modifier.height(8.dp))
        Text("Health Connect", style = AkariText.ScreenTitle, color = AkariColors.Sumi)
        Spacer(Modifier.height(6.dp))
        Text(
            "Optional. Akari can read vitals your wearable already writes to Health Connect — never the other way around, and never over the internet.",
            style = AkariText.Body.copy(fontSize = 14.sp), color = AkariColors.Sumi2,
        )
        Spacer(Modifier.height(22.dp))

        Eyebrow("Allow reading", modifier = Modifier.padding(start = 2.dp, bottom = 8.dp))
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(AkariColors.Card)
                .border(1.dp, AkariColors.Line, RoundedCornerShape(16.dp)).padding(horizontal = 16.dp),
        ) {
            // Akari only reads heart rate (data minimization — see
            // HealthConnectRepository + the manifest). Show only what we read.
            val perms = listOf(
                Triple("hr", "Heart rate", "beat-to-beat during the day"),
            )
            perms.forEachIndexed { i, (key, label, sub) ->
                val on = state.hcPerms[key] == true
                val connectedOn = on && state.hcConnected
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(Modifier.size(9.dp).clip(RoundedCornerShape(50)).background(if (connectedOn) AkariColors.Sage else AkariColors.Sumi3))
                    Column(Modifier.weight(1f)) {
                        Text(label, style = AkariText.Body.copy(fontSize = 14.sp), color = AkariColors.Sumi)
                        Text(sub, style = AkariText.Caption, color = AkariColors.Sumi3)
                    }
                    AkariSwitch(connectedOn, { vm.toggleHcPerm(key) })
                }
                if (i < perms.lastIndex) Box(Modifier.fillMaxWidth().height(1.dp).background(AkariColors.Line))
            }
        }
        Spacer(Modifier.height(20.dp))

        // connect / disconnect
        val connected = state.hcConnected
        Box(
            Modifier.fillMaxWidth().height(54.dp).clip(RoundedCornerShape(16.dp))
                .background(if (connected) Color.Transparent else AkariColors.Sumi)
                .then(if (connected) Modifier.border(1.dp, AkariColors.Clay.copy(alpha = 0.4f), RoundedCornerShape(16.dp)) else Modifier)
                .clickableRole {
                    if (connected) vm.setHcConnected(false)
                    else if (available) launcher.launch(vm.health.permissions)
                    else vm.setHcConnected(true) // no provider on this device → let the user preview the connected state
                },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                if (connected) "Disconnect" else "Connect Health Connect",
                style = AkariText.Button.copy(fontSize = 15.sp),
                color = if (connected) AkariColors.Clay else AkariColors.Washi,
            )
        }
        Spacer(Modifier.height(18.dp))

        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                .background(AkariColors.Ai.copy(alpha = 0.08f)).padding(horizontal = 16.dp, vertical = 15.dp),
        ) {
            Text("Pacing ceiling", style = AkariText.LabelMedium.copy(fontSize = 13.sp), color = AkariColors.Sumi)
            Spacer(Modifier.height(6.dp))
            Row {
                Text("Set to resting + 15 bpm = ", style = AkariText.Label, color = AkariColors.Sumi2)
                Text("${state.ceiling} bpm", style = AkariText.LabelMedium, color = AkariColors.Ai)
                Text(". Akari nudges gently when your heart rate climbs above it.", style = AkariText.Label, color = AkariColors.Sumi2)
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "Community favourites for ME/CFS pacing: Garmin (Body Battery), or the Visible app with its Polar armband.",
            style = AkariText.Caption, color = AkariColors.Sumi3,
        )
    }
}
