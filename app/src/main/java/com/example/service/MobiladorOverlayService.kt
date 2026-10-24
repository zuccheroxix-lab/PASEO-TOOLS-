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
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.Display
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.SeekBar
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
import java.util.UUID
import kotlin.math.hypot
import kotlin.math.roundToInt

class MobiladorOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var mappingOverlayView: MappingCanvasOverlayView? = null
    private var sideDockContainer: View? = null

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

        // 1. Full-screen Mapping Canvas View (starts in Game Mode: touch pass-through)
        val initialProfile = activeProfileFlow.value ?: MobiladorProfile.DEFAULT_PROFILES.first()
        mappingOverlayView = MappingCanvasOverlayView(this, onProfileChanged = { updated ->
            activeProfileFlow.value = updated
        }, onExitEditMode = {
            isEditModeActive.value = false
            updateMappingTouchMode(false)
        }).apply {
            setProfile(initialProfile)
            setCanvasOpacity(overlayOpacity.value)
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

        // 2. Floating Edge Dock + Sliding Side Panel
        sideDockContainer = createFloatingSidePanel(wm, layoutFlag)
        isOverlayRunning.value = true
    }

    private fun createFloatingSidePanel(wm: WindowManager, layoutFlag: Int): View {
        val density = resources.displayMetrics.density
        val rootContainer = FrameLayout(this)

        val rootParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = (140 * density).toInt()
        }

        // Content Views
        val edgeDockTab = LinearLayout(this)
        val sidePanel = LinearLayout(this)

        // ----------------------------------------------------
        // EDGE DOCK TAB (Small handle anchored to bezel)
        // ----------------------------------------------------
        edgeDockTab.apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            val bg = GradientDrawable().apply {
                setColor(Color.parseColor("#E6120509")) // Dark Burgundy
                setStroke((1.5f * density).toInt(), Color.parseColor("#FF2E44")) // Crimson border
                cornerRadii = floatArrayOf(
                    0f, 0f,
                    16f * density, 16f * density,
                    16f * density, 16f * density,
                    0f, 0f
                )
            }
            background = bg
            setPadding((8 * density).toInt(), (10 * density).toInt(), (12 * density).toInt(), (10 * density).toInt())

            val statusDot = View(this@MobiladorOverlayService).apply {
                layoutParams = LinearLayout.LayoutParams((8 * density).toInt(), (8 * density).toInt()).apply {
                    marginEnd = (6 * density).toInt()
                }
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.parseColor("#00E676")) // Emerald online
                }
            }
            addView(statusDot)

            val tabText = TextView(this@MobiladorOverlayService).apply {
                text = "PASEO"
                setTextColor(Color.WHITE)
                textSize = 11f
                typeface = Typeface.DEFAULT_BOLD
                letterSpacing = 0.08f
            }
            addView(tabText)
        }

        // ----------------------------------------------------
        // SLIDING SIDE PANEL (In-game reference menu)
        // ----------------------------------------------------
        sidePanel.apply {
            orientation = LinearLayout.VERTICAL
            val bg = GradientDrawable().apply {
                setColor(Color.parseColor("#F511050A")) // 96% opacity dark burgundy
                setStroke((1.5f * density).toInt(), Color.parseColor("#FF2E44"))
                cornerRadius = 16f * density
            }
            background = bg
            setPadding((14 * density).toInt(), (14 * density).toInt(), (14 * density).toInt(), (14 * density).toInt())
            visibility = View.GONE
            layoutParams = FrameLayout.LayoutParams(
                (290 * density).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        // Helper closures to populate main menu vs submenus
        lateinit var renderMainMenu: () -> Unit
        lateinit var renderMouseSettings: () -> Unit
        lateinit var renderResolutionScale: () -> Unit

        renderMainMenu = {
            sidePanel.removeAllViews()

            // Header Row
            val header = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            }

            val titleCol = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }

            val appTitle = TextView(this).apply {
                text = "PASEO MOBILADOR"
                setTextColor(Color.parseColor("#FF2E44"))
                textSize = 13f
                typeface = Typeface.DEFAULT_BOLD
                letterSpacing = 0.05f
            }
            titleCol.addView(appTitle)

            val profileName = TextView(this).apply {
                val p = activeProfileFlow.value?.name ?: "Battle Royale Spec"
                text = "Profil: $p"
                setTextColor(Color.parseColor("#C8B8BE"))
                textSize = 10f
            }
            titleCol.addView(profileName)
            header.addView(titleCol)

            val closeBtn = TextView(this).apply {
                text = "✕"
                setTextColor(Color.WHITE)
                textSize = 16f
                typeface = Typeface.DEFAULT_BOLD
                setPadding((8 * density).toInt(), (4 * density).toInt(), (8 * density).toInt(), (4 * density).toInt())
                setOnClickListener {
                    sidePanel.visibility = View.GONE
                    edgeDockTab.visibility = View.VISIBLE
                }
            }
            header.addView(closeBtn)
            sidePanel.addView(header)

            val divider = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (1 * density).toInt()).apply {
                    topMargin = (8 * density).toInt()
                    bottomMargin = (10 * density).toInt()
                }
                setBackgroundColor(Color.parseColor("#33FF2E44"))
            }
            sidePanel.addView(divider)

            // Menu Item 1: EDIT KEYBOARD
            sidePanel.addView(createMenuItem(
                density,
                title = "⌨  Edit Keyboard",
                subtitle = if (isEditModeActive.value) "Sedang aktif (Kunci Layout)" else "Atur & geser tombol di atas game",
                accentColor = if (isEditModeActive.value) "#00E676" else "#FF2E44"
            ) {
                val newMode = !isEditModeActive.value
                isEditModeActive.value = newMode
                updateMappingTouchMode(newMode)
                sidePanel.visibility = View.GONE
                edgeDockTab.visibility = View.VISIBLE
            })

            // Menu Item 2: MOUSE SETTINGS
            sidePanel.addView(createMenuItem(
                density,
                title = "🖱  Mouse Settings",
                subtitle = "Speed, ukuran kursor, polling rate hardware",
                accentColor = "#FF8008"
            ) {
                renderMouseSettings()
            })

            // Menu Item 3: RESOLUTION SCALE
            sidePanel.addView(createMenuItem(
                density,
                title = "📐  Resolution Scale",
                subtitle = "Sesuaikan rasio (16:9, 20:9) & koordinat",
                accentColor = "#00E5FF"
            ) {
                renderResolutionScale()
            })

            // Menu Item 4: OPACITY CYCLER
            val currentOpacity = (overlayOpacity.value * 100).roundToInt()
            sidePanel.addView(createMenuItem(
                density,
                title = "👁  Opacity HUD: $currentOpacity%",
                subtitle = "Ketuk untuk siklus: 100% → 60% → 30% → 5%",
                accentColor = "#E0E0E0"
            ) {
                val nextOpacity = when (overlayOpacity.value) {
                    1.0f -> 0.60f
                    0.60f -> 0.30f
                    0.30f -> 0.05f
                    else -> 1.0f
                }
                overlayOpacity.value = nextOpacity
                mappingOverlayView?.setCanvasOpacity(nextOpacity)
                renderMainMenu()
            })

            // Menu Item 5: RETURN TO APP
            sidePanel.addView(createMenuItem(
                density,
                title = "📱  Return to App",
                subtitle = "Buka menu utama PASEO SENSI",
                accentColor = "#B388FF"
            ) {
                returnToApp()
                sidePanel.visibility = View.GONE
                edgeDockTab.visibility = View.VISIBLE
            })

            // Menu Item 6: TURN OFF OVERLAY
            sidePanel.addView(createMenuItem(
                density,
                title = "⛔  Turn Off (Matikan)",
                subtitle = "Hentikan overlay mobilador seutuhnya",
                accentColor = "#FF5252"
            ) {
                stopSelf()
            })
        }

        // Submenu: MOUSE SETTINGS
        renderMouseSettings = {
            sidePanel.removeAllViews()

            // Header Back
            val backHeader = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            }
            val backBtn = TextView(this).apply {
                text = "← Kembali"
                setTextColor(Color.parseColor("#FF2E44"))
                textSize = 12f
                typeface = Typeface.DEFAULT_BOLD
                setPadding(0, (4 * density).toInt(), (10 * density).toInt(), (4 * density).toInt())
                setOnClickListener { renderMainMenu() }
            }
            backHeader.addView(backBtn)

            val titleText = TextView(this).apply {
                text = "MOUSE SETTINGS"
                setTextColor(Color.WHITE)
                textSize = 12f
                typeface = Typeface.DEFAULT_BOLD
            }
            backHeader.addView(titleText)
            sidePanel.addView(backHeader)

            // Current Profile Ref
            val profile = activeProfileFlow.value ?: MobiladorProfile.DEFAULT_PROFILES.first()

            // 1. Cursor Speed
            val speedLabel = TextView(this).apply {
                text = "Kecepatan Kursor: ${String.format(java.util.Locale.US, "%.1fx", profile.cursorSpeed)}"
                setTextColor(Color.WHITE)
                textSize = 11f
                setPadding(0, (10 * density).toInt(), 0, (4 * density).toInt())
            }
            sidePanel.addView(speedLabel)

            val speedSeekBar = SeekBar(this).apply {
                max = 25 // 0.5x to 3.0x (step 0.1)
                progress = ((profile.cursorSpeed - 0.5f) * 10f).roundToInt().coerceIn(0, 25)
                setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(sb: SeekBar?, prog: Int, fromUser: Boolean) {
                        if (fromUser) {
                            val newSpeed = 0.5f + (prog / 10f)
                            val updated = (activeProfileFlow.value ?: profile).copy(cursorSpeed = newSpeed)
                            activeProfileFlow.value = updated
                            speedLabel.text = "Kecepatan Kursor: ${String.format(java.util.Locale.US, "%.1fx", newSpeed)}"
                        }
                    }
                    override fun onStartTrackingTouch(sb: SeekBar?) {}
                    override fun onStopTrackingTouch(sb: SeekBar?) {}
                })
            }
            sidePanel.addView(speedSeekBar)

            // 2. Cursor Size Selector (24dp, 32dp, 48dp)
            val sizeLabel = TextView(this).apply {
                text = "Ukuran Pointer / Reticle:"
                setTextColor(Color.WHITE)
                textSize = 11f
                setPadding(0, (8 * density).toInt(), 0, (6 * density).toInt())
            }
            sidePanel.addView(sizeLabel)

            val sizeRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            }
            listOf(24f to "Kecil (24)", 32f to "Sedang (32)", 48f to "Besar (48)").forEach { (sz, lbl) ->
                val btn = TextView(this).apply {
                    text = lbl
                    textSize = 10f
                    val isSel = (profile.cursorSizeDp - sz) in -2f..2f
                    setTextColor(if (isSel) Color.WHITE else Color.parseColor("#B0A2A7"))
                    background = GradientDrawable().apply {
                        setColor(if (isSel) Color.parseColor("#CCFF2E44") else Color.parseColor("#261118"))
                        cornerRadius = 8f * density
                    }
                    setPadding((8 * density).toInt(), (6 * density).toInt(), (8 * density).toInt(), (6 * density).toInt())
                    layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                        marginEnd = (4 * density).toInt()
                    }
                    gravity = Gravity.CENTER
                    setOnClickListener {
                        val updated = (activeProfileFlow.value ?: profile).copy(cursorSizeDp = sz)
                        activeProfileFlow.value = updated
                        renderMouseSettings()
                    }
                }
                sizeRow.addView(btn)
            }
            sidePanel.addView(sizeRow)

            // 3. Polling Rate Readout & Display Info
            val displayHz = queryDisplayRefreshRate()
            val pollingCard = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                background = GradientDrawable().apply {
                    setColor(Color.parseColor("#221016"))
                    cornerRadius = 8f * density
                    setStroke((1 * density).toInt(), Color.parseColor("#44FF2E44"))
                }
                setPadding((8 * density).toInt(), (8 * density).toInt(), (8 * density).toInt(), (8 * density).toInt())
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    topMargin = (10 * density).toInt()
                }

                val hzText = TextView(this@MobiladorOverlayService).apply {
                    text = "Layar Display: ${displayHz.toInt()} Hz (Refresh Rate)"
                    setTextColor(Color.parseColor("#00E676"))
                    textSize = 11f
                    typeface = Typeface.DEFAULT_BOLD
                }
                addView(hzText)

                val usbInfo = TextView(this@MobiladorOverlayService).apply {
                    text = "USB HID Mouse: 125 Hz standard (500/1000Hz via converter gaming OTG)"
                    setTextColor(Color.parseColor("#C8B8BE"))
                    textSize = 10f
                    setPadding(0, (2 * density).toInt(), 0, 0)
                }
                addView(usbInfo)
            }
            sidePanel.addView(pollingCard)

            // 4. Mouse Look / Pointer Lock Toggle
            val mouseLookRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    topMargin = (10 * density).toInt()
                }
            }
            val mouseLookText = TextView(this).apply {
                text = "Simulasi Mouse Look FPS: " + if (profile.isCursorHiddenInGame) "AKTIF" else "NONAKTIF"
                setTextColor(if (profile.isCursorHiddenInGame) Color.parseColor("#00E676") else Color.parseColor("#FF5252"))
                textSize = 11f
                typeface = Typeface.DEFAULT_BOLD
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }
            mouseLookRow.addView(mouseLookText)

            val toggleLookBtn = TextView(this).apply {
                text = if (profile.isCursorHiddenInGame) "Matikan" else "Aktifkan"
                setTextColor(Color.WHITE)
                textSize = 10f
                background = GradientDrawable().apply {
                    setColor(Color.parseColor("#38151D"))
                    cornerRadius = 6f * density
                }
                setPadding((8 * density).toInt(), (4 * density).toInt(), (8 * density).toInt(), (4 * density).toInt())
                setOnClickListener {
                    val updated = (activeProfileFlow.value ?: profile).copy(isCursorHiddenInGame = !profile.isCursorHiddenInGame)
                    activeProfileFlow.value = updated
                    renderMouseSettings()
                }
            }
            mouseLookRow.addView(toggleLookBtn)
            sidePanel.addView(mouseLookRow)
        }

        // Submenu: RESOLUTION SCALE
        renderResolutionScale = {
            sidePanel.removeAllViews()

            // Header Back
            val backHeader = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            }
            val backBtn = TextView(this).apply {
                text = "← Kembali"
                setTextColor(Color.parseColor("#FF2E44"))
                textSize = 12f
                typeface = Typeface.DEFAULT_BOLD
                setPadding(0, (4 * density).toInt(), (10 * density).toInt(), (4 * density).toInt())
                setOnClickListener { renderMainMenu() }
            }
            backHeader.addView(backBtn)

            val titleText = TextView(this).apply {
                text = "RESOLUTION SCALE"
                setTextColor(Color.WHITE)
                textSize = 12f
                typeface = Typeface.DEFAULT_BOLD
            }
            backHeader.addView(titleText)
            sidePanel.addView(backHeader)

            val profile = activeProfileFlow.value ?: MobiladorProfile.DEFAULT_PROFILES.first()

            // Description
            val desc = TextView(this).apply {
                text = "Sesuaikan skala dan rasio tombol agar pas dengan aspect ratio layar hp saat game berjalan:"
                setTextColor(Color.parseColor("#C8B8BE"))
                textSize = 10f
                setPadding(0, (8 * density).toInt(), 0, (8 * density).toInt())
            }
            sidePanel.addView(desc)

            // Aspect Ratio Presets (16:9, 20:9, 19.5:9, 4:3)
            val ratioRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            }
            listOf("16:9", "20:9", "19.5:9", "4:3").forEach { r ->
                val isSel = profile.aspectRatio == r
                val chip = TextView(this).apply {
                    text = r
                    textSize = 10f
                    setTextColor(if (isSel) Color.WHITE else Color.parseColor("#B0A2A7"))
                    background = GradientDrawable().apply {
                        setColor(if (isSel) Color.parseColor("#CC00E5FF") else Color.parseColor("#261118"))
                        cornerRadius = 8f * density
                    }
                    setPadding((6 * density).toInt(), (6 * density).toInt(), (6 * density).toInt(), (6 * density).toInt())
                    layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                        marginEnd = (4 * density).toInt()
                    }
                    gravity = Gravity.CENTER
                    setOnClickListener {
                        val updated = (activeProfileFlow.value ?: profile).copy(aspectRatio = r)
                        activeProfileFlow.value = updated
                        renderResolutionScale()
                    }
                }
                ratioRow.addView(chip)
            }
            sidePanel.addView(ratioRow)

            // Scale Multiplier Slider (0.80x to 1.20x)
            val currentScale = profile.resolutionScale
            val scaleLabel = TextView(this).apply {
                text = "Skala Koordinat: ${String.format(java.util.Locale.US, "%.2fx", currentScale)} (${(currentScale * 100).toInt()}%)"
                setTextColor(Color.WHITE)
                textSize = 11f
                setPadding(0, (12 * density).toInt(), 0, (4 * density).toInt())
            }
            sidePanel.addView(scaleLabel)

            val scaleSeekBar = SeekBar(this).apply {
                max = 40 // 0.80x to 1.20x (step 0.01)
                progress = ((currentScale - 0.80f) * 100f).roundToInt().coerceIn(0, 40)
                setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(sb: SeekBar?, prog: Int, fromUser: Boolean) {
                        if (fromUser) {
                            val newScale = 0.80f + (prog / 100f)
                            val updated = (activeProfileFlow.value ?: profile).copy(resolutionScale = newScale)
                            activeProfileFlow.value = updated
                            scaleLabel.text = "Skala Koordinat: ${String.format(java.util.Locale.US, "%.2fx", newScale)} (${(newScale * 100).toInt()}%)"
                        }
                    }
                    override fun onStartTrackingTouch(sb: SeekBar?) {}
                    override fun onStopTrackingTouch(sb: SeekBar?) {}
                })
            }
            sidePanel.addView(scaleSeekBar)

            // Reset Scale button
            val btnReset = TextView(this).apply {
                text = "Reset ke 1.00x (Normal)"
                setTextColor(Color.parseColor("#00E676"))
                textSize = 10f
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                background = GradientDrawable().apply {
                    setColor(Color.parseColor("#1B291F"))
                    cornerRadius = 8f * density
                    setStroke((1 * density).toInt(), Color.parseColor("#00E676"))
                }
                setPadding((8 * density).toInt(), (8 * density).toInt(), (8 * density).toInt(), (8 * density).toInt())
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    topMargin = (10 * density).toInt()
                }
                setOnClickListener {
                    val updated = (activeProfileFlow.value ?: profile).copy(resolutionScale = 1.0f)
                    activeProfileFlow.value = updated
                    renderResolutionScale()
                }
            }
            sidePanel.addView(btnReset)
        }

        renderMainMenu()

        rootContainer.addView(edgeDockTab)
        rootContainer.addView(sidePanel)

        // Dragging & Click on Edge Dock Tab
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isClick = false

        edgeDockTab.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = rootParams.x
                    initialY = rootParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isClick = true
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - initialTouchX
                    val dy = event.rawY - initialTouchY
                    if (hypot(dx, dy) > 12) {
                        isClick = false
                    }
                    rootParams.y = (initialY + dy).toInt()
                    wm.updateViewLayout(rootContainer, rootParams)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (isClick) {
                        // Open sliding side panel
                        renderMainMenu()
                        edgeDockTab.visibility = View.GONE
                        sidePanel.visibility = View.VISIBLE
                    }
                    true
                }
                else -> false
            }
        }

        wm.addView(rootContainer, rootParams)
        return rootContainer
    }

    private fun createMenuItem(
        density: Float,
        title: String,
        subtitle: String,
        accentColor: String,
        onClick: () -> Unit
    ): View {
        val item = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val bg = GradientDrawable().apply {
                setColor(Color.parseColor("#200D13"))
                cornerRadius = 10f * density
                setStroke((1 * density).toInt(), Color.parseColor("#381720"))
            }
            background = bg
            setPadding((10 * density).toInt(), (8 * density).toInt(), (10 * density).toInt(), (8 * density).toInt())
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = (6 * density).toInt()
            }
            setOnClickListener { onClick() }

            val titleView = TextView(this@MobiladorOverlayService).apply {
                text = title
                setTextColor(Color.parseColor(accentColor))
                textSize = 11.5f
                typeface = Typeface.DEFAULT_BOLD
            }
            addView(titleView)

            val subView = TextView(this@MobiladorOverlayService).apply {
                text = subtitle
                setTextColor(Color.parseColor("#B3A3AA"))
                textSize = 9.5f
                setPadding(0, (2 * density).toInt(), 0, 0)
            }
            addView(subView)
        }
        return item
    }

    private fun queryDisplayRefreshRate(): Float {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                display?.refreshRate ?: 60f
            } else {
                @Suppress("DEPRECATION")
                windowManager?.defaultDisplay?.refreshRate ?: 60f
            }
        } catch (_: Exception) {
            60f
        }
    }

    private fun returnToApp() {
        try {
            val appIntent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(appIntent)
        } catch (_: Exception) {}
    }

    private fun updateMappingTouchMode(isEditMode: Boolean) {
        val wm = windowManager ?: return
        val view = mappingOverlayView ?: return
        val params = view.layoutParams as? WindowManager.LayoutParams ?: return

        if (isEditMode) {
            // Edit Mode: user can touch, drag, and tap buttons directly on screen
            params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        } else {
            // Game Mode: touch passes through 100% to game underneath
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
            if (sideDockContainer != null && sideDockContainer?.isAttachedToWindow == true) {
                windowManager?.removeViewImmediate(sideDockContainer)
            }
        } catch (_: Exception) {}

        mappingOverlayView = null
        sideDockContainer = null
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
            .setContentText("Panel floating & mapping keyboard/mouse berjalan.")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Matikan", stopPendingIntent)
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
 * directly on top of the running game with high-performance Android 2D graphics.
 * In Edit Mode, enables on-screen button dragging and an in-game top toolbar.
 */
class MappingCanvasOverlayView(
    context: Context,
    private val onProfileChanged: (MobiladorProfile) -> Unit,
    private val onExitEditMode: () -> Unit
) : View(context) {

    private var profile: MobiladorProfile = MobiladorProfile.DEFAULT_PROFILES.first()
    private var canvasOpacity: Float = 0.85f
    private var draggedElementId: String? = null
    private var selectedElementId: String? = null

    // Top Toolbar Rects (for Edit Mode)
    private val btnAddRect = RectF()
    private val btnWasdRect = RectF()
    private val btnFireRect = RectF()
    private val btnAimRect = RectF()
    private val btnLockRect = RectF()

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    private val toolbarPaint = Paint(Paint.ANTI_ALIAS_FLAG)

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
        if (canvasOpacity <= 0.04f && !MobiladorOverlayService.isEditModeActive.value) return

        val screenW = width.toFloat()
        val screenH = height.toFloat()
        if (screenW <= 0 || screenH <= 0) return

        val scale = profile.resolutionScale.coerceIn(0.70f, 1.30f)
        val density = resources.displayMetrics.density

        // 1. Draw Mapping Elements
        for (elem in profile.elements) {
            // Apply Resolution Scale offset from center (0.5, 0.5)
            val scaledNormX = (0.5f + (elem.normalizedX - 0.5f) * scale).coerceIn(0.02f, 0.98f)
            val scaledNormY = (0.5f + (elem.normalizedY - 0.5f) * scale).coerceIn(0.02f, 0.98f)

            val cx = scaledNormX * screenW
            val cy = scaledNormY * screenH
            val radius = (elem.sizeDp * density * scale) / 2f
            val isSelected = elem.id == selectedElementId && MobiladorOverlayService.isEditModeActive.value

            val alphaInt = if (MobiladorOverlayService.isEditModeActive.value) {
                230
            } else {
                ((elem.opacity * canvasOpacity).coerceIn(0.1f, 1.0f) * 255).toInt()
            }

            when (elem.type) {
                MappingElementType.MOVEMENT_WASD -> {
                    val dpadRadius = radius * 1.5f
                    bgPaint.color = Color.argb((alphaInt * 0.28f).toInt(), 229, 9, 20)
                    canvas.drawCircle(cx, cy, dpadRadius, bgPaint)

                    borderPaint.color = Color.argb(alphaInt, 255, 46, 68)
                    borderPaint.strokeWidth = 3.5f
                    canvas.drawCircle(cx, cy, dpadRadius, borderPaint)

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
                    bgPaint.color = Color.argb((alphaInt * 0.50f).toInt(), 229, 9, 20)
                    canvas.drawCircle(cx, cy, radius, bgPaint)

                    borderPaint.color = Color.argb(alphaInt, 255, 46, 68)
                    borderPaint.strokeWidth = 4f
                    canvas.drawCircle(cx, cy, radius, borderPaint)

                    canvas.drawLine(cx - radius * 0.5f, cy, cx + radius * 0.5f, cy, borderPaint)
                    canvas.drawLine(cx, cy - radius * 0.5f, cx, cy + radius * 0.5f, borderPaint)

                    textPaint.textSize = radius * 0.35f
                    textPaint.color = Color.argb(alphaInt, 255, 255, 255)
                    canvas.drawText("M_LEFT", cx, cy - radius * 0.15f, textPaint)
                    canvas.drawText("FIRE", cx, cy + radius * 0.45f, textPaint)
                }
                MappingElementType.AIM -> {
                    bgPaint.color = Color.argb((alphaInt * 0.50f).toInt(), 0, 230, 118)
                    canvas.drawCircle(cx, cy, radius, bgPaint)

                    borderPaint.color = Color.argb(alphaInt, 0, 230, 118)
                    borderPaint.strokeWidth = 4f
                    canvas.drawCircle(cx, cy, radius, borderPaint)
                    canvas.drawCircle(cx, cy, radius * 0.45f, borderPaint)

                    textPaint.textSize = radius * 0.35f
                    textPaint.color = Color.argb(alphaInt, 255, 255, 255)
                    canvas.drawText("M_RIGHT", cx, cy + textPaint.textSize / 3f, textPaint)
                }
                MappingElementType.MOUSE_LOOK -> {
                    bgPaint.color = Color.argb((alphaInt * 0.30f).toInt(), 0, 229, 255)
                    canvas.drawCircle(cx, cy, radius, bgPaint)

                    borderPaint.color = Color.argb(alphaInt, 0, 229, 255)
                    borderPaint.strokeWidth = 3f
                    canvas.drawCircle(cx, cy, radius, borderPaint)

                    textPaint.textSize = radius * 0.32f
                    textPaint.color = Color.argb(alphaInt, 255, 255, 255)
                    canvas.drawText("MOUSE", cx, cy - radius * 0.15f, textPaint)
                    canvas.drawText("LOOK", cx, cy + radius * 0.45f, textPaint)
                }
                else -> {
                    bgPaint.color = Color.argb((alphaInt * 0.45f).toInt(), 30, 20, 26)
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

            // Selection indicator when in edit mode
            if (isSelected) {
                borderPaint.color = Color.YELLOW
                borderPaint.strokeWidth = 4.5f
                canvas.drawCircle(cx, cy, radius + 8f, borderPaint)
            }
        }

        // 2. In Edit Mode: Draw Floating Top Edit Bar
        if (MobiladorOverlayService.isEditModeActive.value) {
            val barTop = 16f * density
            val barHeight = 44f * density
            val btnWidth = 72f * density
            val btnSpacing = 8f * density
            val totalW = (btnWidth * 5) + (btnSpacing * 4)
            val startX = (screenW - totalW) / 2f

            // Background of toolbar
            toolbarPaint.color = Color.parseColor("#EE120509")
            toolbarPaint.style = Paint.Style.FILL
            val barRect = RectF(startX - 12f * density, barTop - 6f * density, startX + totalW + 12f * density, barTop + barHeight + 6f * density)
            canvas.drawRoundRect(barRect, 14f * density, 14f * density, toolbarPaint)

            toolbarPaint.color = Color.parseColor("#FF2E44")
            toolbarPaint.style = Paint.Style.STROKE
            toolbarPaint.strokeWidth = 2f * density
            canvas.drawRoundRect(barRect, 14f * density, 14f * density, toolbarPaint)

            // Button 1: [+ Tombol]
            btnAddRect.set(startX, barTop, startX + btnWidth, barTop + barHeight)
            drawToolbarButton(canvas, btnAddRect, "+ Tombol", "#2A121A", "#FF2E44", density)

            // Button 2: [WASD]
            val x2 = startX + btnWidth + btnSpacing
            btnWasdRect.set(x2, barTop, x2 + btnWidth, barTop + barHeight)
            drawToolbarButton(canvas, btnWasdRect, "WASD", "#2A121A", "#FFFFFF", density)

            // Button 3: [Fire]
            val x3 = x2 + btnWidth + btnSpacing
            btnFireRect.set(x3, barTop, x3 + btnWidth, barTop + barHeight)
            drawToolbarButton(canvas, btnFireRect, "Fire (L)", "#2A121A", "#FF4444", density)

            // Button 4: [Aim]
            val x4 = x3 + btnWidth + btnSpacing
            btnAimRect.set(x4, barTop, x4 + btnWidth, barTop + barHeight)
            drawToolbarButton(canvas, btnAimRect, "Aim (R)", "#2A121A", "#00E676", density)

            // Button 5: [Kunci & Simpan]
            val x5 = x4 + btnWidth + btnSpacing
            btnLockRect.set(x5, barTop, x5 + btnWidth, barTop + barHeight)
            drawToolbarButton(canvas, btnLockRect, "Kunci", "#00E676", "#000000", density)
        }
    }

    private fun drawToolbarButton(
        canvas: Canvas,
        rect: RectF,
        label: String,
        bgColorStr: String,
        textColorStr: String,
        density: Float
    ) {
        toolbarPaint.style = Paint.Style.FILL
        toolbarPaint.color = Color.parseColor(bgColorStr)
        canvas.drawRoundRect(rect, 8f * density, 8f * density, toolbarPaint)

        toolbarPaint.style = Paint.Style.STROKE
        toolbarPaint.color = Color.parseColor("#55FF2E44")
        toolbarPaint.strokeWidth = 1f * density
        canvas.drawRoundRect(rect, 8f * density, 8f * density, toolbarPaint)

        textPaint.color = Color.parseColor(textColorStr)
        textPaint.textSize = 11f * density
        textPaint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText(label, rect.centerX(), rect.centerY() + (textPaint.textSize / 3f), textPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!MobiladorOverlayService.isEditModeActive.value) {
            return false // Let touch pass 100% to game
        }

        val screenW = width.toFloat()
        val screenH = height.toFloat()
        if (screenW <= 0 || screenH <= 0) return false

        val density = resources.displayMetrics.density
        val scale = profile.resolutionScale.coerceIn(0.70f, 1.30f)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val touchX = event.x
                val touchY = event.y

                // 1. Check Top Toolbar clicks
                if (btnAddRect.contains(touchX, touchY)) {
                    addNewElement()
                    return true
                }
                if (btnWasdRect.contains(touchX, touchY)) {
                    addOrResetWasd()
                    return true
                }
                if (btnFireRect.contains(touchX, touchY)) {
                    addOrResetFire()
                    return true
                }
                if (btnAimRect.contains(touchX, touchY)) {
                    addOrResetAim()
                    return true
                }
                if (btnLockRect.contains(touchX, touchY)) {
                    // Lock layout & exit edit mode
                    profile = profile.copy(isLocked = true)
                    onProfileChanged(profile)
                    onExitEditMode()
                    return true
                }

                // 2. Check mapping elements touch
                for (elem in profile.elements.reversed()) {
                    val scaledNormX = (0.5f + (elem.normalizedX - 0.5f) * scale).coerceIn(0.02f, 0.98f)
                    val scaledNormY = (0.5f + (elem.normalizedY - 0.5f) * scale).coerceIn(0.02f, 0.98f)
                    val cx = scaledNormX * screenW
                    val cy = scaledNormY * screenH
                    val radius = (elem.sizeDp * density * scale) / 2f

                    if (hypot(touchX - cx, touchY - cy) <= radius * 1.4f) {
                        draggedElementId = elem.id
                        selectedElementId = elem.id
                        invalidate()
                        return true
                    }
                }

                selectedElementId = null
                invalidate()
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val id = draggedElementId ?: return false
                val rawNormX = (event.x / screenW).coerceIn(0.02f, 0.98f)
                val rawNormY = (event.y / screenH).coerceIn(0.02f, 0.98f)

                // Reverse resolution scale relative to center (0.5, 0.5)
                val unscaledX = (0.5f + (rawNormX - 0.5f) / scale).coerceIn(0.02f, 0.98f)
                val unscaledY = (0.5f + (rawNormY - 0.5f) / scale).coerceIn(0.02f, 0.98f)

                val updatedList = profile.elements.map {
                    if (it.id == id) it.copy(normalizedX = unscaledX, normalizedY = unscaledY) else it
                }
                profile = profile.copy(elements = updatedList)
                onProfileChanged(profile)
                invalidate()
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                draggedElementId = null
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun addNewElement() {
        val nextType = when (profile.elements.size % 6) {
            0 -> MappingElementType.JUMP
            1 -> MappingElementType.CROUCH
            2 -> MappingElementType.RELOAD
            3 -> MappingElementType.INTERACT
            4 -> MappingElementType.INVENTORY
            else -> MappingElementType.MEDKIT
        }
        val newElem = MappingElement(
            id = UUID.randomUUID().toString(),
            name = nextType.displayName,
            type = nextType,
            keyBinding = nextType.defaultKey,
            normalizedX = 0.5f,
            normalizedY = 0.5f,
            sizeDp = 48f
        )
        profile = profile.copy(elements = profile.elements + newElem)
        selectedElementId = newElem.id
        onProfileChanged(profile)
        invalidate()
    }

    private fun addOrResetWasd() {
        val existing = profile.elements.find { it.type == MappingElementType.MOVEMENT_WASD }
        if (existing != null) {
            val updated = profile.elements.map {
                if (it.id == existing.id) it.copy(normalizedX = 0.18f, normalizedY = 0.68f) else it
            }
            profile = profile.copy(elements = updated)
            selectedElementId = existing.id
        } else {
            val wasd = MappingElement(
                id = UUID.randomUUID().toString(),
                name = "WASD Movement",
                type = MappingElementType.MOVEMENT_WASD,
                keyBinding = "W,A,S,D",
                normalizedX = 0.18f,
                normalizedY = 0.68f,
                sizeDp = 90f
            )
            profile = profile.copy(elements = profile.elements + wasd)
            selectedElementId = wasd.id
        }
        onProfileChanged(profile)
        invalidate()
    }

    private fun addOrResetFire() {
        val existing = profile.elements.find { it.type == MappingElementType.FIRE }
        if (existing != null) {
            val updated = profile.elements.map {
                if (it.id == existing.id) it.copy(normalizedX = 0.85f, normalizedY = 0.65f) else it
            }
            profile = profile.copy(elements = updated)
            selectedElementId = existing.id
        } else {
            val fire = MappingElement(
                id = UUID.randomUUID().toString(),
                name = "Fire",
                type = MappingElementType.FIRE,
                keyBinding = "M_LEFT",
                normalizedX = 0.85f,
                normalizedY = 0.65f,
                sizeDp = 55f
            )
            profile = profile.copy(elements = profile.elements + fire)
            selectedElementId = fire.id
        }
        onProfileChanged(profile)
        invalidate()
    }

    private fun addOrResetAim() {
        val existing = profile.elements.find { it.type == MappingElementType.AIM }
        if (existing != null) {
            val updated = profile.elements.map {
                if (it.id == existing.id) it.copy(normalizedX = 0.88f, normalizedY = 0.38f) else it
            }
            profile = profile.copy(elements = updated)
            selectedElementId = existing.id
        } else {
            val aim = MappingElement(
                id = UUID.randomUUID().toString(),
                name = "Scope",
                type = MappingElementType.AIM,
                keyBinding = "M_RIGHT",
                normalizedX = 0.88f,
                normalizedY = 0.38f,
                sizeDp = 50f
            )
            profile = profile.copy(elements = profile.elements + aim)
            selectedElementId = aim.id
        }
        onProfileChanged(profile)
        invalidate()
    }
}
