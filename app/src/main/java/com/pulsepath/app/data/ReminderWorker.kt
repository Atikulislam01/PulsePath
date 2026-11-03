package com.pulsepath.app.data

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.time.LocalTime
import java.util.concurrent.TimeUnit

/** Opening the same app this many times in 2 hours triggers a reminder. Change it if you like. */
const val REMINDER_LAUNCH_THRESHOLD = 10

private const val MIN_GAP_MILLIS = 3L * 3_600_000L   // at most one reminder per 3 hours
private const val TWO_HOURS_MILLIS = 2L * 3_600_000L

/** Runs in the background about every 15 minutes. */
class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val ctx = applicationContext
        try {
            if (hasUsageAccess(ctx) && SettingsPrefs.isMonitoringEnabled(ctx)) {
                // Keep PulsePath's own history up to date, even if the app wasn't opened
                HistoryStore.syncRecent(ctx)

                if (SettingsPrefs.areRemindersEnabled(ctx)) {
                    checkAndRemind(ctx)
                }
            }
        } catch (e: Exception) {
            // A background task must never crash the app
        }
        return Result.success()
    }

    private fun checkAndRemind(ctx: Context) {
        // No reminders at night
        val hour = LocalTime.now().hour
        if (hour >= 22 || hour < 7) return

        val now = System.currentTimeMillis()
        if (now - SettingsPrefs.lastReminderAt(ctx) < MIN_GAP_MILLIS) return

        val recent = UsageRepository.loadRange(ctx, now - TWO_HOURS_MILLIS, now)
        val top = recent.apps.maxByOrNull { it.launches } ?: return
        if (top.launches < REMINDER_LAUNCH_THRESHOLD) return

        val sent = showReminder(
            ctx,
            title = "Time for a short break?",
            text = "You've opened ${top.appName} ${top.launches} times in the last 2 hours."
        )
        if (sent) SettingsPrefs.setLastReminderAt(ctx, now)
    }
}

/** Starts and stops the background task. */
object ReminderScheduler {
    private const val WORK_NAME = "pulsepath_background"

    /** Call this whenever the Monitoring switch might have changed. */
    fun sync(context: Context) {
        val manager = WorkManager.getInstance(context)
        if (SettingsPrefs.isMonitoringEnabled(context)) {
            val request = PeriodicWorkRequestBuilder<ReminderWorker>(15, TimeUnit.MINUTES).build()
            manager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        } else {
            manager.cancelUniqueWork(WORK_NAME)
        }
    }
}