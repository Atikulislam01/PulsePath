package com.pulsepath.app.data

import android.content.Context

/** Saves the Settings switches, your name and the day/night choice on the phone. */
object SettingsPrefs {
    private const val FILE = "pulsepath_settings"
    private const val KEY_MONITORING = "monitoring_enabled"
    private const val KEY_REMINDERS = "reminders_enabled"
    private const val KEY_LAST_REMINDER = "last_reminder_at"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_NAME_ASKED = "name_asked"
    private const val KEY_ONBOARDING_DONE = "onboarding_done"
    private const val KEY_DARK_MODE = "dark_mode"

    private fun prefs(context: Context) =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun isMonitoringEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_MONITORING, true)

    fun setMonitoringEnabled(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean(KEY_MONITORING, value).apply()
    }

    fun areRemindersEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_REMINDERS, true)

    fun setRemindersEnabled(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean(KEY_REMINDERS, value).apply()
    }

    /** When the last reminder was sent (milliseconds), or 0 if never. */
    fun lastReminderAt(context: Context): Long =
        prefs(context).getLong(KEY_LAST_REMINDER, 0L)

    fun setLastReminderAt(context: Context, value: Long) {
        prefs(context).edit().putLong(KEY_LAST_REMINDER, value).apply()
    }

    /** Your name, or an empty text if you haven't set one. */
    fun userName(context: Context): String =
        prefs(context).getString(KEY_USER_NAME, "") ?: ""

    fun setUserName(context: Context, value: String) {
        prefs(context).edit().putString(KEY_USER_NAME, value.trim()).apply()
    }

    /** True once the "What's your name?" box has been shown. */
    fun wasNameAsked(context: Context): Boolean =
        prefs(context).getBoolean(KEY_NAME_ASKED, false)

    fun setNameAsked(context: Context) {
        prefs(context).edit().putBoolean(KEY_NAME_ASKED, true).apply()
    }

    /** True once the Welcome, Onboarding and Permission screens have been completed. */
    fun isOnboardingDone(context: Context): Boolean =
        prefs(context).getBoolean(KEY_ONBOARDING_DONE, false)

    fun setOnboardingDone(context: Context) {
        prefs(context).edit().putBoolean(KEY_ONBOARDING_DONE, true).apply()
    }

    /** True if the user chose night mode. Day mode is the default. */
    fun isDarkMode(context: Context): Boolean =
        prefs(context).getBoolean(KEY_DARK_MODE, false)

    fun setDarkMode(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean(KEY_DARK_MODE, value).apply()
    }
}