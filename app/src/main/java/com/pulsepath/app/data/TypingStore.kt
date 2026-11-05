package com.pulsepath.app.data

import android.content.Context
import org.json.JSONArray

/** One finished typing test. Only numbers are stored, never the typed text. */
data class TypingResult(
    val timeMillis: Long,       // when the test finished
    val wpm: Int,               // words per minute
    val accuracy: Int,          // percent
    val durationMillis: Long    // how long it took
)

/** Saves typing test results on the phone. */
object TypingStore {
    private const val FILE = "pulsepath_typing"
    private const val KEY = "results"
    private const val MAX_RESULTS = 50

    private fun prefs(context: Context) =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    /** All saved results, oldest first. */
    fun load(context: Context): List<TypingResult> {
        val text = prefs(context).getString(KEY, null) ?: return emptyList()
        return try {
            val array = JSONArray(text)
            (0 until array.length()).map { i ->
                val item = array.getJSONArray(i)
                TypingResult(
                    timeMillis = item.getLong(0),
                    wpm = item.getInt(1),
                    accuracy = item.getInt(2),
                    durationMillis = item.getLong(3)
                )
            }
        } catch (error: Exception) {
            emptyList()
        }
    }

    fun add(context: Context, result: TypingResult) {
        val list = (load(context) + result).takeLast(MAX_RESULTS)
        val array = JSONArray()
        list.forEach { r ->
            array.put(
                JSONArray()
                    .put(r.timeMillis)
                    .put(r.wpm)
                    .put(r.accuracy)
                    .put(r.durationMillis)
            )
        }
        prefs(context).edit().putString(KEY, array.toString()).apply()
    }

    fun clear(context: Context) {
        prefs(context).edit().remove(KEY).apply()
    }
}