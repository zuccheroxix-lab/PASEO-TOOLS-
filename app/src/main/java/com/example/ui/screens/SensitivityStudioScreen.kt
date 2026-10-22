package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
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
import com.example.ui.theme.CrimsonBorder
import com.example.ui.theme.CrimsonContainer
import com.example.ui.theme.CrimsonGlow
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.PaseoCardBackground
import com.example.ui.theme.PaseoDarkBackground
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensitivityStudioScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val allProfiles by viewModel.sensitivityRepository.allProfiles.collectAsStateWithLifecycle()

    var showSaveDialog by remember { mutableStateOf(false) }
    var newProfileName by remember { mutableStateOf("") }
    var showImportDialog by remember { mutableStateOf(false) }
    var importJsonText by remember { mutableStateOf("") }
    var showExportDialog by remember { mutableStateOf(false) }
    var exportJsonText by remember { mutableStateOf("") }

    // Live aim simulation pad state
    var aimReticleOffset by remember { mutableStateOf(Offset.Zero) }
    var lastRawDelta by remember { mutableStateOf(Offset.Zero) }
    var lastTransformedDelta by remember { mutableStateOf(Offset.Zero) }

    val bridge = viewModel.sensitivityRepository.bridge

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PaseoDarkBackground)
            .testTag("sensitivity_studio_screen")
    ) {
        TopAppBar(
            title = {
                Column {
                    Text("SENSITIVITY LAB X/Y", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (SensitivityBridge.isNativeAvailable()) Color(0xFF00FF88) else CrimsonGlow)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (SensitivityBridge.isNativeAvailable()) "C++ NDK Engine Active" else "Native Math Parity Engine Active",
                            fontSize = 11.sp,
                            color = Color(0xFFAAAAAA)
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack, modifier = Modifier.testTag("sensitivity_back_button")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                }
            },
            actions = {
                IconButton(
                    onClick = {
                        exportJsonText = viewModel.sensitivityRepository.exportProfilesJson()
                        showExportDialog = true
                    },
                    modifier = Modifier.testTag("export_profiles_button")
                ) {
                    Icon(Icons.Default.FileUpload, contentDescription = "Ekspor JSON", tint = Color.White)
                }
                IconButton(
                    onClick = {
                        importJsonText = ""
                        showImportDialog = true
                    },
                    modifier = Modifier.testTag("import_profiles_button")
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = "Impor JSON", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = PaseoDarkBackground)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Presets Selector Row
            Text("PRESET & PROFIL TERSIMPAN", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CrimsonGlow)

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(allProfiles, key = { it.id }) { prof ->
                    val isSelected = prof.id == activeProfile.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectSensitivityProfile(prof) },
                        label = {
                            Text(
                                prof.name,
                                color = if (isSelected) Color.White else Color(0xFFCCCCCC),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CrimsonPrimary,
                            containerColor = Color(0xFF1F1820)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) CrimsonGlow else Color(0xFF3A2B35)
                        )
                    )
                }
            }

            // Quick Actions for Current Profile
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        newProfileName = "${activeProfile.name} (Custom)"
                        showSaveDialog = true
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("save_as_new_profile_button"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = CrimsonGlow, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Simpan Baru", fontSize = 11.sp, color = Color.White)
                }

                OutlinedButton(
                    onClick = {
                        val duplicated = viewModel.sensitivityRepository.duplicateProfile(activeProfile)
                        viewModel.selectSensitivityProfile(duplicated)
                        viewModel.showMessage("Profil diduplikasi.")
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("duplicate_profile_button"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Duplikasi", fontSize = 11.sp, color = Color.White)
                }

                if (activeProfile.isCustom) {
                    OutlinedButton(
                        onClick = {
                            viewModel.sensitivityRepository.deleteProfile(activeProfile.id)
                            viewModel.showMessage("Profil kustom dihapus.")
                        },
                        modifier = Modifier.testTag("delete_profile_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFFF5555), modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Interactive Aim Calibration Pad
            AimCalibrationPad(
                activeProfile = activeProfile,
                bridge = bridge,
                reticleOffset = aimReticleOffset,
                onReticleMove = { rawDelta, transformedDelta ->
                    lastRawDelta = rawDelta
                    lastTransformedDelta = transformedDelta
                    aimReticleOffset += transformedDelta
                },
                onResetReticle = {
                    aimReticleOffset = Offset.Zero
                    bridge.reset()
                }
            )

            // Asymmetric X & Y Axes Configuration Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PaseoCardBackground),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CrimsonBorder, Color(0xFF2A2026))))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SUMBU SENSITIVITAS X & Y (SEPARATE)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = CrimsonGlow
                    )
                    Spacer(Modifier.height(12.dp))

                    // Axis X
                    SensitivitySliderWithInput(
                        label = "Sumbu X (Horizontal / Yaw)",
                        value = activeProfile.sensiX,
                        range = 0.1f..3.0f,
                        step = 0.05f,
                        onValueChange = { newVal ->
                            val updated = activeProfile.copy(sensiX = newVal)
                            viewModel.updateActiveProfile(updated)
                        }
                    )

                    Spacer(Modifier.height(14.dp))

                    // Axis Y
                    SensitivitySliderWithInput(
                        label = "Sumbu Y (Vertikal / Pitch)",
                        value = activeProfile.sensiY,
                        range = 0.1f..3.0f,
                        step = 0.05f,
                        onValueChange = { newVal ->
                            val updated = activeProfile.copy(sensiY = newVal)
                            viewModel.updateActiveProfile(updated)
                        }
                    )
                }
            }

            // Dynamics & Mathematical Curves Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PaseoCardBackground),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CrimsonBorder, Color(0xFF2A2026))))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "KURVA RESPON & FILTRASI DINAMIS (C++ CORE)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = CrimsonGlow
                    )
                    Spacer(Modifier.height(12.dp))

                    // Curve Type Selector
                    Text("Tipe Kurva Respon", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SensitivityCurveType.values().forEach { curve ->
                            val selected = activeProfile.curveType == curve
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selected) CrimsonPrimary else Color(0xFF1E1720))
                                    .border(1.dp, if (selected) CrimsonGlow else Color(0xFF332630), RoundedCornerShape(8.dp))
                                    .clickable {
                                        val updated = activeProfile.copy(curveType = curve)
                                        viewModel.updateActiveProfile(updated)
                                    }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = curve.displayName,
                                    fontSize = 10.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selected) Color.White else Color(0xFFCCCCCC)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Deadzone Slider
                    SensitivitySliderWithInput(
                        label = "Deadzone Filter (Anti Tremor)",
                        value = activeProfile.deadzone,
                        range = 0.00f..0.10f,
                        step = 0.005f,
                        format = "%.3f",
                        onValueChange = { newVal ->
                            val updated = activeProfile.copy(deadzone = newVal)
                            viewModel.updateActiveProfile(updated)
                        }
                    )

                    Spacer(Modifier.height(12.dp))

                    // Smoothing Slider
                    SensitivitySliderWithInput(
                        label = "Smoothing / EMA Filter",
                        value = activeProfile.smoothing,
                        range = 0.00f..0.40f,
                        step = 0.01f,
                        format = "%.2f",
                        onValueChange = { newVal ->
                            val updated = activeProfile.copy(smoothing = newVal)
                            viewModel.updateActiveProfile(updated)
                        }
                    )

                    Spacer(Modifier.height(12.dp))

                    // Acceleration Factor
                    SensitivitySliderWithInput(
                        label = "Velocity Acceleration Multiplier",
                        value = activeProfile.acceleration,
                        range = 0.80f..2.00f,
                        step = 0.05f,
                        format = "%.2fx",
                        onValueChange = { newVal ->
                            val updated = activeProfile.copy(acceleration = newVal)
                            viewModel.updateActiveProfile(updated)
                        }
                    )
                }
            }

            // In-Game Scope Sensitivities (Free Fire / Battle Royale Spec)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PaseoCardBackground),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CrimsonBorder, Color(0xFF2A2026))))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SETELAN IN-GAME SCOPES (0-100)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = CrimsonGlow
                    )
                    Spacer(Modifier.height(12.dp))

                    IntScopeSlider("Lihat Sekeliling (General)", activeProfile.generalSensi) {
                        viewModel.updateActiveProfile(activeProfile.copy(generalSensi = it))
                    }
                    IntScopeSlider("Red Dot Sight", activeProfile.redDotSensi) {
                        viewModel.updateActiveProfile(activeProfile.copy(redDotSensi = it))
                    }
                    IntScopeSlider("Scope 2x", activeProfile.scope2xSensi) {
                        viewModel.updateActiveProfile(activeProfile.copy(scope2xSensi = it))
                    }
                    IntScopeSlider("Scope 4x", activeProfile.scope4xSensi) {
                        viewModel.updateActiveProfile(activeProfile.copy(scope4xSensi = it))
                    }
                    IntScopeSlider("Sniper Scope", activeProfile.sniperSensi) {
                        viewModel.updateActiveProfile(activeProfile.copy(sniperSensi = it))
                    }
                    IntScopeSlider("Lihat Bebas (Free Look)", activeProfile.freeLookSensi) {
                        viewModel.updateActiveProfile(activeProfile.copy(freeLookSensi = it))
                    }
                }
            }

            // Technical Disclaimers Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1318)),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Color(0xFF4A1A24), Color(0xFF251016))))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = CrimsonGlow, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Batasan Teknis Android: Aplikasi tanpa root tidak dapat menginjeksi modifikasi pointer sistem ke game lain secara universal. Modul C++ memproses perhitungan matematika, kalibrasi internal, dan transformasi profil yang dapat diterapkan ke game melalui integrasi DPI Shizuku atau setelan in-game panduan.",
                        fontSize = 11.sp,
                        color = Color(0xFFB0A4AB),
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }

    // Save Dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Simpan Profil Kustom", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Beri nama untuk profil sensitivitas baru ini:", color = Color(0xFFCCCCCC), fontSize = 13.sp)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newProfileName,
                        onValueChange = { newProfileName = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CrimsonPrimary,
                            unfocusedBorderColor = Color(0xFF443340),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newProfileName.isNotBlank()) {
                            val saved = viewModel.sensitivityRepository.saveCustomProfile(
                                activeProfile.copy(name = newProfileName.trim())
                            )
                            viewModel.selectSensitivityProfile(saved)
                            showSaveDialog = false
                            viewModel.showMessage("Profil '${saved.name}' berhasil disimpan.")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Batal", color = Color.Gray)
                }
            },
            containerColor = PaseoCardBackground
        )
    }

    // Export Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Ekspor Konfigurasi JSON", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Salin data konfigurasi JSON profil sensitivitas berikut:", color = Color(0xFFCCCCCC), fontSize = 12.sp)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = exportJsonText,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CrimsonPrimary,
                            unfocusedBorderColor = Color(0xFF443340),
                            focusedTextColor = Color(0xFF88FFAA),
                            unfocusedTextColor = Color(0xFF88FFAA)
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showExportDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
                ) {
                    Text("Selesai")
                }
            },
            containerColor = PaseoCardBackground
        )
    }

    // Import Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Impor Konfigurasi JSON", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Tempel JSON konfigurasi profil sensitivitas:", color = Color(0xFFCCCCCC), fontSize = 12.sp)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = importJsonText,
                        onValueChange = { importJsonText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CrimsonPrimary,
                            unfocusedBorderColor = Color(0xFF443340),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val result = viewModel.sensitivityRepository.importProfilesJson(importJsonText)
                        result.onSuccess { count ->
                            showImportDialog = false
                            viewModel.showMessage("$count profil berhasil diimpor.")
                        }.onFailure {
                            viewModel.showMessage("Format JSON tidak valid.")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
                ) {
                    Text("Impor")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Batal", color = Color.Gray)
                }
            },
            containerColor = PaseoCardBackground
        )
    }
}

