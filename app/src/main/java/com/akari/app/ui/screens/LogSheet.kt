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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akari.app.domain.ActivityPreset
import com.akari.app.ui.AppViewModel
import com.akari.app.ui.DesignData
import com.akari.app.ui.Sheet
import com.akari.app.ui.UiState
import com.akari.app.ui.components.AkariTextField
import com.akari.app.ui.components.PathGlyph
import com.akari.app.ui.components.clickableRole
import com.akari.app.ui.components.pressScale
import com.akari.app.ui.theme.AkariColors
import com.akari.app.ui.theme.AkariText

@Composable
fun LogSheet(vm: AppViewModel, state: UiState, motion: Boolean) {
    Box(Modifier.fillMaxSize()) {
        // scrim
        Box(
            Modifier.fillMaxSize().background(Color(0x572A1E0E))
                .clickableRole(onClick = { vm.closeSheet() }),
        )
        // sheet
        Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().fillMaxHeight(0.82f)
                .clip(RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp))
                .background(AkariColors.Washi),
        ) {
            Box(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 4.dp), contentAlignment = Alignment.Center) {
                Box(Modifier.width(38.dp).height(5.dp).clip(RoundedCornerShape(3.dp)).background(AkariColors.Line2))
            }
            Row(
                Modifier.fillMaxWidth().padding(start = 22.dp, end = 22.dp, top = 6.dp, bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (state.sheet != Sheet.Log) {
                    Box(
                        Modifier.size(34.dp).clip(CircleShape).background(AkariColors.Card)
                            .border(1.dp, AkariColors.Line2, CircleShape)
                            .clickableRole(onClick = { vm.backToLog() }),
                        contentAlignment = Alignment.Center,
                    ) { PathGlyph("M15 6l-6 6 6 6", 17.dp, AkariColors.Sumi2, strokeWidth = 1.9.dp) }
                }
                Column(Modifier.weight(1f)) {
                    Text(title(state.sheet), style = AkariText.SheetTitle, color = AkariColors.Sumi)
                    Text(sub(state.sheet, state.poetic), style = AkariText.poetic(15), color = AkariColors.Sumi2)
                }
            }
            Column(
                Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                    .padding(start = 20.dp, end = 20.dp, bottom = 26.dp, top = 4.dp),
            ) {
                when (state.sheet) {
                    Sheet.Log -> Chooser(vm)
                    Sheet.Activity -> Activities(vm)
                    Sheet.Rest -> Rests(vm)
                    Sheet.Symptom -> SymptomForm(vm, state, motion)
                    Sheet.Vitals -> VitalsForm(vm, state, motion)
                    Sheet.FoodMeds -> FoodForm(vm, state, motion)
                    Sheet.None -> Unit
                }
            }
        }
    }
}

@Composable
private fun Chooser(vm: AppViewModel) {
    GridTwo(DesignData.logTypes) { lt ->
        val tint = tintColor(lt.tint)
        val (bg, bd) = when (lt.tint) {
            DesignData.Tint.SAGE -> AkariColors.Sage.copy(alpha = 0.09f) to AkariColors.Sage.copy(alpha = 0.28f)
            DesignData.Tint.CLAY -> AkariColors.Clay.copy(alpha = 0.10f) to AkariColors.Clay.copy(alpha = 0.28f)
            else -> AkariColors.Card to AkariColors.Line
        }
        Column(
            Modifier.fillMaxWidth().pressScale(0.97f).clip(RoundedCornerShape(16.dp)).background(bg)
                .border(1.dp, bd, RoundedCornerShape(16.dp))
                .clickableRole(onClick = { vm.pickLog(lt.sheet) })
                .padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            PathGlyph(lt.iconPath, 24.dp, tint)
            Column {
                Text(lt.name, style = AkariText.Body.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium), color = AkariColors.Sumi)
                Text(lt.sub, style = AkariText.Caption, color = AkariColors.Sumi3)
            }
        }
    }
}

