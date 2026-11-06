package com.pulsepath.app

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.lifecycleScope
import com.pulsepath.app.data.HistoryStore
import com.pulsepath.app.data.ReminderScheduler
import com.pulsepath.app.data.SettingsPrefs
import com.pulsepath.app.data.hasUsageAccess
import com.pulsepath.app.navigation.PulsePathNavHost
import com.pulsepath.app.ui.theme.PulsePathTheme
import com.pulsepath.app.ui.theme.ThemeState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Start in the mode the user chose last time
        ThemeState.isDark = SettingsPrefs.isDarkMode(this)

        setContent {
            val dark = ThemeState.isDark

            // Light icons on a dark background, dark icons on a light one
            DisposableEffect(dark) {
                val style = if (dark) {
                    SystemBarStyle.dark(AndroidColor.TRANSPARENT)
                } else {
                    SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
                }
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
                onDispose { }
            }

            PulsePathTheme {
                PulsePathNavHost()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        ReminderScheduler.sync(applicationContext)
        saveHistoryInBackground()
    }

    override fun onStop() {
        super.onStop()
        saveHistoryInBackground()
    }

    /** Saves the last 8 days of usage inside PulsePath, if Monitoring is on and permission is granted. */
    private fun saveHistoryInBackground() {
        val appContext = applicationContext
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                if (hasUsageAccess(appContext) && SettingsPrefs.isMonitoringEnabled(appContext)) {
                    HistoryStore.syncRecent(appContext)
                }
            } catch (e: Exception) {
                // Saving history must never crash the app
            }
        }
    }
}