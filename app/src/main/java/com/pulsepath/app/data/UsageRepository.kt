package com.pulsepath.app.data

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.TextStyle
import java.util.Locale

// ---------- Data classes ----------

/** One stretch of time when an app was on screen. */
data class UsageSessionRecord(
    val packageName: String,
    val startMillis: Long,
    val endMillis: Long
) {
    val durationMillis: Long get() = endMillis - startMillis
}

/** Total usage of one app. "launches" counts how many times it was opened. */
data class AppUsageSummary(
    val packageName: String,
    val appName: String,
    val category: String,
    val totalMillis: Long,
    val launches: Int
)

/** Everything we learned about a time range. */
data class UsageSnapshot(
    val totalMillis: Long,
    val apps: List<AppUsageSummary>,
    val sessions: List<UsageSessionRecord>
)

data class CategoryTotal(val name: String, val millis: Long)

/** Everything the Home screen needs. */
data class HomeData(
    val todayMillis: Long,
    val changePercent: Int?,          // null = no data from last week to compare
    val weekMillis: List<Long>,       // 7 days, oldest first, today last
    val weekLabels: List<String>,     // "Mon", "Tue", ...
    val categories: List<CategoryTotal>
)

// ---------- Category names ----------

const val CATEGORY_SOCIAL = "Social media"
const val CATEGORY_MESSAGING = "Messaging"
const val CATEGORY_VIDEO = "Video streaming"
const val CATEGORY_GAMING = "Gaming"
const val CATEGORY_PRODUCTIVITY = "Productivity"
const val CATEGORY_EDUCATION = "Education"
const val CATEGORY_OTHER = "Other"

// ---------- Helpers ----------

/** 16_200_000 ms becomes "4h 30m". */
fun formatDuration(millis: Long): String {
    if (millis <= 0L) return "0m"
    val totalMinutes = millis / 60_000L
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        totalMinutes > 0 -> "${minutes}m"
        else -> "<1m"
    }
}

fun startOfDayMillis(date: LocalDate): Long =
    date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

// ---------- The repository ----------

object UsageRepository {

    private const val MIN_SESSION_MILLIS = 1_000L

    /** Usage for one calendar day. */
    fun loadDay(context: Context, date: LocalDate): UsageSnapshot =
        loadRange(context, startOfDayMillis(date), startOfDayMillis(date.plusDays(1)))

    /** Usage between two moments (in milliseconds since 1970). */
    fun loadRange(context: Context, startMillis: Long, endMillis: Long): UsageSnapshot {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val pm = context.packageManager
        val ignored = ignoredPackages(context)

        // What Android still remembers, plus saved days that Android has already forgotten
        val live = readSessions(usm, startMillis, endMillis, ignored)
        val rawSessions = live +
                HistoryStore.sessionsForMissingDays(context, startMillis, endMillis, live, ignored)

        val apps = rawSessions
            .groupBy { it.packageName }
            .mapNotNull { (pkg, list) ->
                val info = try {
                    pm.getApplicationInfo(pkg, 0)
                } catch (e: PackageManager.NameNotFoundException) {
                    return@mapNotNull null
                }
                AppUsageSummary(
                    packageName = pkg,
                    appName = pm.getApplicationLabel(info).toString(),
                    category = categoryFor(pkg, info),
                    totalMillis = list.sumOf { it.durationMillis },
                    launches = list.size
                )
            }
            .sortedByDescending { it.totalMillis }

        val visible = apps.map { it.packageName }.toSet()
        val sessions = rawSessions
            .filter { it.packageName in visible }
            .sortedBy { it.startMillis }

        return UsageSnapshot(
            totalMillis = apps.sumOf { it.totalMillis },
            apps = apps,
            sessions = sessions
        )
    }

