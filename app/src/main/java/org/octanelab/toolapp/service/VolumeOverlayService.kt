package org.octanelab.toolapp.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.app.NotificationCompat
import org.octanelab.toolapp.R
import kotlin.math.abs

class VolumeOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var audioManager: AudioManager

    private var handleView: View? = null
    private var feedbackView: View? = null

    private val feedbackHandler = Handler(Looper.getMainLooper())
    private var hideFeedbackRunnable: Runnable? = null

    companion object {
        const val ACTION_START = "ACTION_START_VOLUME_OVERLAY"
        const val ACTION_STOP = "ACTION_STOP_VOLUME_OVERLAY"
        private const val CHANNEL_ID = "volume_overlay_service_channel"
        private const val NOTIFICATION_ID = 1001
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        startForegroundServiceNotification()
        showFloatingHandle()
        return START_STICKY
    }

    private fun startForegroundServiceNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Volume Overlay Controller",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Running floating volume gesture overlay"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Volume Edge Controller Active")
            .setContentText("Swipe edge up/down to adjust media volume")
            .setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun showFloatingHandle() {
        if (handleView != null) return

        // Create elegant floating edge handle strip
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#990F172A")) // Dark glassmorphic background
                setStroke(3, Color.parseColor("#64748B")) // Subtle border
                cornerRadius = 24f
            }
        }

        // Add subtle grip lines inside handle
        val gripLine = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(12, 80).apply {
                topMargin = 12
                bottomMargin = 12
            }
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#94A3B8"))
                cornerRadius = 6f
            }
        }
        container.addView(gripLine)

        val params = WindowManager.LayoutParams(
            48, // width in pixels
            360, // height in pixels
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.END or Gravity.CENTER_VERTICAL
            x = 0
            y = 0
        }

        var startY = 0f
        var touchAccumulator = 0f
        val swipeThreshold = 35f // touch sensitivity in pixels

        container.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startY = event.rawY
                    touchAccumulator = 0f
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaY = startY - event.rawY // UP is positive, DOWN is negative
                    touchAccumulator += deltaY
                    startY = event.rawY

                    if (abs(touchAccumulator) >= swipeThreshold) {
                        if (touchAccumulator > 0) {
                            adjustVolume(1) // Swipe UP -> Increase
                        } else {
                            adjustVolume(-1) // Swipe DOWN -> Decrease
                        }
                        touchAccumulator = 0f
                    }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    true
                }
                else -> false
            }
        }

        handleView = container
        try {
            windowManager.addView(handleView, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun adjustVolume(direction: Int) {
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val newVolume = (currentVolume + direction).coerceIn(0, maxVolume)

        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            newVolume,
            0 // Don't show system dialog, use our custom overlay
        )

        showVolumeFeedback(newVolume, maxVolume)
    }

    private fun showVolumeFeedback(current: Int, max: Int) {
        val percentage = ((current.toFloat() / max.toFloat()) * 100).toInt()

        if (feedbackView == null) {
            val layout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(32, 24, 32, 24)
                background = GradientDrawable().apply {
                    setColor(Color.parseColor("#F10F172A")) // Sleek glass dark
                    cornerRadius = 32f
                    setStroke(2, Color.parseColor("#38BDF8")) // Cyan accent border
                }
            }

            val iconView = ImageView(this).apply {
                setImageResource(android.R.drawable.ic_lock_silent_mode_off)
                setColorFilter(Color.parseColor("#38BDF8"))
                layoutParams = LinearLayout.LayoutParams(48, 48).apply {
                    marginEnd = 24
                }
            }

            val progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
                setMax(100)
                setProgress(percentage)
                layoutParams = LinearLayout.LayoutParams(260, 20).apply {
                    marginEnd = 24
                }
            }

            val textView = TextView(this).apply {
                text = "$percentage%"
                setTextColor(Color.WHITE)
                textSize = 14f
                tag = "volume_text"
            }

            layout.addView(iconView)
            layout.addView(progressBar)
            layout.addView(textView)

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                y = 140
            }

            feedbackView = layout
            try {
                windowManager.addView(feedbackView, params)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            val layout = feedbackView as? LinearLayout
            val bar = layout?.getChildAt(1) as? ProgressBar
            val text = layout?.findViewWithTag<TextView>("volume_text")
            bar?.setProgress(percentage)
            text?.text = "$percentage%"
            feedbackView?.visibility = View.VISIBLE
        }

        // Auto-hide feedback card after 1.5 seconds
        hideFeedbackRunnable?.let { feedbackHandler.removeCallbacks(it) }
        hideFeedbackRunnable = Runnable {
            feedbackView?.visibility = View.GONE
        }
        feedbackHandler.postDelayed(hideFeedbackRunnable!!, 1500)
    }

    override fun onDestroy() {
        super.onDestroy()
        hideFeedbackRunnable?.let { feedbackHandler.removeCallbacks(it) }
        handleView?.let {
            try { windowManager.removeView(it) } catch (e: Exception) { e.printStackTrace() }
        }
        feedbackView?.let {
            try { windowManager.removeView(it) } catch (e: Exception) { e.printStackTrace() }
        }
    }
}
