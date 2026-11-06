package com.pulsepath.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import kotlin.math.roundToInt

/** Typing totals for one app. Only numbers, never text. */
data class AppTyping(
    val packageName: String,
    val chars: Long,
    val millis: Long
) {
    /** One "word" is 5 characters. */
    val wpm: Int
        get() = if (millis <= 0L) 0 else ((chars / 5.0) / (millis / 60_000.0)).roundToInt()
}

/** Saves typing activity from other apps: characters and active typing time per app per day. */
object TypingTracker {
    private const val FILE = "pulsepath_typing_tracker"
    private const val KEY_ENABLED = "enabled"
    private const val DAY_PREFIX = "d_"
    private const val KEEP_DAYS = 30L

    private fun prefs(context: Context) =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    /** True once the user has accepted the consent box and has not paused tracking. */
    fun isEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_ENABLED, false)

    fun setEnabled(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean(KEY_ENABLED, value).apply()
    }

    /** Adds one typing burst to today's totals. */
    @Synchronized
    fun addBurst(context: Context, packageName: String, chars: Int, millis: Long) {
        val key = DAY_PREFIX + LocalDate.now()
        val p = prefs(context)
        val day = try {
            JSONObject(p.getString(key, "{}") ?: "{}")
        } catch (error: Exception) {
            JSONObject()
        }

        val old = day.optJSONArray(packageName)
        val newChars = (old?.optLong(0) ?: 0L) + chars
        val newMillis = (old?.optLong(1) ?: 0L) + millis
        day.put(packageName, JSONArray().put(newChars).put(newMillis))

        val edit = p.edit().putString(key, day.toString())

        // Forget days older than 30 days
        val cutoff = DAY_PREFIX + LocalDate.now().minusDays(KEEP_DAYS)
        p.all.keys
            .filter { it.startsWith(DAY_PREFIX) && it < cutoff }
            .forEach { edit.remove(it) }

        edit.apply()
    }

    /** Totals per app for the last [days] days, busiest app first. */
    @Synchronized
    fun loadApps(context: Context, days: Int): List<AppTyping> {
        val p = prefs(context)
        val chars = HashMap<String, Long>()
        val millis = HashMap<String, Long>()

        for (back in 0 until days) {
            val key = DAY_PREFIX + LocalDate.now().minusDays(back.toLong())
            val text = p.getString(key, null) ?: continue
            try {
                val day = JSONObject(text)
                day.keys().forEach { pkg ->
                    val entry = day.getJSONArray(pkg)
                    chars[pkg] = (chars[pkg] ?: 0L) + entry.getLong(0)
                    millis[pkg] = (millis[pkg] ?: 0L) + entry.getLong(1)
                }
            } catch (error: Exception) {
                // Skip a damaged day
            }
        }

        return chars.keys
            .map { AppTyping(it, chars[it] ?: 0L, millis[it] ?: 0L) }
            .sortedByDescending { it.chars }
    }

    /** Deletes all saved typing activity. */
    @Synchronized
    fun clear(context: Context) {
        val p = prefs(context)
        val edit = p.edit()
        p.all.keys.filter { it.startsWith(DAY_PREFIX) }.forEach { edit.remove(it) }
        edit.apply()
    }
}