    /** Everything the Home screen shows. Call this from a background thread. */
    fun loadHomeData(context: Context): HomeData {
        val today = LocalDate.now()
        val todaySnapshot = loadDay(context, today)

        val weekDays = (6 downTo 0).map { today.minusDays(it.toLong()) }
        val weekMillis = weekDays.map { day ->
            if (day == today) todaySnapshot.totalMillis else loadDay(context, day).totalMillis
        }
        val weekLabels = weekDays.map {
            it.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
        }

        // Compare "today so far" with the same weekday last week up to the same time
        val lastWeekStart = startOfDayMillis(today.minusDays(7))
        val lastWeekNow = ZonedDateTime.now().minusDays(7).toInstant().toEpochMilli()
        val lastWeekMillis = loadRange(context, lastWeekStart, lastWeekNow).totalMillis

        val change = if (lastWeekMillis > 0L) {
            (((todaySnapshot.totalMillis - lastWeekMillis) * 100) / lastWeekMillis).toInt()
        } else {
            null
        }

        val categories = todaySnapshot.apps
            .groupBy { it.category }
            .map { (name, list) -> CategoryTotal(name, list.sumOf { it.totalMillis }) }
            .sortedByDescending { it.millis }
            .take(3)

        return HomeData(
            todayMillis = todaySnapshot.totalMillis,
            changePercent = change,
            weekMillis = weekMillis,
            weekLabels = weekLabels,
            categories = categories
        )
    }

    /** Guesses a category from the package name, then from Android's own category. */
    fun categoryFor(pkg: String, info: ApplicationInfo): String {
        val p = pkg.lowercase()
        return when {
            listOf("whatsapp", "telegram", "orca", "messaging", "signal", "viber").any { p.contains(it) } ->
                CATEGORY_MESSAGING

            listOf(
                "instagram", "facebook", "musically", "tiktok", "snapchat",
                "twitter", "reddit", "pinterest", "linkedin"
            ).any { p.contains(it) } -> CATEGORY_SOCIAL

            listOf("youtube", "netflix", "hotstar", "primevideo", "twitch").any { p.contains(it) } ->
                CATEGORY_VIDEO

            listOf(
                "duolingo", "khanacademy", "coursera", "udemy", "byjus",
                "unacademy", "classroom", "edx"
            ).any { p.contains(it) } -> CATEGORY_EDUCATION

            else -> when (info.category) {
                ApplicationInfo.CATEGORY_SOCIAL -> CATEGORY_SOCIAL
                ApplicationInfo.CATEGORY_VIDEO -> CATEGORY_VIDEO
                ApplicationInfo.CATEGORY_GAME -> CATEGORY_GAMING
                ApplicationInfo.CATEGORY_PRODUCTIVITY -> CATEGORY_PRODUCTIVITY
                else -> CATEGORY_OTHER
            }
        }
    }

    // ---------- Private helpers ----------

    /** Turns Android's list of "app came to front" and "app went away" events into sessions. */
    @Suppress("DEPRECATION")
    private fun readSessions(
        usm: UsageStatsManager,
        start: Long,
        end: Long,
        ignored: Set<String>
    ): List<UsageSessionRecord> {
        val events = usm.queryEvents(start, end)
        val event = UsageEvents.Event()
        val open = HashMap<String, Long>()
        val seen = HashSet<String>()
        val result = ArrayList<UsageSessionRecord>()

        fun add(pkg: String, from: Long, to: Long) {
            if (to - from >= MIN_SESSION_MILLIS) {
                result.add(UsageSessionRecord(pkg, from, to))
            }
        }

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val pkg = event.packageName ?: continue
            if (pkg in ignored) continue
            val time = event.timeStamp

            when (event.eventType) {
                UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                    seen.add(pkg)
                    if (!open.containsKey(pkg)) open[pkg] = time
                }
                UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                    val startedAt = open.remove(pkg)
                    if (startedAt != null) {
                        add(pkg, startedAt, time)
                    } else if (seen.add(pkg)) {
                        // The app was already open when this range began
                        add(pkg, start, time)
                    }
                }
            }
        }

        // Apps still open at the end of the range
        val closeAt = minOf(end, System.currentTimeMillis())
        open.forEach { (pkg, startedAt) -> add(pkg, startedAt, closeAt) }

        return result
    }

    /** Apps we don't want to count: PulsePath itself, the home screen, system UI. */
    private fun ignoredPackages(context: Context): Set<String> {
        val pm = context.packageManager
        val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val launchers = pm.queryIntentActivities(homeIntent, 0).map { it.activityInfo.packageName }
        return (launchers + context.packageName + "com.android.systemui").toSet()
    }
}