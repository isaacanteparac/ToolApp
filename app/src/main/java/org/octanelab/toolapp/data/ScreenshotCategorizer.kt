package org.octanelab.toolapp.data

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

object ScreenshotCategorizer {

    /**
     * Identify the foreground application active on screen right around the moment of screenshot capture.
     */
    fun detectForegroundAppName(context: Context): String {
        val packageName = getForegroundPackageName(context) ?: return "Uncategorized"
        return getAppNameFromPackage(context, packageName)
    }

    private fun getForegroundPackageName(context: Context): String? {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return null

        val endTime = System.currentTimeMillis()
        val startTime = endTime - 10000 // Query events in the last 10 seconds

        val events = usageStatsManager.queryEvents(startTime, endTime)
        var lastForegroundPackage: String? = null

        val event = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val isForeground = event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND ||
                    (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && event.eventType == UsageEvents.Event.ACTIVITY_RESUMED)
            if (isForeground) {
                // Avoid picking our own app if possible when detecting background active apps
                if (event.packageName != context.packageName) {
                    lastForegroundPackage = event.packageName
                } else if (lastForegroundPackage == null) {
                    lastForegroundPackage = event.packageName
                }
            }
        }
        return lastForegroundPackage
    }

    fun getAppNameFromPackage(context: Context, packageName: String): String {
        return try {
            val pm = context.packageManager
            val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getApplicationInfo(packageName, 0)
            }
            val label = pm.getApplicationLabel(appInfo).toString()
            sanitizeFolderName(label)
        } catch (e: Exception) {
            // Fallback: format package name cleanly if package info not found
            val lastPart = packageName.substringAfterLast('.')
            if (lastPart.isNotBlank()) sanitizeFolderName(lastPart.capitalizeWords()) else "Uncategorized"
        }
    }

    private fun sanitizeFolderName(name: String): String {
        val clean = name.replace(Regex("[\\\\/:*?\"<>|]"), "").trim()
        return clean.ifBlank { "Uncategorized" }
    }

    private fun String.capitalizeWords(): String {
        return this.split(" ")
            .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }
    }
}
