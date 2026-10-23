package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.model.CrosshairConfig
import com.example.model.CrosshairShape
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CrosshairService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayView: CrosshairOverlayView? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var configJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())

        if (Settings.canDrawOverlays(this)) {
            initOverlay()
        } else {
            stopSelf()
        }

        configJob = serviceScope.launch {
            liveConfig.collect { config ->
                if (!config.isEnabled) {
                    stopSelf()
                } else {
                    updateOverlay(config)
                }
            }
        }
    }

    private fun initOverlay() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        overlayView = CrosshairOverlayView(this)

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val config = liveConfig.value
        val density = resources.displayMetrics.density
        val totalSizePx = ((config.sizeDp + 40) * density).toInt()

        val params = WindowManager.LayoutParams(
            totalSizePx,
            totalSizePx,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
            x = (config.offsetX * density).toInt()
            y = (config.offsetY * density).toInt()
        }

        try {
            windowManager?.addView(overlayView, params)
            overlayView?.setConfig(config)
            isOverlayRunning.value = true
        } catch (e: Exception) {
            isOverlayRunning.value = false
        }
    }

    private fun updateOverlay(config: CrosshairConfig) {
        val density = resources.displayMetrics.density
        val totalSizePx = ((config.sizeDp + 40) * density).toInt()

        val params = overlayView?.layoutParams as? WindowManager.LayoutParams
        if (params != null) {
            params.width = totalSizePx
            params.height = totalSizePx
            params.x = (config.offsetX * density).toInt()
            params.y = (config.offsetY * density).toInt()
            try {
                windowManager?.updateViewLayout(overlayView, params)
            } catch (e: Exception) {
                // View might not be attached
            }
        }
        overlayView?.setConfig(config)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            liveConfig.value = liveConfig.value.copy(isEnabled = false)
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    override fun onDestroy() {
        configJob?.cancel()
        overlayView?.let {
            try {
                windowManager?.removeView(it)
            } catch (e: Exception) {
                // Safe ignore
            }
        }
        isOverlayRunning.value = false
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "PASEO SENSI Aim Assistant",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifikasi status overlay crosshair aim assistant"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = Intent(this, CrosshairService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("PASEO SENSI — Aim Assistant Aktif")
            .setContentText("Crosshair kustom aktif melayang di layar game")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentIntent(openAppPendingIntent)
            .addAction(android.R.drawable.ic_delete, "Matikan Overlay", stopPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    companion object {
        const val CHANNEL_ID = "paseo_sensi_crosshair_channel"
        const val NOTIFICATION_ID = 2001
        const val ACTION_STOP = "com.zuccheroxann.paseosensi.ACTION_STOP_CROSSHAIR"

        val liveConfig = MutableStateFlow(CrosshairConfig(isEnabled = false))
        val isOverlayRunning = MutableStateFlow(false)

        fun start(context: Context, config: CrosshairConfig) {
            liveConfig.value = config.copy(isEnabled = true)
            val intent = Intent(context, CrosshairService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            isOverlayRunning.value = false
            liveConfig.value = liveConfig.value.copy(isEnabled = false)
            val intent = Intent(context, CrosshairService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}

class CrosshairOverlayView(context: Context) : View(context) {

    private var config: CrosshairConfig = CrosshairConfig()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        color = Color.BLACK
    }

    fun setConfig(newConfig: CrosshairConfig) {
        this.config = newConfig
        val colorInt = (newConfig.colorValue.toInt())
        val alphaInt = (newConfig.opacity * 255).toInt().coerceIn(0, 255)

        paint.color = Color.argb(
            alphaInt,
            Color.red(colorInt),
            Color.green(colorInt),
            Color.blue(colorInt)
        )
        dotPaint.color = paint.color
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val density = resources.displayMetrics.density
        val cx = width / 2f
        val cy = height / 2f

        val sizePx = (config.sizeDp * density) / 2f
        val thicknessPx = config.thicknessDp * density
        val gapPx = config.gapDp * density
        val dotRadiusPx = config.centerDotRadius * density

        paint.strokeWidth = thicknessPx
        outlinePaint.strokeWidth = thicknessPx + (1.5f * density)

        // Draw center dot if enabled
        if (config.showCenterDot) {
            canvas.drawCircle(cx, cy, dotRadiusPx + (1f * density), outlinePaint)
            canvas.drawCircle(cx, cy, dotRadiusPx, dotPaint)
        }

        when (config.shape) {
            CrosshairShape.CLASSIC_CROSS -> {
                // Top, Bottom, Left, Right bars
                drawBarWithOutline(canvas, cx, cy - gapPx - sizePx, cx, cy - gapPx)
                drawBarWithOutline(canvas, cx, cy + gapPx, cx, cy + gapPx + sizePx)
                drawBarWithOutline(canvas, cx - gapPx - sizePx, cy, cx - gapPx, cy)
                drawBarWithOutline(canvas, cx + gapPx, cy, cx + gapPx + sizePx, cy)
            }
            CrosshairShape.DOT -> {
                canvas.drawCircle(cx, cy, (sizePx / 2.5f) + (1.5f * density), outlinePaint)
                canvas.drawCircle(cx, cy, sizePx / 2.5f, dotPaint)
            }
            CrosshairShape.CIRCLE_CROSS -> {
                val circleRadius = sizePx * 0.7f
                canvas.drawCircle(cx, cy, circleRadius, outlinePaint)
                canvas.drawCircle(cx, cy, circleRadius, paint)

                drawBarWithOutline(canvas, cx, cy - circleRadius - sizePx * 0.4f, cx, cy - circleRadius + (2f * density))
                drawBarWithOutline(canvas, cx, cy + circleRadius - (2f * density), cx, cy + circleRadius + sizePx * 0.4f)
                drawBarWithOutline(canvas, cx - circleRadius - sizePx * 0.4f, cy, cx - circleRadius + (2f * density), cy)
                drawBarWithOutline(canvas, cx + circleRadius - (2f * density), cy, cx + circleRadius + sizePx * 0.4f, cy)
            }
            CrosshairShape.TACTICAL_T -> {
                // T shape: Left, Right, Bottom bars only (no top bar for clear headshot visibility)
                drawBarWithOutline(canvas, cx - gapPx - sizePx, cy, cx - gapPx, cy)
                drawBarWithOutline(canvas, cx + gapPx, cy, cx + gapPx + sizePx, cy)
                drawBarWithOutline(canvas, cx, cy + gapPx, cx, cy + gapPx + sizePx)
            }
            CrosshairShape.SNIPER_CIRCLE -> {
                val circleRadius = sizePx * 0.9f
                canvas.drawCircle(cx, cy, circleRadius, outlinePaint)
                canvas.drawCircle(cx, cy, circleRadius, paint)

                // Thin cross lines through
                paint.strokeWidth = thicknessPx * 0.7f
                canvas.drawLine(cx - circleRadius, cy, cx + circleRadius, cy, paint)
                canvas.drawLine(cx, cy - circleRadius, cx, cy + circleRadius, paint)
            }
            CrosshairShape.DIAMOND -> {
                val d = sizePx * 0.8f
                canvas.drawLine(cx, cy - d, cx + d, cy, outlinePaint)
                canvas.drawLine(cx + d, cy, cx, cy + d, outlinePaint)
                canvas.drawLine(cx, cy + d, cx - d, cy, outlinePaint)
                canvas.drawLine(cx - d, cy, cx, cy - d, outlinePaint)

                canvas.drawLine(cx, cy - d, cx + d, cy, paint)
                canvas.drawLine(cx + d, cy, cx, cy + d, paint)
                canvas.drawLine(cx, cy + d, cx - d, cy, paint)
                canvas.drawLine(cx - d, cy, cx, cy - d, paint)
            }
        }
    }

    private fun drawBarWithOutline(canvas: Canvas, startX: Float, startY: Float, stopX: Float, stopY: Float) {
        canvas.drawLine(startX, startY, stopX, stopY, outlinePaint)
        canvas.drawLine(startX, startY, stopX, stopY, paint)
    }
}