@Composable
private fun Activities(vm: AppViewModel) {
    GridTwo(DesignData.presets) { a: ActivityPreset ->
        Column(
            Modifier.fillMaxWidth().pressScale(0.97f).clip(RoundedCornerShape(16.dp)).background(AkariColors.Card)
                .border(1.dp, AkariColors.Line, RoundedCornerShape(16.dp))
                .clickableRole(onClick = { vm.logActivity(a.name, a.p, a.c, a.e) })
                .padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                PathGlyph(a.iconPath, 22.dp, AkariColors.Sumi2)
                Text("−${a.total}", style = AkariText.Label, color = AkariColors.Sumi3)
            }
            Text(a.name, style = AkariText.Body.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium), color = AkariColors.Sumi)
            SplitBar(a.p, a.c, a.e, Modifier.fillMaxWidth().height(5.dp))
        }
    }
    Spacer(Modifier.height(18.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)) {
        Legend(AkariColors.Clay, "Physical")
        Legend(AkariColors.Ai, "Cognitive")
        Legend(AkariColors.Plum, "Emotional")
    }
}

@Composable
private fun Rests(vm: AppViewModel) {
    GridTwo(DesignData.rests) { r ->
        Row(
            Modifier.fillMaxWidth().pressScale(0.97f).clip(RoundedCornerShape(16.dp))
                .background(AkariColors.Sage.copy(alpha = 0.09f))
                .border(1.dp, AkariColors.Sage.copy(alpha = 0.28f), RoundedCornerShape(16.dp))
                .clickableRole(onClick = { vm.logRest(r.name, r.savasana) })
                .padding(15.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PathGlyph(r.iconPath, 22.dp, Color(0xFF5F7A51))
            Text(r.name, style = AkariText.Body.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium), color = AkariColors.Sumi)
        }
    }
}

@Composable
private fun SymptomForm(vm: AppViewModel, state: UiState, motion: Boolean) {
    Text("How strong, right now?", style = AkariText.Label, color = AkariColors.Sumi2)
    Spacer(Modifier.height(10.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
        (1..5).forEach { n ->
            val on = n <= state.symSev
            Box(
                Modifier.weight(1f).height(46.dp).clip(RoundedCornerShape(13.dp))
                    .background(if (on) AkariColors.Plum.copy(alpha = 0.16f) else AkariColors.Card)
                    .border(1.5.dp, if (on) AkariColors.Plum else AkariColors.Line2, RoundedCornerShape(13.dp))
                    .clickableRole(onClick = { vm.setSymSev(n) }),
                contentAlignment = Alignment.Center,
            ) { Text("$n", style = AkariText.InlineNumber, color = if (on) Color(0xFF7A5068) else AkariColors.Sumi3) }
        }
    }
    Spacer(Modifier.height(22.dp))
    Text("What are you feeling?", style = AkariText.Label, color = AkariColors.Sumi2)
    Spacer(Modifier.height(10.dp))
    FlowChips(DesignData.symptomTags, state.symTags) { vm.toggleSymTag(it) }
    Spacer(Modifier.height(24.dp))
    DarkButton("Log symptom", motion, onClick = { vm.saveSymptom() })
}

@Composable
private fun VitalsForm(vm: AppViewModel, state: UiState, motion: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FieldRow("Heart rate", trailing = "bpm") {
            NumField(state.vHr, vm::setVHr, KeyboardType.Number)
        }
        FieldRow("Blood pressure") {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                NumField(state.vSys, vm::setVSys, KeyboardType.Number, 52.dp)
                Text("/", color = AkariColors.Sumi3)
                NumField(state.vDia, vm::setVDia, KeyboardType.Number, 52.dp)
            }
        }
        FieldRow("Temperature", trailing = "°C") {
            NumField(state.vTemp, vm::setVTemp, KeyboardType.Decimal)
        }
    }
    Spacer(Modifier.height(22.dp))
    DarkButton("Log vitals", motion, onClick = { vm.saveVitals() })
}

@Composable
private fun FoodForm(vm: AppViewModel, state: UiState, motion: Boolean) {
    Text("Food & drink", style = AkariText.Label, color = AkariColors.Sumi2)
    Spacer(Modifier.height(8.dp))
    NoteArea(state.fFood, vm::setFFood, "Toast, tea, a little soup…")
    Spacer(Modifier.height(18.dp))
    Text("Medications & supplements", style = AkariText.Label, color = AkariColors.Sumi2)
    Spacer(Modifier.height(8.dp))
    NoteArea(state.fMeds, vm::setFMeds, "LDN, salt, electrolytes…")
    Spacer(Modifier.height(24.dp))
    DarkButton("Save note", motion, onClick = { vm.saveFood() })
}

