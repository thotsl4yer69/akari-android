package com.akari.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.akari.app.ui.AkariApp
import com.akari.app.ui.AppViewModel
import com.akari.app.ui.theme.AkariTheme

class MainActivity : ComponentActivity() {

    private val vm: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state by vm.uiState.collectAsStateWithLifecycle()
            AkariTheme(lanternHue = state.lanternHue, poeticVoice = state.poetic) {
                AkariApp(vm, state)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        vm.onAppResume()
    }
}
