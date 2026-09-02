package org.octanelab.toolapp.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.MediaStore
import androidx.core.app.NotificationCompat
import org.octanelab.toolapp.data.MediaRepository
import org.octanelab.toolapp.data.ScreenshotCategorizer
import java.util.concurrent.Executors

class ScreenshotObserverService : Service() {

    private lateinit var contentObserver: ContentObserver
    private val executor = Executors.newSingleThreadExecutor()
    private val processedUris = HashSet<Uri>()
    private var lastProcessedTimestamp = 0L

    companion object {
        const val ACTION_START = "ACTION_START_SCREENSHOT_OBSERVER"
        const val ACTION_STOP = "ACTION_STOP_SCREENSHOT_OBSERVER"
        private const val CHANNEL_ID = "screenshot_observer_service_channel"
        private const val NOTIFICATION_ID = 1002
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        registerScreenshotObserver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        startForegroundServiceNotification()
        return START_STICKY
    }

    private fun startForegroundServiceNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Screenshot Categorizer Monitor",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitors newly captured screenshots for auto-categorization"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Auto-Categorize Screenshots Active")
            .setContentText("Detecting active app on capture and organizing into subfolders")
            .setSmallIcon(android.R.drawable.ic_menu_gallery)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun registerScreenshotObserver() {
        contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                super.onChange(selfChange, uri)
                executor.execute {
                    handleContentChange(uri)
                }
            }
        }

        contentResolver.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            true,
            contentObserver
        )
    }

    private fun handleContentChange(targetUri: Uri?) {
        val now = System.currentTimeMillis()
        // Throttle rapid consecutive events within 1 second for same trigger
        if (now - lastProcessedTimestamp < 800) return

        val recentScreenshots = MediaRepository.fetchScreenshots(this)
        if (recentScreenshots.isEmpty()) return

        // Get the latest screenshot taken in the last 15 seconds
        val latest = recentScreenshots.first()
        val imageAgeSeconds = (now / 1000) - latest.dateAdded

        if (imageAgeSeconds > 20) return // Skip old photos
        if (processedUris.contains(latest.uri)) return // Skip already processed

        // If the screenshot is ALREADY inside a specific app subfolder (not General/Uncategorized root), skip
        if (latest.category != "General" && latest.category != "Uncategorized" && latest.category.isNotBlank()) {
            processedUris.add(latest.uri)
            return
        }

        // Detect current active app foreground package
        val detectedAppName = ScreenshotCategorizer.detectForegroundAppName(this)
        if (detectedAppName == "Uncategorized" || detectedAppName.isBlank() || detectedAppName == "ToolApp") {
            processedUris.add(latest.uri)
            return
        }

        lastProcessedTimestamp = now
        processedUris.add(latest.uri)

        // Automatically organize screenshot into app subfolder
        val newUri = MediaRepository.categorizeAndMoveScreenshot(this, latest.uri, detectedAppName)
        if (newUri != null) {
            processedUris.add(newUri)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            contentResolver.unregisterContentObserver(contentObserver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        executor.shutdown()
    }
}
