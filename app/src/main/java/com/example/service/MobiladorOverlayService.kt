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
import android.graphics.RectF
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.model.mobilador.MappingElement
import com.example.model.mobilador.MappingElementType
import com.example.model.mobilador.MobiladorProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.math.hypot

class MobiladorOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var mappingOverlayView: MappingCanvasOverlayView? = null
    private var controlPillView: View? = null

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var profileObserverJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())

        if (Settings.canDrawOverlays(this)) {
            initOverlays()
        } else {
            isOverlayRunning.value = false
            stopSelf()
        }

        profileObserverJob = serviceScope.launch {
            activeProfileFlow.collect { profile ->
                if (profile != null) {
                    mappingOverlayView?.setProfile(profile)
                }
            }
        }
    }

    private fun initOverlays() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        val wm = windowManager ?: return

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        // 1. Full-screen Mapping Canvas View
        mappingOverlayView = MappingCanvasOverlayView(this).apply {
            setProfile(activeProfileFlow.value ?: MobiladorProfile.DEFAULT_PROFILES.first())
        }

        val mappingParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }

        try {
            wm.addView(mappingOverlayView, mappingParams)
        } catch (e: Exception) {
            isOverlayRunning.value = false
            stopSelf()
            return
        }

        // 2. Draggable Control Pill / Floating Hub
        controlPillView = createFloatingControlPill(wm, layoutFlag)
        isOverlayRunning.value = true
    }

    private fun createFloatingControlPill(wm: WindowManager, layoutFlag: Int): View {
        val density = resources.displayMetrics.density
        val pillContainer = FrameLayout(this)

        val pillParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = (20 * density).toInt()
            y = (100 * density).toInt()
        }

        // Expanded Card
        val expandedPanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#E6120509")) // 90% opacity burgundy
            setPadding((12 * density).toInt(), (10 * density).toInt(), (12 * density).toInt(), (10 * density).toInt())
            visibility = View.GONE

            // Title Bar
            val titleText = TextView(this@MobiladorOverlayService).apply {
                text = "PASEO MOBILADOR"
                setTextColor(Color.parseColor("#FF2E44"))
                textSize = 12f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            }
            addView(titleText)

            val subtitleText = TextView(this@MobiladorOverlayService).apply {
                val prof = activeProfileFlow.value?.name ?: "Default"
                text = "Profil: $prof"
                setTextColor(Color.parseColor("#B0A2A7"))
                textSize = 10f
                setPadding(0, 0, 0, (8 * density).toInt())
            }
            addView(subtitleText)

            // Button: Toggle Mode (Lock Game Mode / Edit On Screen)
            val btnToggleMode = TextView(this@MobiladorOverlayService).apply {
                text = if (isEditModeActive.value) "MODE: GESER TOMBOL" else "MODE: MAIN GAME (TOUCH PASS)"
                setTextColor(Color.WHITE)
                textSize = 11f
                setBackgroundColor(if (isEditModeActive.value) Color.parseColor("#E50914") else Color.parseColor("#250A10"))
                setPadding((8 * density).toInt(), (6 * density).toInt(), (8 * density).toInt(), (6 * density).toInt())
                setOnClickListener {
                    val newMode = !isEditModeActive.value
                    isEditModeActive.value = newMode
                    text = if (newMode) "MODE: GESER TOMBOL" else "MODE: MAIN GAME (TOUCH PASS)"
                    setBackgroundColor(if (newMode) Color.parseColor("#E50914") else Color.parseColor("#250A10"))
                    updateMappingTouchMode(newMode)
                }
            }
            addView(btnToggleMode)

            // Button: Cycle Opacity (100% -> 50% -> 25% -> 0%)
            val btnOpacity = TextView(this@MobiladorOverlayService).apply {
                text = "Opacity: ${(overlayOpacity.value * 100).toInt()}%"
                setTextColor(Color.parseColor("#00E676"))
                textSize = 11f
                setPadding(0, (6 * density).toInt(), 0, (6 * density).toInt())
                setOnClickListener {
                    val nextOpacity = when (overlayOpacity.value) {
                        1.0f -> 0.6f
                        0.6f -> 0.3f
                        0.3f -> 0.05f
                        else -> 1.0f
                    }
                    overlayOpacity.value = nextOpacity
                    text = "Opacity: ${(nextOpacity * 100).toInt()}%"
                    mappingOverlayView?.setCanvasOpacity(nextOpacity)
                }
            }
            addView(btnOpacity)

            // Button: Close Overlay
            val btnClose = TextView(this@MobiladorOverlayService).apply {
                text = "Tutup Overlay"
                setTextColor(Color.parseColor("#FF6B6B"))
                textSize = 11f
                setPadding(0, (6 * density).toInt(), 0, 0)
                setOnClickListener {
                    stopSelf()
                }
            }
            addView(btnClose)
        }

        // Floating Anchor Button (Draggable pill)
        val anchorButton = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.parseColor("#CC1A050A"))
            setPadding((10 * density).toInt(), (6 * density).toInt(), (10 * density).toInt(), (6 * density).toInt())
            gravity = Gravity.CENTER_VERTICAL

            val dot = View(this@MobiladorOverlayService).apply {
                layoutParams = LinearLayout.LayoutParams((8 * density).toInt(), (8 * density).toInt()).apply {
                    marginEnd = (6 * density).toInt()
                }
                setBackgroundColor(Color.parseColor("#00E676")) // Green online dot
            }
            addView(dot)

            val label = TextView(this@MobiladorOverlayService).apply {
                text = "PASEO"
                setTextColor(Color.WHITE)
                textSize = 11f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            }
            addView(label)
        }

        pillContainer.addView(anchorButton)
        pillContainer.addView(expandedPanel)

        // Drag & Touch listener on anchorButton
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isClick = false

        anchorButton.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = pillParams.x
                    initialY = pillParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isClick = true
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - initialTouchX
                    val dy = event.rawY - initialTouchY
                    if (hypot(dx, dy) > 10) {
                        isClick = false
                    }
                    pillParams.x = (initialX + dx).toInt()
                    pillParams.y = (initialY + dy).toInt()
                    wm.updateViewLayout(pillContainer, pillParams)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (isClick) {
                        // Toggle expand / collapse panel
                        expandedPanel.visibility = if (expandedPanel.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                    }
                    true
                }
                else -> false
            }
        }

        wm.addView(pillContainer, pillParams)
        return pillContainer
    }

    private fun updateMappingTouchMode(isEditMode: Boolean) {
        val wm = windowManager ?: return
        val view = mappingOverlayView ?: return
        val params = view.layoutParams as? WindowManager.LayoutParams ?: return

        if (isEditMode) {
            // Allows user to touch and reposition buttons directly over the game
            params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        } else {
            // Touch passes through 100% to game
            params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        }
        wm.updateViewLayout(view, params)
    }

    override fun onDestroy() {
        super.onDestroy()
        profileObserverJob?.cancel()

        try {
            if (mappingOverlayView != null && mappingOverlayView?.isAttachedToWindow == true) {
                windowManager?.removeViewImmediate(mappingOverlayView)
            }
        } catch (_: Exception) {}

        try {
            if (controlPillView != null && controlPillView?.isAttachedToWindow == true) {
                windowManager?.removeViewImmediate(controlPillView)
            }
        } catch (_: Exception) {}

        mappingOverlayView = null
        controlPillView = null
        isOverlayRunning.value = false
        isEditModeActive.value = false
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "PASEO SENSI Mobilador Overlay",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Layanan overlay HUD mapping keyboard dan mouse untuk game"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, MobiladorOverlayService::class.java).apply {
            action = ACTION_STOP_OVERLAY
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("PASEO SENSI — Mobilador HUD Aktif")
            .setContentText("Overlay mapping tombol tampil di atas game.")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Hentikan", stopPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_OVERLAY) {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    companion object {
        private const val NOTIFICATION_ID = 2002
        private const val CHANNEL_ID = "paseo_mobilador_overlay_channel"
        const val ACTION_STOP_OVERLAY = "com.example.service.ACTION_STOP_MOBILADOR_OVERLAY"

        val isOverlayRunning = MutableStateFlow(false)
        val activeProfileFlow = MutableStateFlow<MobiladorProfile?>(null)
        val isEditModeActive = MutableStateFlow(false)
        val overlayOpacity = MutableStateFlow(0.85f)

        fun start(context: Context, profile: MobiladorProfile): Boolean {
            if (!Settings.canDrawOverlays(context)) {
                return false
            }
            activeProfileFlow.value = profile
            val intent = Intent(context, MobiladorOverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            return true
        }

        fun stop(context: Context) {
            val intent = Intent(context, MobiladorOverlayService::class.java)
            context.stopService(intent)
            isOverlayRunning.value = false
            isEditModeActive.value = false
        }

        fun updateProfile(profile: MobiladorProfile) {
            activeProfileFlow.value = profile
        }
    }
}

