package com.akari.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.compose.ui.graphics.toArgb
import com.akari.app.ui.theme.AkariColors
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Single app-wide DataStore. Lives under files/datastore/ — backed up with
// the DB, never leaves the device.
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "akari_prefs")

/** All device-local settings, no account, no cloud. */
data class Prefs(
    val name: String = "",
    val restingHr: Int = 67,
    val lanternHue: Int = DEFAULT_HUE,
    val poeticVoice: Boolean = true,
    val onboardingDone: Boolean = false,
    val hcConnected: Boolean = false,
    val hcHr: Boolean = true,
    val hcResting: Boolean = true,
    val hcSleep: Boolean = true,
    val hcSteps: Boolean = true,
) {
    companion object {
        val DEFAULT_HUE: Int = AkariColors.Akari.toArgb()
    }
}

class PrefsRepository(private val context: Context) {

    val flow: Flow<Prefs> = context.dataStore.data.map { p ->
        Prefs(
            name = p[K.name] ?: "",
            restingHr = p[K.restingHr] ?: 67,
            lanternHue = p[K.hue] ?: Prefs.DEFAULT_HUE,
            poeticVoice = p[K.poetic] ?: true,
            onboardingDone = p[K.onboarding] ?: false,
            hcConnected = p[K.hcConnected] ?: false,
            hcHr = p[K.hcHr] ?: true,
            hcResting = p[K.hcResting] ?: true,
            hcSleep = p[K.hcSleep] ?: true,
            hcSteps = p[K.hcSteps] ?: true,
        )
    }

    suspend fun onboardingDoneOnce(): Boolean = flow.first().onboardingDone

    suspend fun setName(v: String) = edit { it[K.name] = v }
    suspend fun setRestingHr(v: Int) = edit { it[K.restingHr] = v }
    suspend fun setHue(argb: Int) = edit { it[K.hue] = argb }
    suspend fun setPoetic(v: Boolean) = edit { it[K.poetic] = v }
    suspend fun setOnboardingDone(v: Boolean) = edit { it[K.onboarding] = v }
    suspend fun setHcConnected(v: Boolean) = edit { it[K.hcConnected] = v }
    suspend fun setHcPerm(key: String, v: Boolean) = edit {
        when (key) {
            "hr" -> it[K.hcHr] = v
            "resting" -> it[K.hcResting] = v
            "sleep" -> it[K.hcSleep] = v
            "steps" -> it[K.hcSteps] = v
        }
    }

    private suspend fun edit(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        context.dataStore.edit(block)
    }

    private object K {
        val name = stringPreferencesKey("name")
        val restingHr = intPreferencesKey("resting_hr")
        val hue = intPreferencesKey("lantern_hue")
        val poetic = booleanPreferencesKey("poetic_voice")
        val onboarding = booleanPreferencesKey("onboarding_done")
        val hcConnected = booleanPreferencesKey("hc_connected")
        val hcHr = booleanPreferencesKey("hc_hr")
        val hcResting = booleanPreferencesKey("hc_resting")
        val hcSleep = booleanPreferencesKey("hc_sleep")
        val hcSteps = booleanPreferencesKey("hc_steps")
    }
}
