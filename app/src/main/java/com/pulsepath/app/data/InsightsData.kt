package com.pulsepath.app.data

import android.content.Context
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

/** How much higher or lower "now" is than "before", in percent. null if there is nothing to compare. */
fun percentChange(now: Long, before: Long): Int? =
    if (before > 0L) (((now - before) * 100) / before).toInt() else null

// =====================================================================
// Behaviour Insights
// =====================================================================

data class InsightsData(
    val eveningSocialMillis: Long,   // social media, 6 pm to midnight, last 7 days
    val daySocialMillis: Long,       // social media, 6 am to 6 pm, last 7 days
    val recentPackage: String?,      // most opened app in the last 2 hours
    val recentAppName: String?,
    val recentLaunches: Int,
    val avgSessionMillis: Long?,     // average session today
    val avgChangePercent: Int?       // compared with yesterday
)

/** Cuts a session into pieces that each sit inside one clock hour. */
private fun forEachHourPiece(
    session: UsageSessionRecord,
    onPiece: (hour: Int, millis: Long) -> Unit
) {
    val zone = ZoneId.systemDefault()
    var cursor = session.startMillis
    while (cursor < session.endMillis) {
        val moment = Instant.ofEpochMilli(cursor).atZone(zone)
        val nextHour = moment.truncatedTo(ChronoUnit.HOURS).plusHours(1).toInstant().toEpochMilli()
        val pieceEnd = minOf(nextHour, session.endMillis)
        onPiece(moment.hour, pieceEnd - cursor)
        cursor = pieceEnd
    }
}

private fun averageSessionMillis(snapshot: UsageSnapshot): Long? {
    val list = snapshot.sessions.filter { it.durationMillis >= 10_000L }
    return if (list.isEmpty()) null else list.sumOf { it.durationMillis } / list.size
}

/** Call this from a background thread. */
fun loadInsights(context: Context): InsightsData {
    val today = LocalDate.now()

    // Social media in the evening versus during the day
    val week = UsageRepository.loadRange(
        context,
        startOfDayMillis(today.minusDays(6)),
        startOfDayMillis(today.plusDays(1))
    )
    val socialPackages = week.apps
        .filter { it.category == CATEGORY_SOCIAL }
        .map { it.packageName }
        .toSet()

    var evening = 0L
    var daytime = 0L
    week.sessions.filter { it.packageName in socialPackages }.forEach { session ->
        forEachHourPiece(session) { hour, millis ->
            when (hour) {
                in 18..23 -> evening += millis
                in 6..17 -> daytime += millis
            }
        }
    }

    // Most opened app in the last 2 hours
    val now = System.currentTimeMillis()
    val recent = UsageRepository.loadRange(context, now - 2 * 3_600_000L, now)
    val topRecent = recent.apps.maxByOrNull { it.launches }

    // Average session today and yesterday
    val avgToday = averageSessionMillis(UsageRepository.loadDay(context, today))
    val avgYesterday = averageSessionMillis(UsageRepository.loadDay(context, today.minusDays(1)))
    val avgChange = if (avgToday != null && avgYesterday != null) {
        percentChange(avgToday, avgYesterday)
    } else {
        null
    }

    return InsightsData(
        eveningSocialMillis = evening,
        daySocialMillis = daytime,
        recentPackage = topRecent?.packageName,
        recentAppName = topRecent?.appName,
        recentLaunches = topRecent?.launches ?: 0,
        avgSessionMillis = avgToday,
        avgChangePercent = avgChange
    )
}

// =====================================================================
// App switching
// =====================================================================

data class AppPairCount(
    val firstPackage: String,
    val firstName: String,
    val secondPackage: String,
    val secondName: String,
    val times: Int
)

data class SwitchingData(
    val todaySwitches: Int,
    val changePercent: Int?,          // today so far versus yesterday up to the same time
    val dailySwitches: List<Int>,     // last 7 days, oldest first
    val pairs: List<AppPairCount>     // top 5 pairs in the last 7 days
)