/**
 * Full-screen custom view that renders all mapped buttons (WASD, Fire, Aim, Reload, etc.)
 * directly on top of the screen/game with high-performance Android 2D graphics.
 */
class MappingCanvasOverlayView(context: Context) : View(context) {

    private var profile: MobiladorProfile = MobiladorProfile.DEFAULT_PROFILES.first()
    private var canvasOpacity: Float = 0.85f
    private var draggedElementId: String? = null

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }
    private val subTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E0E0E0")
        textAlign = Paint.Align.CENTER
        textSize = 24f
    }

    fun setProfile(newProfile: MobiladorProfile) {
        this.profile = newProfile
        invalidate()
    }

    fun setCanvasOpacity(opacity: Float) {
        this.canvasOpacity = opacity
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (canvasOpacity <= 0.05f) return

        val screenW = width.toFloat()
        val screenH = height.toFloat()
        if (screenW <= 0 || screenH <= 0) return

        for (elem in profile.elements) {
            val cx = elem.normalizedX * screenW
            val cy = elem.normalizedY * screenH
            val radius = (elem.sizeDp * resources.displayMetrics.density) / 2f
            val alphaInt = ((elem.opacity * canvasOpacity).coerceIn(0.1f, 1.0f) * 255).toInt()

            when (elem.type) {
                MappingElementType.MOVEMENT_WASD -> {
                    // Draw large D-Pad Movement Circle
                    val dpadRadius = radius * 1.6f
                    bgPaint.color = Color.argb((alphaInt * 0.25f).toInt(), 229, 9, 20)
                    canvas.drawCircle(cx, cy, dpadRadius, bgPaint)

                    borderPaint.color = Color.argb(alphaInt, 255, 46, 68)
                    borderPaint.strokeWidth = 3.5f
                    canvas.drawCircle(cx, cy, dpadRadius, borderPaint)

                    // Draw inner circle
                    bgPaint.color = Color.argb((alphaInt * 0.5f).toInt(), 20, 20, 30)
                    canvas.drawCircle(cx, cy, radius * 0.7f, bgPaint)

                    textPaint.textSize = radius * 0.45f
                    textPaint.color = Color.argb(alphaInt, 255, 255, 255)
                    canvas.drawText("W", cx, cy - dpadRadius * 0.55f + textPaint.textSize / 3f, textPaint)
                    canvas.drawText("S", cx, cy + dpadRadius * 0.55f + textPaint.textSize / 3f, textPaint)
                    canvas.drawText("A", cx - dpadRadius * 0.55f, cy + textPaint.textSize / 3f, textPaint)
                    canvas.drawText("D", cx + dpadRadius * 0.55f, cy + textPaint.textSize / 3f, textPaint)
                    canvas.drawText("WASD", cx, cy + textPaint.textSize / 3f, textPaint)
                }
                MappingElementType.FIRE -> {
                    // Crimson Fire Button (L-Click)
                    bgPaint.color = Color.argb((alphaInt * 0.5f).toInt(), 229, 9, 20)
                    canvas.drawCircle(cx, cy, radius, bgPaint)

                    borderPaint.color = Color.argb(alphaInt, 255, 46, 68)
                    borderPaint.strokeWidth = 4f
                    canvas.drawCircle(cx, cy, radius, borderPaint)

                    // Fire Reticle icon
                    canvas.drawLine(cx - radius * 0.5f, cy, cx + radius * 0.5f, cy, borderPaint)
                    canvas.drawLine(cx, cy - radius * 0.5f, cx, cy + radius * 0.5f, borderPaint)

                    textPaint.textSize = radius * 0.35f
                    textPaint.color = Color.argb(alphaInt, 255, 255, 255)
                    canvas.drawText("M_LEFT", cx, cy - radius * 0.15f, textPaint)
                    canvas.drawText("FIRE", cx, cy + radius * 0.45f, textPaint)
                }
                MappingElementType.AIM -> {
                    // Emerald Aim Button (R-Click)
                    bgPaint.color = Color.argb((alphaInt * 0.5f).toInt(), 0, 230, 118)
                    canvas.drawCircle(cx, cy, radius, bgPaint)

                    borderPaint.color = Color.argb(alphaInt, 0, 230, 118)
                    borderPaint.strokeWidth = 4f
                    canvas.drawCircle(cx, cy, radius, borderPaint)
                    canvas.drawCircle(cx, cy, radius * 0.45f, borderPaint)

                    textPaint.textSize = radius * 0.35f
                    textPaint.color = Color.argb(alphaInt, 255, 255, 255)
                    canvas.drawText("M_RIGHT", cx, cy + textPaint.textSize / 3f, textPaint)
                }
                else -> {
                    // Standard Action Key (Crouch, Jump, Reload, Weapon, etc.)
                    bgPaint.color = Color.argb((alphaInt * 0.4f).toInt(), 30, 20, 25)
                    canvas.drawCircle(cx, cy, radius, bgPaint)

                    borderPaint.color = Color.argb(alphaInt, 255, 145, 0)
                    borderPaint.strokeWidth = 3f
                    canvas.drawCircle(cx, cy, radius, borderPaint)

                    textPaint.textSize = radius * 0.42f
                    textPaint.color = Color.argb(alphaInt, 255, 255, 255)
                    val label = elem.keyBinding
                    canvas.drawText(label, cx, cy + textPaint.textSize / 3f, textPaint)
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!MobiladorOverlayService.isEditModeActive.value) {
            return false // Let touch pass
        }

        val screenW = width.toFloat()
        val screenH = height.toFloat()
        if (screenW <= 0 || screenH <= 0) return false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Find clicked element
                val touchX = event.x
                val touchY = event.y
                for (elem in profile.elements) {
                    val cx = elem.normalizedX * screenW
                    val cy = elem.normalizedY * screenH
                    val radius = (elem.sizeDp * resources.displayMetrics.density) / 2f
                    if (hypot(touchX - cx, touchY - cy) <= radius * 1.5f) {
                        draggedElementId = elem.id
                        return true
                    }
                }
                return false
            }
            MotionEvent.ACTION_MOVE -> {
                val id = draggedElementId ?: return false
                val newNormX = (event.x / screenW).coerceIn(0.02f, 0.98f)
                val newNormY = (event.y / screenH).coerceIn(0.02f, 0.98f)

                val updatedList = profile.elements.map {
                    if (it.id == id) it.copy(normalizedX = newNormX, normalizedY = newNormY) else it
                }
                profile = profile.copy(elements = updatedList)
                MobiladorOverlayService.activeProfileFlow.value = profile
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                draggedElementId = null
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}
