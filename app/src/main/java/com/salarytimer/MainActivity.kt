package com.salarytimer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.salarytimer.ui.screens.HomeScreen
import com.salarytimer.ui.screens.SettingsScreen
import com.salarytimer.ui.theme.SalaryTimerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SalaryTimerTheme {
                val vm: SalaryViewModel = viewModel()
                val result by vm.result.collectAsState()
                val settings by vm.settings.collectAsState()
                var showSettings by remember { mutableStateOf(false) }

                if (showSettings) {
                    SettingsScreen(
                        settings = settings,
                        onSave = { vm.saveSettings(it) },
                        onBack = { showSettings = false }
                    )
                } else {
                    HomeScreen(
                        result = result,
                        onSettingsClick = { showSettings = true }
                    )
                }
            }
        }
    }
}
