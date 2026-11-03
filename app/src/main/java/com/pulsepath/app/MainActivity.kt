package com.pulsepath.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.pulsepath.app.data.HistoryStore
import com.pulsepath.app.data.ReminderScheduler
import com.pulsepath.app.data.SettingsPrefs
import com.pulsepath.app.data.hasUsageAccess
import com.pulsepath.app.navigation.PulsePathNavHost
import com.pulsepath.app.ui.theme.PulsePathTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
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