/** Call this from a background thread. */
fun loadSwitching(context: Context): SwitchingData {
    val zone = ZoneId.systemDefault()
    val today = LocalDate.now()
    val yesterday = today.minusDays(1)
    val days = (6 downTo 0).map { today.minusDays(it.toLong()) }

    val snapshot = UsageRepository.loadRange(
        context,
        startOfDayMillis(days.first()),
        startOfDayMillis(today.plusDays(1))
    )
    val names = snapshot.apps.associate { it.packageName to it.appName }

    val perDay = HashMap<LocalDate, Int>()
    val pairCounts = HashMap<Pair<String, String>, Int>()
    var yesterdaySoFar = 0
    val yesterdayCutoff = System.currentTimeMillis() - 24 * 3_600_000L

    snapshot.sessions.zipWithNext { a, b ->
        if (a.packageName != b.packageName) {
            val day = Instant.ofEpochMilli(b.startMillis).atZone(zone).toLocalDate()
            perDay[day] = (perDay[day] ?: 0) + 1
            if (day == yesterday && b.startMillis <= yesterdayCutoff) yesterdaySoFar++

            // "YouTube + WhatsApp" and "WhatsApp + YouTube" count as the same pair
            val key = if (a.packageName < b.packageName) {
                a.packageName to b.packageName
            } else {
                b.packageName to a.packageName
            }
            pairCounts[key] = (pairCounts[key] ?: 0) + 1
        }
    }

    val dailySwitches = days.map { perDay[it] ?: 0 }
    val todaySwitches = dailySwitches.last()

    val pairs = pairCounts.entries
        .sortedByDescending { it.value }
        .take(5)
        .map { (key, times) ->
            AppPairCount(
                firstPackage = key.first,
                firstName = names[key.first] ?: key.first,
                secondPackage = key.second,
                secondName = names[key.second] ?: key.second,
                times = times
            )
        }

    return SwitchingData(
        todaySwitches = todaySwitches,
        changePercent = percentChange(todaySwitches.toLong(), yesterdaySoFar.toLong()),
        dailySwitches = dailySwitches,
        pairs = pairs
    )
}

// =====================================================================
// Weekly trends
// =====================================================================

data class WeeklyData(
    val thisWeekMillis: List<Long?>,   // Mon to Sun. null = a day that hasn't happened yet
    val lastWeekMillis: List<Long>,
    val thisWeekOpens: List<Int?>,
    val lastWeekOpens: List<Int>,
    val daysSoFar: Int,                // how many days of this week are done or in progress
    val lastWeekToDateMillis: Long,    // last week, up to the same moment of the week
    val lastWeekToDateOpens: Int
)

/** Call this from a background thread. */
fun loadWeekly(context: Context): WeeklyData {
    val today = LocalDate.now()
    val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val lastMonday = monday.minusWeeks(1)
    val todayIndex = today.dayOfWeek.value - 1      // Monday = 0 ... Sunday = 6

    val thisSnapshots = (0..6).map { i ->
        val day = monday.plusDays(i.toLong())
        if (day.isAfter(today)) null else UsageRepository.loadDay(context, day)
    }
    val lastSnapshots = (0..6).map { i ->
        UsageRepository.loadDay(context, lastMonday.plusDays(i.toLong()))
    }

    // Last week up to this same time of day, so today's partial total is compared fairly
    val sameDayLastWeekStart = startOfDayMillis(lastMonday.plusDays(todayIndex.toLong()))
    val sameMomentLastWeek = ZonedDateTime.now().minusDays(7).toInstant().toEpochMilli()
    val lastPartial = UsageRepository.loadRange(context, sameDayLastWeekStart, sameMomentLastWeek)
    val lastFullDays = lastSnapshots.take(todayIndex)

    return WeeklyData(
        thisWeekMillis = thisSnapshots.map { it?.totalMillis },
        lastWeekMillis = lastSnapshots.map { it.totalMillis },
        thisWeekOpens = thisSnapshots.map { it?.sessions?.size },
        lastWeekOpens = lastSnapshots.map { it.sessions.size },
        daysSoFar = todayIndex + 1,
        lastWeekToDateMillis = lastFullDays.sumOf { it.totalMillis } + lastPartial.totalMillis,
        lastWeekToDateOpens = lastFullDays.sumOf { it.sessions.size } + lastPartial.sessions.size
    )
}