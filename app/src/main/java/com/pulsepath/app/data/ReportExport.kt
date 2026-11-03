package com.pulsepath.app.data

import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

// ---------- Report data ----------

/** All usage for the last [days] days, plus how many times you switched apps. */
data class ReportData(
    val days: Int,
    val snapshot: UsageSnapshot,
    val switches: Int
)

/** Call this from a background thread. days = 1 means "today". */
fun loadReport(context: Context, days: Int): ReportData {
    val today = LocalDate.now()
    val start = startOfDayMillis(today.minusDays((days - 1).toLong()))
    val end = startOfDayMillis(today.plusDays(1))
    val snapshot = UsageRepository.loadRange(context, start, end)

    // A "switch" is when the next session belongs to a different app
    var switches = 0
    snapshot.sessions.zipWithNext { a, b ->
        if (a.packageName != b.packageName) switches++
    }

    return ReportData(days = days, snapshot = snapshot, switches = switches)
}

// ---------- Export ----------

enum class ExportFormat(val extension: String, val mimeType: String) {
    CSV("csv", "text/csv"),
    JSON("json", "application/json")
}

/** Writes the report to a file in the app's cache folder. Call from a background thread. */
fun createExportFile(context: Context, report: ReportData, format: ExportFormat): File {
    val dir = File(context.cacheDir, "exports").apply { mkdirs() }
    dir.listFiles()?.forEach { it.delete() }   // remove old exports

    val file = File(dir, "pulsepath_${report.days}d_${LocalDate.now()}.${format.extension}")
    file.writeText(
        when (format) {
            ExportFormat.CSV -> buildCsv(report)
            ExportFormat.JSON -> buildJson(report)
        }
    )
    return file
}

/** Opens Android's share sheet so you can save the file or send it somewhere. */
fun shareExportFile(context: Context, file: File, format: ExportFormat) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val send = Intent(Intent.ACTION_SEND).apply {
        type = format.mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
        clipData = ClipData.newRawUri(file.name, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(send, "Export PulsePath data"))
}

private fun csvField(text: String): String = "\"" + text.replace("\"", "\"\"") + "\""

private fun buildCsv(report: ReportData): String {
    val apps = report.snapshot.apps.associateBy { it.packageName }
    val zone = ZoneId.systemDefault()
    val timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss")

    val sb = StringBuilder("date,start_time,end_time,duration_seconds,package_name,app_name,category\n")
    report.snapshot.sessions.forEach { s ->
        val start = Instant.ofEpochMilli(s.startMillis).atZone(zone)
        val end = Instant.ofEpochMilli(s.endMillis).atZone(zone)
        val app = apps[s.packageName]

        sb.append(start.format(DateTimeFormatter.ISO_LOCAL_DATE)).append(',')
            .append(start.format(timeFormat)).append(',')
            .append(end.format(timeFormat)).append(',')
            .append(s.durationMillis / 1000).append(',')
            .append(csvField(s.packageName)).append(',')
            .append(csvField(app?.appName ?: s.packageName)).append(',')
            .append(csvField(app?.category ?: CATEGORY_OTHER)).append('\n')
    }
    return sb.toString()
}

private fun buildJson(report: ReportData): String {
    val zone = ZoneId.systemDefault()
    val iso = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    val apps = report.snapshot.apps.associateBy { it.packageName }

    val summary = JSONObject()
        .put("total_screen_time_seconds", report.snapshot.totalMillis / 1000)
        .put("apps_used", report.snapshot.apps.size)
        .put("sessions", report.snapshot.sessions.size)
        .put("app_switches", report.switches)

    val appsJson = JSONArray()
    report.snapshot.apps.forEach { a ->
        appsJson.put(
            JSONObject()
                .put("package_name", a.packageName)
                .put("app_name", a.appName)
                .put("category", a.category)
                .put("total_seconds", a.totalMillis / 1000)
                .put("launches", a.launches)
        )
    }

    val sessionsJson = JSONArray()
    report.snapshot.sessions.forEach { s ->
        sessionsJson.put(
            JSONObject()
                .put("package_name", s.packageName)
                .put("app_name", apps[s.packageName]?.appName ?: s.packageName)
                .put("start", Instant.ofEpochMilli(s.startMillis).atZone(zone).format(iso))
                .put("end", Instant.ofEpochMilli(s.endMillis).atZone(zone).format(iso))
                .put("duration_seconds", s.durationMillis / 1000)
        )
    }

    return JSONObject()
        .put("generated_at", ZonedDateTime.now().format(iso))
        .put("period_days", report.days)
        .put("summary", summary)
        .put("apps", appsJson)
        .put("sessions", sessionsJson)
        .toString(2)
}