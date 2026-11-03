package com.pulsepath.app.data

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.pulsepath.app.MainActivity

private const val CHANNEL_ID = "pulsepath_reminders"

/** True if PulsePath is allowed to show notifications. */
fun hasNotificationAccess(context: Context): Boolean =
    NotificationManagerCompat.from(context).areNotificationsEnabled()

/** Opens this app's notification page in Android settings. */
fun openNotificationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}

/** Shows a notification. Returns false if notifications are blocked. */
@SuppressLint("MissingPermission")
fun showReminder(context: Context, title: String, text: String): Boolean {
    if (!hasNotificationAccess(context)) return false

    val channel = NotificationChannel(
        CHANNEL_ID,
        "Usage reminders",
        NotificationManager.IMPORTANCE_DEFAULT
    ).apply {
        description = "Gentle reminders about how you use your phone"
    }
    context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

    val openApp = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val pending = PendingIntent.getActivity(
        context,
        0,
        openApp,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_dialog_info)   // placeholder icon, replaced during polish
        .setContentTitle(title)
        .setContentText(text)
        .setStyle(NotificationCompat.BigTextStyle().bigText(text))
        .setContentIntent(pending)
        .setAutoCancel(true)
        .build()

    NotificationManagerCompat.from(context).notify(1001, notification)
    return true
}