package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.SensitivityCurveType
import com.example.model.SensitivityProfile
import com.example.nativecore.SensitivityBridge
import com.example.ui.MainViewModel
import com.example.ui.theme.CrimsonContainer
import com.example.ui.theme.CrimsonGlow
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.PaseoBorderDark
import com.example.ui.theme.PaseoBorderGlow
import com.example.ui.theme.PaseoDarkBackground
import com.example.ui.theme.PaseoSurfaceCard
import com.example.ui.theme.PaseoSurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun SensitivityStudioScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val allProfiles by viewModel.sensitivityRepository.profilesList.collectAsStateWithLifecycle()

    var showNewProfileDialog by remember { mutableStateOf(false) }
    var newProfileName by remember { mutableStateOf("") }

    var showImportDialog by remember { mutableStateOf(false) }
    var importJsonText by remember { mutableStateOf("") }

    var notificationMessage by remember { mutableStateOf<String?>(null) }

    // Live editing states
    var currentSensiX by remember(activeProfile) { mutableFloatStateOf(activeProfile.sensiX) }
    var currentSensiY by remember(activeProfile) { mutableFloatStateOf(activeProfile.sensiY) }
    var manualXText by remember(activeProfile) { mutableStateOf(String.format(Locale.US, "%.2f", activeProfile.sensiX)) }
    var manualYText by remember(activeProfile) { mutableStateOf(String.format(Locale.US, "%.2f", activeProfile.sensiY)) }
    var currentCurve by remember(activeProfile) { mutableStateOf(activeProfile.curveType) }
    var currentDeadzone by remember(activeProfile) { mutableFloatStateOf(activeProfile.deadzone) }
    var currentSmoothing by remember(activeProfile) { mutableFloatStateOf(activeProfile.smoothing) }
    var currentGeneral by remember(activeProfile) { mutableFloatStateOf(activeProfile.generalSensi.toFloat()) }
    var currentRedDot by remember(activeProfile) { mutableFloatStateOf(activeProfile.redDotSensi.toFloat()) }

    // Aim Simulator State
    var simulatorTouchPos by remember { mutableStateOf(Offset(0.5f, 0.5f)) }
    var transformedAimPos by remember { mutableStateOf(Offset(0.5f, 0.5f)) }
    var liveVelocity by remember { mutableFloatStateOf(0.0f) }
    var lastTouchTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    Surface(
        color = PaseoDarkBackground,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }
                    Column {
                        Text(
                            text = "Sensitivity X/Y Studio",
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Memory,
                                contentDescription = null,
                                tint = if (SensitivityBridge.isNativeModuleActive()) EmeraldAccent else CrimsonGlow,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = SensitivityBridge.getEngineVersion(),
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Row {
                    IconButton(
                        onClick = {
                            val exported = viewModel.sensitivityRepository.exportProfileJson(activeProfile)
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("PaseoSensi_Profile", exported))
                            notificationMessage = "Konfigurasi JSON berhasil disalin ke clipboard!"
                        }
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Ekspor JSON", tint = Color.White)
                    }
                    IconButton(
                        onClick = { showImportDialog = true }
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Impor JSON", tint = Color.White)
                    }
                }
            }

            AnimatedVisibility(visible = notificationMessage != null, enter = fadeIn(), exit = fadeOut()) {
                notificationMessage?.let { msg ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CrimsonContainer),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(msg, color = Color.White, fontSize = 11.sp, modifier = Modifier.weight(1f))
                            TextButton(onClick = { notificationMessage = null }) {
                                Text("Tutup", color = CrimsonGlow, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Presets Horizontal Selector
                item {
                    Text(
                        text = "PROFIL SENSITIVITAS TERSIMPAN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(allProfiles) { profile ->
                            val isSelected = profile.id == activeProfile.id
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    viewModel.sensitivityRepository.setActiveProfile(profile)
                                    viewModel.saveActiveProfile(profile)
                                    notificationMessage = "Profil '${profile.name}' dimuat."
                                },
                                label = { Text(profile.name, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CrimsonPrimary,
                                    selectedLabelColor = Color.White,
                                    containerColor = PaseoSurfaceCard,
                                    labelColor = TextSecondary
                                )
                            )
                        }
                        item {
                            Button(
                                onClick = { showNewProfileDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = PaseoSurfaceDark),
                                modifier = Modifier.height(32.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Tambah", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Profil Baru", fontSize = 11.sp)
                            }
                        }
                    }
                }

                // ==========================================
                // INTERACTIVE C++ TOUCH & AIM SIMULATOR PAD
                // ==========================================
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PaseoSurfaceCard),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, PaseoBorderGlow, RoundedCornerShape(18.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Tune, contentDescription = null, tint = CrimsonGlow, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("C++ Aim Simulator Pad", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                }
                                Text(
                                    text = "Kecepatan: ${String.format(Locale.US, "%.1f", liveVelocity)} px/ms",
                                    color = EmeraldAccent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Text(
                                text = "Geser jari di area simulator untuk menguji respon kurva X/Y secara nyata:",
                                fontSize = 11.sp,
                                color = TextMuted,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            // Interactive Canvas
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF090A10))
                                    .border(1.dp, PaseoBorderDark, RoundedCornerShape(14.dp))
                                    .pointerInput(currentSensiX, currentSensiY, currentCurve, currentDeadzone, currentSmoothing) {
                                        detectDragGestures(
                                            onDragStart = { offset ->
                                                val now = System.currentTimeMillis()
                                                lastTouchTime = now
                                                simulatorTouchPos = Offset(offset.x / size.width, offset.y / size.height)
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                val now = System.currentTimeMillis()
                                                val dt = (now - lastTouchTime).coerceAtLeast(1L).toFloat()
                                                lastTouchTime = now

                                                // Calculate transformation via C++ Native Bridge
                                                val result = SensitivityBridge.calculateTransformedPoint(
                                                    rawDeltaX = dragAmount.x,
                                                    rawDeltaY = dragAmount.y,
                                                    sensiMultiplierX = currentSensiX,
                                                    sensiMultiplierY = currentSensiY,
                                                    curveType = currentCurve,
                                                    smoothingFactor = currentSmoothing,
                                                    deadzoneThreshold = currentDeadzone,
                                                    accelerationFactor = 1.05f,
                                                    deltaTimeMs = dt
                                                )

                                                liveVelocity = result.velocity

                                                // Update transformed cursor position
                                                val newTransX = (transformedAimPos.x + (result.x / size.width)).coerceIn(0.05f, 0.95f)
                                                val newTransY = (transformedAimPos.y + (result.y / size.height)).coerceIn(0.05f, 0.95f)
                                                transformedAimPos = Offset(newTransX, newTransY)
                                                simulatorTouchPos = Offset(change.position.x / size.width, change.position.y / size.height)
                                            }
                                        )
                                    }
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val w = size.width
                                    val h = size.height

                                    // Grid lines
                                    drawLine(Color(0x22FFFFFF), Offset(0f, h / 2), Offset(w, h / 2), 1f)
                                    drawLine(Color(0x22FFFFFF), Offset(w / 2, 0f), Offset(w / 2, h), 1f)

                                    // Center calibration dot
                                    drawCircle(Color(0x44FFFFFF), radius = 4f, center = Offset(w / 2, h / 2))

                                    // Finger input touch trail (gray)
                                    val fingerCenter = Offset(simulatorTouchPos.x * w, simulatorTouchPos.y * h)
                                    drawCircle(
                                        color = Color(0x33FFFFFF),
                                        radius = 20f,
                                        center = fingerCenter
                                    )
                                    drawCircle(
                                        color = Color(0x66FFFFFF),
                                        radius = 6f,
                                        center = fingerCenter
                                    )

                                    // Transformed Crosshair Target Cursor (Crimson Glow)
                                    val aimCenter = Offset(transformedAimPos.x * w, transformedAimPos.y * h)
                                    drawCircle(
                                        color = CrimsonGlow.copy(alpha = 0.4f),
                                        radius = 24f,
                                        center = aimCenter
                                    )
                                    drawCircle(
                                        color = CrimsonPrimary,
                                        radius = 12f,
                                        center = aimCenter,
                                        style = Stroke(width = 2.5f)
                                    )
                                    drawCircle(
                                        color = Color.White,
                                        radius = 3.5f,
                                        center = aimCenter
                                    )

                                    // Vector connection line
                                    drawLine(
                                        color = CrimsonGlow.copy(alpha = 0.35f),
                                        start = fingerCenter,
                                        end = aimCenter,
                                        strokeWidth = 2f
                                    )
                                }

                                // Simulator overlay badges
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0x88000000))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("Sentuhan Jari: Abu-abu", color = Color.LightGray, fontSize = 9.sp)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0x88000000))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("Output Target C++: Merah", color = CrimsonGlow, fontSize = 9.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // X & Y SEPARATE MULTIPLIERS CONTROLS
                // ==========================================
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PaseoSurfaceCard),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, PaseoBorderDark, RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "PENGATURAN SUMBU X & Y TERPISAH",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // Horizontal Axis X Slider & Manual Input
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Sumbu Horizontal (X)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                OutlinedTextField(
                                    value = manualXText,
                                    onValueChange = { input ->
                                        manualXText = input
                                        val parsed = input.toFloatOrNull()
                                        if (parsed != null && parsed in 0.1f..4.0f) {
                                            currentSensiX = parsed
                                        }
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    modifier = Modifier
                                        .width(90.dp)
                                        .height(48.dp)
                                        .testTag("sensi_x_manual_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CrimsonPrimary,
                                        unfocusedBorderColor = PaseoBorderDark,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )
                            }
                            Slider(
                                value = currentSensiX,
                                onValueChange = {
                                    currentSensiX = it
                                    manualXText = String.format(Locale.US, "%.2f", it)
                                },
                                valueRange = 0.1f..3.0f,
                                modifier = Modifier.testTag("sensi_x_slider"),
                                colors = SliderDefaults.colors(thumbColor = CrimsonPrimary, activeTrackColor = CrimsonPrimary)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Vertical Axis Y Slider & Manual Input
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Sumbu Vertikal (Y - Drag Headshot)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                OutlinedTextField(
                                    value = manualYText,
                                    onValueChange = { input ->
                                        manualYText = input
                                        val parsed = input.toFloatOrNull()
                                        if (parsed != null && parsed in 0.1f..4.0f) {
                                            currentSensiY = parsed
                                        }
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    modifier = Modifier
                                        .width(90.dp)
                                        .height(48.dp)
                                        .testTag("sensi_y_manual_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CrimsonPrimary,
                                        unfocusedBorderColor = PaseoBorderDark,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )
                            }
                            Slider(
                                value = currentSensiY,
                                onValueChange = {
                                    currentSensiY = it
                                    manualYText = String.format(Locale.US, "%.2f", it)
                                },
                                valueRange = 0.1f..3.0f,
                                modifier = Modifier.testTag("sensi_y_slider"),
                                colors = SliderDefaults.colors(thumbColor = CrimsonPrimary, activeTrackColor = CrimsonPrimary)
                            )
                        }
                    }
                }

                // ==========================================
                // RESPONSE CURVE & DAMPING
                // ==========================================
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PaseoSurfaceCard),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, PaseoBorderDark, RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "ALGORITMA KURVA RESPON (C++ ENGINE)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            SensitivityCurveType.values().forEach { curve ->
                                val isSelected = currentCurve == curve
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) PaseoSurfaceDark else Color.Transparent)
                                        .border(
                                            1.dp,
                                            if (isSelected) CrimsonPrimary else Color.Transparent,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable { currentCurve = curve }
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) CrimsonPrimary else PaseoBorderDark)
                                            .border(1.dp, if (isSelected) CrimsonGlow else Color.Gray, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(curve.displayName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(curve.description, color = TextMuted, fontSize = 11.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Deadzone
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Deadzone Threshold", color = TextSecondary, fontSize = 12.sp)
                                Text("${String.format(Locale.US, "%.2f", currentDeadzone)}", color = Color.White, fontSize = 12.sp)
                            }
                            Slider(
                                value = currentDeadzone,
                                onValueChange = { currentDeadzone = it },
                                valueRange = 0.00f..0.15f,
                                colors = SliderDefaults.colors(thumbColor = CrimsonPrimary, activeTrackColor = CrimsonPrimary)
                            )

                            // Smoothing
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Jitter Smoothing Filter", color = TextSecondary, fontSize = 12.sp)
                                Text("${(currentSmoothing * 100).toInt()}%", color = Color.White, fontSize = 12.sp)
                            }
                            Slider(
                                value = currentSmoothing,
                                onValueChange = { currentSmoothing = it },
                                valueRange = 0.00f..0.50f,
                                colors = SliderDefaults.colors(thumbColor = CrimsonPrimary, activeTrackColor = CrimsonPrimary)
                            )
                        }
                    }
                }

                // ==========================================
                // GAME IN-GAME SLIDERS
                // ==========================================
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PaseoSurfaceCard),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, PaseoBorderDark, RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "SKALA SENSITIVITAS IN-GAME",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // General
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Lihat Sekeliling (General)", color = TextSecondary, fontSize = 12.sp)
                                Text("${currentGeneral.toInt()}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Slider(
                                value = currentGeneral,
                                onValueChange = { currentGeneral = it },
                                valueRange = 0f..100f,
                                colors = SliderDefaults.colors(thumbColor = CrimsonPrimary, activeTrackColor = CrimsonPrimary)
                            )

                            // Red Dot
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Red Dot Sight", color = TextSecondary, fontSize = 12.sp)
                                Text("${currentRedDot.toInt()}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Slider(
                                value = currentRedDot,
                                onValueChange = { currentRedDot = it },
                                valueRange = 0f..100f,
                                colors = SliderDefaults.colors(thumbColor = CrimsonPrimary, activeTrackColor = CrimsonPrimary)
                            )
                        }
                    }
                }

                // ==========================================
                // ACTIONS: SAVE, DUPLICATE, RESET
                // ==========================================
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val updated = activeProfile.copy(
                                    sensiX = currentSensiX,
                                    sensiY = currentSensiY,
                                    curveType = currentCurve,
                                    deadzone = currentDeadzone,
                                    smoothing = currentSmoothing,
                                    generalSensi = currentGeneral.toInt(),
                                    redDotSensi = currentRedDot.toInt()
                                )
                                viewModel.sensitivityRepository.saveCustomProfile(updated)
                                viewModel.saveActiveProfile(updated)
                                notificationMessage = "Profil '${updated.name}' berhasil disimpan!"
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("save_sensi_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Simpan", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val def = viewModel.sensitivityRepository.resetActiveToDefault()
                                viewModel.saveActiveProfile(def)
                                notificationMessage = "Profil di-reset ke setelan standar."
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PaseoSurfaceDark),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reset")
                        }
                    }

                    if (activeProfile.isCustom) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                viewModel.sensitivityRepository.deleteProfile(activeProfile.id)
                                notificationMessage = "Profil kustom telah dihapus."
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FF1744)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Hapus Profil Ini", color = Color(0xFFFF8A80), fontSize = 12.sp)
                        }
                    }
                }

                // ==========================================
                // TECHNICAL DISCLOSURE CARD
                // ==========================================
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF13141E)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, PaseoBorderDark, RoundedCornerShape(14.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = TextMuted, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Batasan Teknis & Keamanan Android",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Pada Android tanpa root, aplikasi biasa tidak dapat secara universal mengubah koordinat sentuh OS ke dalam game eksternal. Modul C++ PASEO SENSI melakukan perhitungan kurva transformasi, simulasi touch pad internal, dan kalkulasi rekomendasi sensitivitas. Tanpa suntikan ilegal, memori tampering, atau risiko banned.",
                                    color = TextMuted,
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    // New Profile Dialog
    if (showNewProfileDialog) {
        AlertDialog(
            onDismissRequest = { showNewProfileDialog = false },
            title = { Text("Buat Profil Sensitivitas Baru", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = newProfileName,
                    onValueChange = { newProfileName = it },
                    label = { Text("Nama Profil (misal: Headshot FF Pro)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CrimsonPrimary,
                        unfocusedBorderColor = PaseoBorderDark,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newProfileName.isNotBlank()) {
                            val newP = viewModel.sensitivityRepository.duplicateProfile(activeProfile.id, newProfileName.trim())
                            if (newP != null) {
                                viewModel.sensitivityRepository.setActiveProfile(newP)
                                viewModel.saveActiveProfile(newP)
                            }
                            showNewProfileDialog = false
                            newProfileName = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
                ) {
                    Text("Buat")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewProfileDialog = false }) {
                    Text("Batal", color = TextMuted)
                }
            },
            containerColor = PaseoSurfaceCard
        )
    }

    // Import JSON Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Impor Konfigurasi Sensitivitas", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = importJsonText,
                    onValueChange = { importJsonText = it },
                    label = { Text("Tempel JSON Profil di sini") },
                    maxLines = 6,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CrimsonPrimary,
                        unfocusedBorderColor = PaseoBorderDark,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val imported = viewModel.sensitivityRepository.importProfileJson(importJsonText)
                        if (imported != null) {
                            notificationMessage = "Profil '${imported.name}' berhasil diimpor!"
                            showImportDialog = false
                            importJsonText = ""
                        } else {
                            notificationMessage = "Format JSON tidak valid!"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
                ) {
                    Text("Impor")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Batal", color = TextMuted)
                }
            },
            containerColor = PaseoSurfaceCard
        )
    }
}
