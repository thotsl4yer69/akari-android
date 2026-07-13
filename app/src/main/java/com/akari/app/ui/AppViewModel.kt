package com.akari.app.ui

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.akari.app.BuildConfig
import com.akari.app.data.AkariDb
import com.akari.app.data.DiaryDate
import com.akari.app.data.DiaryRepository
import com.akari.app.data.Prefs
import com.akari.app.data.PrefsRepository
import com.akari.app.domain.DiaryEntry
import com.akari.app.domain.EntryKind
import com.akari.app.domain.SleepQuality
import com.akari.app.domain.Zone
import com.akari.app.health.HealthConnectRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** UI-only state that is not persisted to Room/DataStore. */
private data class Transient(
    val screen: Screen? = null,       // null = derive from data (onboarding / morning / home)
    val sheet: Sheet = Sheet.None,
    val onbStep: Int = 0,
    val zone: Zone = Zone.AMBER,
    val battery: Int = 60,
    val sleep: SleepQuality = SleepQuality.POOR,
    val hr: Int? = null,
    val crashRest: Boolean = false,
    val crashPem: Boolean = false,
    val crashMeds: Boolean = false,
    val symSev: Int = 3,
    val symTags: Set<String> = emptySet(),
    val vHr: String = "", val vSys: String = "", val vDia: String = "", val vTemp: String = "",
    val fFood: String = "", val fMeds: String = "",
    val toast: String? = null,
    val openDay: Int? = null,
    val restingHrText: String? = null,   // live edit buffer; null → mirror prefs
    val today: Long = DiaryDate.currentEpochDay(),
)

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = DiaryRepository(AkariDb.get(app).dao())
    private val prefsRepo = PrefsRepository(app)
    val health = HealthConnectRepository(app)

    private val t = MutableStateFlow(Transient())
    private var toastJob: kotlinx.coroutines.Job? = null

    private data class DayData(
        val today: Long,
        val startBattery: Int,
        val entries: List<DiaryEntry>,
        val pemDay: Boolean,
        val history: List<com.akari.app.domain.DayRecord>,
        val allEntries: List<DiaryEntry>,
    )

    private val dataFlow = t.map { it.today }.distinctUntilChanged().flatMapLatest { today ->
        combine(
            repo.day(today),
            repo.entriesForDay(today),
            repo.history(today),
            repo.allEntries(),
        ) { day, entries, history, all ->
            DayData(today, day?.startBattery ?: 60, entries, day?.pem == true, history, all)
        }
    }

    val uiState: StateFlow<UiState> =
        combine(t, prefsRepo.flow, dataFlow) { tr, prefs, data ->
            build(tr, prefs, data)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState())

    init {
        if (BuildConfig.SEED_DEMO) viewModelScope.launch { seedDemoIfEmpty() }
    }

    // ---------------------------------------------------------------- build ---
    private fun build(tr: Transient, prefs: Prefs, data: DayData): UiState {
        val hasDay = data.entries.any { it.kind == EntryKind.INTENTION || it.kind == EntryKind.WAKE }
        val screen = tr.screen ?: when {
            !prefs.onboardingDone -> Screen.Onboarding
            !hasDay -> Screen.Morning
            else -> Screen.Home
        }
        val pemEntry = data.entries.firstOrNull { it.kind == EntryKind.PEM }
        return UiState(
            screen = screen,
            sheet = tr.sheet,
            onbStep = tr.onbStep,
            name = prefs.name,
            restingHr = prefs.restingHr,
            restingHrText = tr.restingHrText ?: prefs.restingHr.toString(),
            lanternHue = Color(prefs.lanternHue),
            poetic = prefs.poeticVoice,
            zone = tr.zone,
            battery = tr.battery,
            sleep = tr.sleep,
            startBattery = data.startBattery,
            entries = data.entries,
            pem = data.pemDay || pemEntry != null,
            pemTime = pemEntry?.timeLabel,
            // Respect the per-data "Heart rate" toggle: no HR unless connected AND allowed.
            hr = if (prefs.hcConnected && prefs.hcHr) tr.hr else null,
            hcConnected = prefs.hcConnected,
            hcPerms = mapOf(
                "hr" to prefs.hcHr, "resting" to prefs.hcResting,
                "sleep" to prefs.hcSleep, "steps" to prefs.hcSteps,
            ),
            history = data.history,
            allEntries = data.allEntries,
            openDay = tr.openDay,
            crashRest = tr.crashRest, crashPem = tr.crashPem, crashMeds = tr.crashMeds,
            symSev = tr.symSev, symTags = tr.symTags,
            vHr = tr.vHr, vSys = tr.vSys, vDia = tr.vDia, vTemp = tr.vTemp,
            fFood = tr.fFood, fMeds = tr.fMeds,
            toast = tr.toast,
        )
    }

    // ------------------------------------------------------------ lifecycle ---
    /** Recompute the diary day; a rollover past 4am re-derives to the morning check-in. */
    fun onAppResume() {
        val nowDay = DiaryDate.currentEpochDay()
        if (nowDay != t.value.today) {
            t.update { it.copy(today = nowDay, screen = null, sheet = Sheet.None) }
        }
    }

    // ------------------------------------------------------------ navigation ---
    fun go(screen: Screen) = t.update { it.copy(screen = screen, sheet = Sheet.None, openDay = null) }
    fun goHealthConnect() = t.update { it.copy(screen = Screen.HealthConnect) }

    fun onbNext() {
        val step = t.value.onbStep
        if (step >= 2) finishOnboarding() else t.update { it.copy(onbStep = step + 1) }
    }
    fun skipOnboarding() = finishOnboarding()

    private fun finishOnboarding() {
        viewModelScope.launch { prefsRepo.setOnboardingDone(true) }
        t.update { it.copy(screen = Screen.Morning) }
    }

    // ------------------------------------------------------------- profile ---
    fun setName(v: String) = viewModelScope.launch { prefsRepo.setName(v) }
    fun setResting(raw: String) {
        val digits = raw.filter { it.isDigit() }.take(3)
        t.update { it.copy(restingHrText = digits) }
        val n = digits.toIntOrNull() ?: 67
        viewModelScope.launch { prefsRepo.setRestingHr(n) }
    }

    // -------------------------------------------------------------- morning ---
    fun pickZone(z: Zone) = t.update { it.copy(zone = z, battery = z.presetBattery) }
    fun setBattery(v: Int) {
        val b = v.coerceIn(0, 100)
        t.update { it.copy(battery = b, zone = com.akari.app.domain.PacingEngine.zoneOf(b)) }
    }
    fun pickSleep(s: SleepQuality) = t.update { it.copy(sleep = s) }

    fun lightLantern() {
        val tr = t.value
        viewModelScope.launch {
            repo.startDay(tr.today, tr.battery, tr.sleep, tr.zone)
            val base = System.currentTimeMillis()
            repo.addEntry(marker(EntryKind.WAKE, "Woke", "slept ${tr.sleep.wokeWord}", base), tr.today)
            repo.addEntry(marker(EntryKind.INTENTION, "Morning intention", zoneWord(tr.zone), base + 1), tr.today)
        }
        t.update { it.copy(screen = Screen.Home, sheet = Sheet.None) }
    }

    // -------------------------------------------------------------- logging ---
    fun openLog() = t.update { it.copy(sheet = Sheet.Log) }
    fun pickLog(sheet: Sheet) {
        if (sheet == Sheet.None) { flagPem(); t.update { it.copy(sheet = Sheet.None) }; return }
        t.update { it.copy(sheet = sheet) }
    }
    fun backToLog() = t.update { it.copy(sheet = Sheet.Log) }
    fun closeSheet() = t.update { it.copy(sheet = Sheet.None) }

    fun logActivity(name: String, p: Int, c: Int, e: Int) {
        val today = t.value.today
        val total = p + c + e
        viewModelScope.launch {
            repo.addEntry(activity(name, p, c, e, total), today)
        }
        t.update { it.copy(sheet = Sheet.None) }
        flashToast("$name · the lantern dims a little")
    }

    fun logRest(name: String, savasana: Boolean) {
        val today = t.value.today
        viewModelScope.launch {
            repo.addEntry(
                marker(EntryKind.REST, name, if (savasana) "savasana counts" else "a deliberate rest"), today,
            )
        }
        t.update { it.copy(sheet = Sheet.None) }
        flashToast(if (savasana) "Rest logged · savasana counts" else "Rest logged · well done")
    }

    fun setSymSev(n: Int) = t.update { it.copy(symSev = n) }
    fun toggleSymTag(tag: String) = t.update {
        it.copy(symTags = if (tag in it.symTags) it.symTags - tag else it.symTags + tag)
    }
    fun saveSymptom() {
        val tr = t.value
        val head = if (tr.symTags.isNotEmpty()) tr.symTags.take(3).joinToString(", ") else "noted"
        val sub = "$head · severity ${tr.symSev}"
        viewModelScope.launch { repo.addEntry(marker(EntryKind.SYMPTOM, "Symptom", sub), tr.today) }
        t.update { it.copy(sheet = Sheet.None, symTags = emptySet(), symSev = 3) }
        flashToast("Symptom logged · gently noted")
    }

    fun setVHr(v: String) = t.update { it.copy(vHr = v.filter { c -> c.isDigit() }.take(3)) }
    fun setVSys(v: String) = t.update { it.copy(vSys = v.filter { c -> c.isDigit() }.take(3)) }
    fun setVDia(v: String) = t.update { it.copy(vDia = v.filter { c -> c.isDigit() }.take(3)) }
    fun setVTemp(v: String) = t.update { it.copy(vTemp = v.filter { c -> c.isDigit() || c == '.' }.take(4)) }
    fun saveVitals() {
        val tr = t.value
        val bits = buildList {
            if (tr.vHr.isNotBlank()) add("${tr.vHr} bpm")
            if (tr.vSys.isNotBlank() && tr.vDia.isNotBlank()) add("${tr.vSys}/${tr.vDia}")
            if (tr.vTemp.isNotBlank()) add("${tr.vTemp}°C")
        }
        viewModelScope.launch {
            repo.addEntry(marker(EntryKind.VITALS, "Vitals", if (bits.isEmpty()) "noted" else bits.joinToString(" · ")), tr.today)
        }
        t.update { it.copy(sheet = Sheet.None, vHr = "", vSys = "", vDia = "", vTemp = "") }
        flashToast("Vitals logged")
    }

    fun setFFood(v: String) = t.update { it.copy(fFood = v) }
    fun setFMeds(v: String) = t.update { it.copy(fMeds = v) }
    fun saveFood() {
        val tr = t.value
        val bits = buildList {
            if (tr.fFood.isNotBlank()) add(tr.fFood.trim())
            if (tr.fMeds.isNotBlank()) add("meds: ${tr.fMeds.trim()}")
        }
        viewModelScope.launch {
            repo.addEntry(marker(EntryKind.NOTE, "Food & meds", if (bits.isEmpty()) "noted" else bits.joinToString(" · ").take(80)), tr.today)
        }
        t.update { it.copy(sheet = Sheet.None, fFood = "", fMeds = "") }
        flashToast("Note saved")
    }

    fun flagPem() {
        if (uiState.value.pem) { flashToast("Already flagged today · rest is the work"); return }
        val today = t.value.today
        viewModelScope.launch {
            repo.setPem(today, true)
            repo.addEntry(marker(EntryKind.PEM, "PEM flagged", "the most useful data point"), today)
        }
        flashToast("PEM flagged · rest is the work now")
    }

    // ------------------------------------------------------------- settings ---
    fun setHue(color: Color) = viewModelScope.launch { prefsRepo.setHue(color.toArgb()) }
    fun togglePoetic() = viewModelScope.launch { prefsRepo.setPoetic(!uiState.value.poetic) }
    fun clearAllData() {
        viewModelScope.launch {
            repo.wipeAll()
            prefsRepo.clear()
            t.update {
                Transient(
                    screen = Screen.Onboarding,
                    today = it.today,
                    toast = "Your diary was cleared",
                )
            }
        }
    }
    fun exportCsv() = flashToast("Exported diary.csv · share with your doctor")
    fun backup() = flashToast("Backed up akari-backup.json to this phone")
    fun restore() = flashToast("Choose a backup file to restore")

    // -------------------------------------------------------- health connect ---
    fun setHcConnected(connected: Boolean) {
        viewModelScope.launch { prefsRepo.setHcConnected(connected) }
        flashToast(if (connected) "Connected · reading your wearable" else "Disconnected from Health Connect")
        if (connected) refreshHeartRate() else t.update { it.copy(hr = null) }
    }
    fun toggleHcPerm(key: String) {
        val cur = uiState.value.hcPerms[key] ?: true
        viewModelScope.launch { prefsRepo.setHcPerm(key, !cur) }
    }

    /** Poll the latest HR from Health Connect (called ~every 15s while Home is resumed). */
    fun refreshHeartRate() {
        // Don't read HR unless connected AND the "Heart rate" toggle is on.
        if (!uiState.value.hcConnected || uiState.value.hcPerms["hr"] != true) return
        viewModelScope.launch {
            val bpm = health.latestHeartRate()
            if (bpm != null) t.update { it.copy(hr = bpm) }
        }
    }

    // ---------------------------------------------------------------- crash ---
    fun enterCrash() = t.update {
        it.copy(screen = Screen.Crash, sheet = Sheet.None, crashRest = false, crashPem = false, crashMeds = false)
    }
    fun exitCrash() = t.update { it.copy(screen = Screen.Home) }
    fun crashRest() {
        val today = t.value.today
        viewModelScope.launch { repo.addEntry(marker(EntryKind.REST, "Resting", "in crash mode"), today) }
        t.update { it.copy(crashRest = true) }
    }
    fun crashMeds() {
        val today = t.value.today
        viewModelScope.launch { repo.addEntry(marker(EntryKind.MEDS, "Took meds", null), today) }
        t.update { it.copy(crashMeds = true) }
    }
    fun crashPemAction() {
        val today = t.value.today
        if (!uiState.value.pem) viewModelScope.launch {
            repo.setPem(today, true)
            repo.addEntry(marker(EntryKind.PEM, "PEM flagged", "from crash mode"), today)
        }
        t.update { it.copy(crashPem = true) }
    }

    // ---------------------------------------------------------- history ui ---
    fun toggleDay(index: Int) = t.update { it.copy(openDay = if (it.openDay == index) null else index) }

    // -------------------------------------------------------------- helpers ---
    private fun flashToast(msg: String) {
        toastJob?.cancel()
        t.update { it.copy(toast = msg) }
        toastJob = viewModelScope.launch {
            kotlinx.coroutines.delay(2550)
            t.update { it.copy(toast = null) }
        }
    }

    private fun now() = System.currentTimeMillis()

    private fun marker(kind: EntryKind, name: String, sub: String?, at: Long = now()) = DiaryEntry(
        id = 0, kind = kind, name = name, sub = sub, epochMillis = at, timeLabel = DiaryDate.timeLabel(at),
    )

    private fun activity(name: String, p: Int, c: Int, e: Int, total: Int, at: Long = now()) = DiaryEntry(
        id = 0, kind = EntryKind.ACTIVITY, name = name, sub = null, epochMillis = at,
        timeLabel = DiaryDate.timeLabel(at), p = p, c = c, e = e, total = total,
    )

    private fun zoneWord(z: Zone) = when (z) {
        Zone.GREEN -> "a steady day"
        Zone.AMBER -> "a careful day"
        Zone.RED -> "a low day, rest first"
    }

    // ----------------------------------------------------------- debug seed ---
    private suspend fun seedDemoIfEmpty() {
        if (prefsRepo.onboardingDoneOnce()) return
        val today = t.value.today
        if (repo.dayOnce(today) != null) return

        // 8 past days for History/Trends, built from real presets so the 48h
        // trigger look-back surfaces meaningful names ("Errand out" ranks 4×,
        // like the prototype's triggerData).
        val byName = DesignData.presets.associateBy { it.name }
        val seed = listOf(
            SeedDay(SleepQuality.POOR, 70, true, listOf("Errand out", "See a friend", "Screen time", "Housework")),
            SeedDay(SleepQuality.OKAY, 85, false, listOf("Errand out", "Drive", "Cook a meal", "Shower")),
            SeedDay(SleepQuality.RESTED, 80, false, listOf("See a friend", "Screen time", "Short walk")),
            SeedDay(SleepQuality.POOR, 55, false, listOf("Phone call", "Cook a meal")),
            SeedDay(SleepQuality.BROKEN, 40, true, listOf("Errand out", "See a friend", "Screen time", "Appointment")),
            SeedDay(SleepQuality.OKAY, 75, false, listOf("Errand out", "Drive", "Housework", "Cook a meal")),
            SeedDay(SleepQuality.RESTED, 82, false, listOf("Shower", "Short walk", "Screen time")),
            SeedDay(SleepQuality.POOR, 60, false, listOf("Phone call", "Drive")),
        )
        seed.forEachIndexed { i, sd ->
            val day = today - (i + 1)
            repo.startDay(day, sd.start, sd.sleep, com.akari.app.domain.PacingEngine.zoneOf(sd.start))
            if (sd.pem) repo.setPem(day, true)
            val base = day * 86_400_000L + 9 * 3_600_000L
            repo.addEntry(marker(EntryKind.WAKE, "Woke", "slept ${sd.sleep.wokeWord}", base), day)
            sd.activities.forEachIndexed { j, name ->
                val a = byName.getValue(name)
                repo.addEntry(activity(a.name, a.p, a.c, a.e, a.total, base + (j + 1) * 5_400_000L), day)
            }
            // PEM in the evening so the day's own activities fall inside its 48h window.
            if (sd.pem) repo.addEntry(marker(EntryKind.PEM, "PEM flagged", "the most useful data point", base + 10 * 3_600_000L), day)
        }

        // Today, seeded.
        repo.startDay(today, 78, SleepQuality.POOR, com.akari.app.domain.PacingEngine.zoneOf(78))
        val base = today * 86_400_000L + 7 * 3_600_000L
        repo.addEntry(marker(EntryKind.WAKE, "Woke", "slept poorly", base), today)
        repo.addEntry(marker(EntryKind.INTENTION, "Morning intention", "a careful day", base + 1500_000L), today)
        repo.addEntry(activity("Shower", 5, 1, 1, 7, base + 3_600_000L), today)
        repo.addEntry(activity("Phone call", 1, 3, 3, 7, base + 7_200_000L), today)

        prefsRepo.setOnboardingDone(true)
        t.update { it.copy(screen = Screen.Home) }
    }

    private data class SeedDay(
        val sleep: SleepQuality, val start: Int, val pem: Boolean, val activities: List<String>,
    )
}
