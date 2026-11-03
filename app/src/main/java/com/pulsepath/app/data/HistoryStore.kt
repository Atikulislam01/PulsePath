package com.pulsepath.app.data

import android.content.Context
import org.json.JSONArray
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Keeps a daily copy of your sessions inside PulsePath, one small file per day,
 * so old days don't disappear when Android forgets them.
 */
object HistoryStore {

    const val RECOVERY_DAYS = 30
    private const val DAY_MILLIS = 24L * 3_600_000L
    private const val PREFS = "pulsepath_history"
    private const val KEY_DELETED_AT = "deleted_at"

    private fun liveDir(context: Context) = File(context.filesDir, "history")
    private fun trashDir(context: Context) = File(context.filesDir, "history_trash")
    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun fileFor(context: Context, date: LocalDate) =
        File(liveDir(context), "$date.json")

    // ---------- Saving and reading ----------

    /** Saves one day. An existing copy with more usage in it is never overwritten by a smaller one. */
    @Synchronized
    fun saveDay(context: Context, date: LocalDate, sessions: List<UsageSessionRecord>) {
        if (sessions.isEmpty()) return

        val old = readDay(context, date)
        val newTotal = sessions.sumOf { it.durationMillis }
        if (old != null && old.sumOf { it.durationMillis } >= newTotal) return

        val array = JSONArray()
        sessions.forEach { s ->
            array.put(
                JSONArray()
                    .put(s.packageName)
                    .put(s.startMillis)
                    .put(s.endMillis)
            )
        }

        val file = fileFor(context, date)
        file.parentFile?.mkdirs()
        file.writeText(array.toString())
    }

    /** The saved sessions of one day, or null if that day was never saved. */
    @Synchronized
    fun readDay(context: Context, date: LocalDate): List<UsageSessionRecord>? {
        val file = fileFor(context, date)
        if (!file.exists()) return null
        return try {
            val array = JSONArray(file.readText())
            (0 until array.length()).map { i ->
                val e = array.getJSONArray(i)
                UsageSessionRecord(e.getString(0), e.getLong(1), e.getLong(2))
            }
        } catch (e: Exception) {
            null
        }
    }

    /** Saves the last 8 days from Android. Call from a background thread. */
    fun syncRecent(context: Context) {
        purgeExpiredTrash(context)
        val today = LocalDate.now()
        for (back in 0..7) {
            val day = today.minusDays(back.toLong())
            val snapshot = UsageRepository.loadDay(context, day)
            saveDay(context, day, snapshot.sessions)
        }
    }

    /**
     * For days in the range that Android no longer has, returns the saved copy instead.
     * Only whole past days are filled in. Today always comes straight from Android.
     */
    fun sessionsForMissingDays(
        context: Context,
        start: Long,
        end: Long,
        live: List<UsageSessionRecord>,
        ignored: Set<String>
    ): List<UsageSessionRecord> {
        if (end <= start) return emptyList()

        val zone = ZoneId.systemDefault()
        val today = LocalDate.now()
        val liveDays = live
            .map { Instant.ofEpochMilli(it.startMillis).atZone(zone).toLocalDate() }
            .toSet()

        var day = Instant.ofEpochMilli(start).atZone(zone).toLocalDate()
        val lastDay = Instant.ofEpochMilli(end - 1).atZone(zone).toLocalDate()
        val extra = ArrayList<UsageSessionRecord>()

        while (!day.isAfter(lastDay)) {
            if (day.isBefore(today) && day !in liveDays) {
                readDay(context, day)?.forEach { s ->
                    val from = maxOf(s.startMillis, start)
                    val to = minOf(s.endMillis, end)
                    if (to - from >= 1_000L && s.packageName !in ignored) {
                        extra.add(UsageSessionRecord(s.packageName, from, to))
                    }
                }
            }
            day = day.plusDays(1)
        }
        return extra
    }

    // ---------- Info ----------

    /** How many days are saved right now. */
    @Synchronized
    fun storedDayCount(context: Context): Int =
        liveDir(context).listFiles()?.size ?: 0

    // ---------- Delete and recover ----------

    /** Moves all saved days to the trash. Returns how many days were moved. */
    @Synchronized
    fun deleteAll(context: Context): Int {
        val live = liveDir(context)
        val count = live.listFiles()?.size ?: 0
        if (count == 0) return 0

        trashDir(context).deleteRecursively()
        if (!live.renameTo(trashDir(context))) {
            // Fallback if the folder couldn't be moved: copy, then remove
            live.copyRecursively(trashDir(context), overwrite = true)
            live.deleteRecursively()
        }
        prefs(context).edit().putLong(KEY_DELETED_AT, System.currentTimeMillis()).apply()
        return count
    }

    /** How many deleted days can still be restored. 0 if none, or if 30 days have passed. */
    @Synchronized
    fun recoverableDayCount(context: Context): Int {
        val deletedAt = prefs(context).getLong(KEY_DELETED_AT, 0L)
        if (deletedAt == 0L) return 0
        if (System.currentTimeMillis() - deletedAt > RECOVERY_DAYS * DAY_MILLIS) return 0
        return trashDir(context).listFiles()?.size ?: 0
    }

    /** How many days are left to restore deleted data. */
    @Synchronized
    fun recoveryDaysLeft(context: Context): Int {
        val deletedAt = prefs(context).getLong(KEY_DELETED_AT, 0L)
        if (deletedAt == 0L) return 0
        val passed = ((System.currentTimeMillis() - deletedAt) / DAY_MILLIS).toInt()
        return (RECOVERY_DAYS - passed).coerceIn(0, RECOVERY_DAYS)
    }

    /** Puts deleted days back. Days that were saved again in the meantime are kept as they are. */
    @Synchronized
    fun restoreDeleted(context: Context): Int {
        if (recoverableDayCount(context) == 0) return 0

        val live = liveDir(context)
        live.mkdirs()
        var restored = 0
        trashDir(context).listFiles()?.forEach { file ->
            val target = File(live, file.name)
            if (!target.exists()) {
                file.copyTo(target)
                restored++
            }
        }
        trashDir(context).deleteRecursively()
        prefs(context).edit().remove(KEY_DELETED_AT).apply()
        return restored
    }

    /** Erases the trash for good once the 30 days are over. */
    @Synchronized
    private fun purgeExpiredTrash(context: Context) {
        val deletedAt = prefs(context).getLong(KEY_DELETED_AT, 0L)
        if (deletedAt != 0L &&
            System.currentTimeMillis() - deletedAt > RECOVERY_DAYS * DAY_MILLIS
        ) {
            trashDir(context).deleteRecursively()
            prefs(context).edit().remove(KEY_DELETED_AT).apply()
        }
    }
}