@Composable
private fun AimCalibrationPad(
    activeProfile: SensitivityProfile,
    bridge: SensitivityBridge,
    reticleOffset: Offset,
    onReticleMove: (rawDelta: Offset, transformedDelta: Offset) -> Unit,
    onResetReticle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF140D13)),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CrimsonBorder, Color(0xFF2A1520))))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("AREA KALIBRASI BIDIKAN TOUCH", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CrimsonGlow)
                    Text("Sentuh & geser untuk menguji respon C++ real-time", fontSize = 10.sp, color = Color(0xFFAAAAAA))
                }
                IconButton(onClick = onResetReticle, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset Reticle", tint = CrimsonGlow, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0D080C))
                    .border(1.dp, Color(0xFF2E1A25), RoundedCornerShape(10.dp))
                    .pointerInput(activeProfile) {
                        detectDragGestures(
                            onDragStart = { bridge.reset() },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val rawDx = dragAmount.x
                                val rawDy = dragAmount.y
                                val transformed = bridge.processDelta(rawDx, rawDy, 0.016f)
                                onReticleMove(Offset(rawDx, rawDy), Offset(transformed.first, transformed.second))
                            },
                            onDragEnd = { bridge.reset() },
                            onDragCancel = { bridge.reset() }
                        )
                    }
                    .testTag("aim_calibration_surface"),
                contentAlignment = Alignment.Center
            ) {
                // Interactive Aim Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)

                    // Draw grid lines
                    val gridColor = Color(0xFF22151D)
                    val step = 40.dp.toPx()
                    var x = step
                    while (x < size.width) {
                        drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), 1f)
                        x += step
                    }
                    var y = step
                    while (y < size.height) {
                        drawLine(gridColor, Offset(0f, y), Offset(size.width, y), 1f)
                        y += step
                    }

                    // Center cross reference
                    drawLine(Color(0xFF442434), Offset(center.x, 0f), Offset(center.x, size.height), 1.5f)
                    drawLine(Color(0xFF442434), Offset(0f, center.y), Offset(size.width, center.y), 1.5f)
                    drawCircle(Color(0xFF442434), 60.dp.toPx(), center, style = Stroke(1f))

                    // Reticle position clamped to bounds
                    val clampedX = (center.x + reticleOffset.x).coerceIn(20f, size.width - 20f)
                    val clampedY = (center.y + reticleOffset.y).coerceIn(20f, size.height - 20f)
                    val targetPos = Offset(clampedX, clampedY)

                    // Target Reticle
                    drawCircle(CrimsonGlow.copy(alpha = 0.25f), 18.dp.toPx(), targetPos)
                    drawCircle(CrimsonPrimary, 10.dp.toPx(), targetPos, style = Stroke(2.dp.toPx()))
                    drawCircle(Color.White, 3.dp.toPx(), targetPos)
                    drawLine(CrimsonGlow, Offset(targetPos.x - 14.dp.toPx(), targetPos.y), Offset(targetPos.x + 14.dp.toPx(), targetPos.y), 2f)
                    drawLine(CrimsonGlow, Offset(targetPos.x, targetPos.y - 14.dp.toPx()), Offset(targetPos.x, targetPos.y + 14.dp.toPx()), 2f)
                }
            }
        }
    }
}

@Composable
private fun SensitivitySliderWithInput(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    step: Float,
    format: String = "%.2fx",
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text(
                text = String.format(Locale.US, format, value),
                color = CrimsonGlow,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = CrimsonGlow,
                activeTrackColor = CrimsonPrimary,
                inactiveTrackColor = Color(0xFF2E2028)
            )
        )
    }
}

@Composable
private fun IntScopeSlider(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = Color(0xFFDDDDDD), fontSize = 12.sp)
            Text("$value", color = CrimsonGlow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 0f..100f,
            colors = SliderDefaults.colors(
                thumbColor = CrimsonGlow,
                activeTrackColor = CrimsonPrimary,
                inactiveTrackColor = Color(0xFF2E2028)
            )
        )
    }
}
