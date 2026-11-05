package com.pulsepath.app.data

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent

/** True if the user switched PulsePath's typing service on in Android's Accessibility settings. */
fun isTypingServiceEnabled(context: Context): Boolean {
    val enabled = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false
    val me = ComponentName(context, TypingMonitorService::class.java)
    return enabled.split(':').any { ComponentName.unflattenFromString(it) == me }
}

/** Opens Android's Accessibility settings page. */
fun openAccessibilitySettings(context: Context) {
    context.startActivity(
        Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}

/**
 * Measures typing speed in other apps.
 * It only looks at HOW MANY characters were added and WHEN. It never reads the text itself,
 * ignores password fields, and skips large jumps such as pastes.
 */
class TypingMonitorService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
    private val flushLater = Runnable { flush() }

    private var burstPackage: String? = null
    private var burstChars = 0
    private var burstMillis = 0L
    private var lastEventTime = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) return
        if (event.isPassword) return

        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName) return   // the PulsePath typing test is measured separately

        if (!TypingTracker.isEnabled(this) || !SettingsPrefs.isMonitoringEnabled(this)) return

        // Large jumps are pastes, autofill or voice typing. They end the burst and are not counted.
        if (event.addedCount > MAX_ADDED_PER_EVENT) {
            flush()
            return
        }

        val now = SystemClock.elapsedRealtime()
        val sameBurst = pkg == burstPackage && now - lastEventTime <= MAX_GAP_MILLIS

        if (!sameBurst) {
            flush()
            // The first keystroke only starts the clock
            burstPackage = pkg
            lastEventTime = now
        } else {
            burstMillis += now - lastEventTime
            burstChars += event.addedCount
            lastEventTime = now
        }

        handler.removeCallbacks(flushLater)
        handler.postDelayed(flushLater, MAX_GAP_MILLIS + 500L)
    }

    /** Saves the current burst and starts fresh. */
    private fun flush() {
        handler.removeCallbacks(flushLater)
        val pkg = burstPackage
        if (pkg != null && burstChars >= MIN_BURST_CHARS && burstMillis > 0L) {
            TypingTracker.addBurst(this, pkg, burstChars, burstMillis)
        }
        burstPackage = null
        burstChars = 0
        burstMillis = 0L
    }

    override fun onInterrupt() {
        flush()
    }

    override fun onDestroy() {
        flush()
        super.onDestroy()
    }

    private companion object {
        const val MAX_GAP_MILLIS = 2_000L       // a longer pause ends the burst
        const val MAX_ADDED_PER_EVENT = 10      // more than this at once is a paste, not typing
        const val MIN_BURST_CHARS = 3
    }
}