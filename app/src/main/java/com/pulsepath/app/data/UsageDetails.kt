package com.pulsepath.app.data

import android.content.Context
import android.content.pm.PackageManager
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

enum class DetailPeriod { DAY, WEEK, MONTH }

/** One bar in the app detail chart. */
data class ChartBar(val label: String, val millis: Long)

data class AppDetailData(
    val totalMillis: Long,
    val launches: Int,
    val bars: List<ChartBar>,
    val labelIndices: Set<Int>,       // which bars get a text label underneath
    val sessions: List<UsageSessionRecord>
)

// ---------- Formatting helpers ----------

private val clockFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
private val shortDateFormatter = DateTimeFormatter.ofPattern("EEE d MMM", Locale.ENGLISH)
private val monthLabelFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH)

/** 1730000000000 becomes "06:12 AM". */
fun formatClock(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).format(clockFormatter)

/** 1730000000000 becomes "Fri 1 Nov". */
fun formatShortDate(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).format(shortDateFormatter)

/** The name of an app, e.g. "com.google.android.youtube" becomes "YouTube". */
fun appLabel(context: Context, packageName: String): String =
    try {
        val pm = context.packageManager
        pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
    } catch (e: PackageManager.NameNotFoundException) {
        packageName
    }

private fun hourLabel(hour: Int): String {
    val h = if (hour % 12 == 0) 12 else hour % 12
    return "$h${if (hour < 12) "am" else "pm"}"
}

// ---------- Loading ----------

private class Buckets(
    val starts: List<Long>,
    val labels: List<String>,
    val labelIndices: Set<Int>
)

/** Usage of one app, split into bars. Call this from a background thread. */
fun loadAppDetail(context: Context, packageName: String, period: DetailPeriod): AppDetailData {
    val today = LocalDate.now()
    val zone = ZoneId.systemDefault()

    val buckets = when (period) {
        // 24 bars, one per hour of today
        DetailPeriod.DAY -> {
            val dayStart = today.atStartOfDay(zone)
            Buckets(
                starts = (0 until 24).map { dayStart.plusHours(it.toLong()).toInstant().toEpochMilli() },
                labels = (0 until 24).map { hourLabel(it) },
                labelIndices = setOf(0, 6, 12, 18)
            )
        }
        // 7 bars, one per day
        DetailPeriod.WEEK -> {
            val days = (6 downTo 0).map { today.minusDays(it.toLong()) }
            Buckets(
                starts = days.map { startOfDayMillis(it) },
                labels = days.map { it.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH) },
                labelIndices = (0..6).toSet()
            )
        }
        // 30 bars, one per day
        DetailPeriod.MONTH -> {
            val days = (29 downTo 0).map { today.minusDays(it.toLong()) }
            Buckets(
                starts = days.map { startOfDayMillis(it) },
                labels = days.map { it.format(monthLabelFormatter) },
                labelIndices = setOf(1, 8, 15, 22, 29)
            )
        }
    }

    val rangeStart = buckets.starts.first()
    val rangeEnd = startOfDayMillis(today.plusDays(1))
    val ends = buckets.starts.drop(1) + rangeEnd

    val mine = UsageRepository
        .loadRange(context, rangeStart, rangeEnd)
        .sessions
        .filter { it.packageName == packageName }

    val bars = buckets.starts.indices.map { i ->
        val from = buckets.starts[i]
        val to = ends[i]
        // Add up how much of each session falls inside this bar
        val millis = mine.sumOf { s ->
            maxOf(0L, minOf(s.endMillis, to) - maxOf(s.startMillis, from))
        }
        ChartBar(buckets.labels[i], millis)
    }

    return AppDetailData(
        totalMillis = mine.sumOf { it.durationMillis },
        launches = mine.size,
        bars = bars,
        labelIndices = buckets.labelIndices,
        sessions = mine.sortedByDescending { it.startMillis }.take(20)
    )
}