// ---- small building blocks ----

@Composable
private fun <T> GridTwo(items: List<T>, item: @Composable (T) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
        items.chunked(2).forEach { pair ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                Box(Modifier.weight(1f)) { item(pair[0]) }
                if (pair.size > 1) Box(Modifier.weight(1f)) { item(pair[1]) } else Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun Legend(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(color))
        Text(label, style = AkariText.Tag.copy(letterSpacing = androidx.compose.ui.unit.TextUnit(0f, androidx.compose.ui.unit.TextUnitType.Em)), color = AkariColors.Sumi3)
    }
}

@Composable
private fun FlowChips(tags: List<String>, selected: Set<String>, onTap: (String) -> Unit) {
    // simple wrapping via chunks of 3 to stay layout-safe at large font scales
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        tags.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                row.forEach { tag ->
                    val on = tag in selected
                    Box(
                        Modifier.clip(RoundedCornerShape(20.dp))
                            .background(if (on) AkariColors.Plum.copy(alpha = 0.14f) else AkariColors.Card)
                            .border(1.5.dp, if (on) AkariColors.Plum else AkariColors.Line2, RoundedCornerShape(20.dp))
                            .clickableRole(onClick = { onTap(tag) })
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                    ) { Text(tag, style = AkariText.Label, color = if (on) Color(0xFF7A5068) else AkariColors.Sumi2) }
                }
            }
        }
    }
}

@Composable
private fun FieldRow(label: String, trailing: String? = null, content: @Composable () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(AkariColors.Card)
            .border(1.dp, AkariColors.Line, RoundedCornerShape(14.dp)).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(label, style = AkariText.Body.copy(fontSize = 14.sp), color = AkariColors.Sumi2, modifier = Modifier.weight(1f))
        content()
        if (trailing != null) Text(trailing, style = AkariText.Label, color = AkariColors.Sumi3, modifier = Modifier.width(34.dp))
    }
}

@Composable
private fun NumField(value: String, onChange: (String) -> Unit, kb: KeyboardType, width: androidx.compose.ui.unit.Dp = 70.dp) {
    Box(Modifier.width(width).clip(RoundedCornerShape(9.dp)).background(AkariColors.Washi2).padding(9.dp)) {
        AkariTextField(value, onChange, placeholder = "—", keyboardType = kb, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun NoteArea(value: String, onChange: (String) -> Unit, placeholder: String) {
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp)).background(AkariColors.Card)
            .border(1.dp, AkariColors.Line2, RoundedCornerShape(13.dp)).padding(horizontal = 15.dp, vertical = 13.dp),
    ) {
        AkariTextField(value, onChange, placeholder = placeholder, singleLine = false, modifier = Modifier.fillMaxWidth())
    }
}

private fun tintColor(t: DesignData.Tint): Color = when (t) {
    DesignData.Tint.EMBER -> AkariColors.Ember
    DesignData.Tint.SAGE -> Color(0xFF5F7A51)
    DesignData.Tint.PLUM -> AkariColors.Plum
    DesignData.Tint.AI -> AkariColors.Ai
    DesignData.Tint.SUMI2 -> AkariColors.Sumi2
    DesignData.Tint.CLAY -> AkariColors.Clay
}

private fun title(sheet: Sheet): String = when (sheet) {
    Sheet.Log -> "Log something"
    Sheet.Activity -> "What did you do?"
    Sheet.Rest -> "Rest is the work"
    Sheet.Symptom -> "How are you?"
    Sheet.Vitals -> "Vitals"
    Sheet.FoodMeds -> "Food & meds"
    Sheet.None -> ""
}

private fun sub(sheet: Sheet, poetic: Boolean): String = when (sheet) {
    Sheet.Log -> "Everything here stays on your phone."
    Sheet.Activity -> if (poetic) "One tap logs it and dims the lantern." else "Tap to log an activity."
    Sheet.Rest -> if (poetic) "Choose a kind of rest — savasana counts." else "Log a deliberate rest."
    Sheet.Symptom -> "A gentle, honest note."
    Sheet.Vitals -> "Only what you want to record."
    Sheet.FoodMeds -> "Whatever helps you notice patterns."
    Sheet.None -> ""
}
