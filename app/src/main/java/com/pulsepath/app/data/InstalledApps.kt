package com.pulsepath.app.data

import android.content.Context
import android.content.Intent

data class InstalledApp(
    val packageName: String,
    val name: String,
    val category: String
)

/** Every app on the phone that has a launcher icon, with its guessed category. Call from a background thread. */
fun loadInstalledApps(context: Context): List<InstalledApp> {
    val pm = context.packageManager
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)

    return pm.queryIntentActivities(intent, 0)
        .map { it.activityInfo.applicationInfo }
        .distinctBy { it.packageName }
        .filter { it.packageName != context.packageName }
        .map { info ->
            InstalledApp(
                packageName = info.packageName,
                name = pm.getApplicationLabel(info).toString(),
                category = UsageRepository.categoryFor(info.packageName, info)
            )
        }
        .sortedBy { it.name.lowercase() }
}