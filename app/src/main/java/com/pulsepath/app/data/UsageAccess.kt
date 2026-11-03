package com.pulsepath.app.data

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings

/** Returns true if the user has granted "Usage access" to this app. */
fun hasUsageAccess(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
    } else {
        @Suppress("DEPRECATION")
        appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
    }

    if (mode == AppOpsManager.MODE_ALLOWED) return true

    // Some phones report "default" and rely on the normal permission instead
    if (mode == AppOpsManager.MODE_DEFAULT &&
        context.checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) ==
        PackageManager.PERMISSION_GRANTED
    ) {
        return true
    }

    // Last test: try to really read the last day of usage
    return try {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val now = System.currentTimeMillis()
        usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, now - 24L * 3_600_000L, now)
            .isNotEmpty()
    } catch (e: Exception) {
        false
    }
}

/** Opens Android's "Usage access" settings page. */
fun openUsageAccessSettings(context: Context) {
    try {
        // Tries to jump straight to this app's entry
        val intent = Intent(
            Settings.ACTION_USAGE_ACCESS_SETTINGS,
            Uri.parse("package:${context.packageName}")
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback: the general list of apps
        context.startActivity(
            Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}