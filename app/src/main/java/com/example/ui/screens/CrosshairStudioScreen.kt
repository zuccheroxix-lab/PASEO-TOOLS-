package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.grid.GridCells
import android.content.ClipData
import android.content.ClipboardManager
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Visibility
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
import androidx.compose.material3.Switch
import androidx.compose.material3.TextButton
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.CrosshairConfig
import com.example.model.CrosshairShape
import com.example.ui.MainViewModel
import com.example.ui.theme.CrimsonGlow
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.PaseoBorderDark
import com.example.ui.theme.PaseoDarkBackground
import com.example.ui.theme.PaseoSurfaceCard
import com.example.ui.theme.PaseoSurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun CrosshairStudioScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentConfig by viewModel.crosshairConfig.collectAsStateWithLifecycle()

    var activeTab by remember { mutableIntStateOf(0) } // 0 = Gallery, 1 = Editor
    var selectedFilter by remember { mutableStateOf("All") }

    var shape by remember(currentConfig) { mutableStateOf(currentConfig.shape) }
    var sizeDp by remember(currentConfig) { mutableFloatStateOf(currentConfig.sizeDp.toFloat()) }
    var offsetX by remember(currentConfig) { mutableFloatStateOf(currentConfig.offsetX.toFloat()) }
    var offsetY by remember(currentConfig) { mutableFloatStateOf(currentConfig.offsetY.toFloat()) }
    var selectedColor by remember(currentConfig) { mutableLongStateOf(currentConfig.colorValue) }
    var opacity by remember(currentConfig) { mutableFloatStateOf(currentConfig.opacity) }
    var thicknessDp by remember(currentConfig) { mutableFloatStateOf(currentConfig.thicknessDp) }

    var showImportDialog by remember { mutableStateOf(false) }
    var importJsonText by remember { mutableStateOf("") }
    var showSavePresetDialog by remember { mutableStateOf(false) }
    var presetNameInput by remember { mutableStateOf("") }

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
                    Text(
                        text = if (activeTab == 0) "Crosshair Gallery" else "Crosshair Editor",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Overlay Active Indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (currentConfig.isEnabled) "Overlay On" else "Overlay Off",
                        color = if (currentConfig.isEnabled) Color(0xFF00E676) else TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Switch(
                        checked = currentConfig.isEnabled,
                        onCheckedChange = { target ->
                            if (target && !Settings.canDrawOverlays(context)) {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                                )
                                context.startActivity(intent)
                                viewModel.showMessage("Berikan izin 'Tampilkan di atas aplikasi lain'")
                            } else {
                                viewModel.toggleCrosshairOverlay(target)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = CrimsonPrimary,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = Color(0xFF20202A)
                        )
                    )
                }
            }

            // Top Segment Switch: Gallery / Editor
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PaseoSurfaceDark)
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (activeTab == 0) CrimsonPrimary else Color.Transparent)
                        .clickable { activeTab = 0 }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Gallery",
                        color = if (activeTab == 0) Color.White else TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (activeTab == 1) CrimsonPrimary else Color.Transparent)
                        .clickable { activeTab = 1 }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Editor",
                        color = if (activeTab == 1) Color.White else TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Content according to active tab
            if (activeTab == 0) {
                // GALLERY VIEW
                Column(modifier = Modifier.fillMaxSize()) {
                    // Filter Chips Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("All", "Basic", "Pro", "Custom").forEach { filter ->
                            FilterChip(
                                selected = selectedFilter == filter,
                                onClick = { selectedFilter = filter },
                                label = { Text(text = filter, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CrimsonPrimary,
                                    selectedLabelColor = Color.White,
                                    containerColor = PaseoSurfaceCard,
                                    labelColor = TextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = PaseoBorderDark,
                                    selectedBorderColor = CrimsonPrimary,
                                    enabled = true,
                                    selected = selectedFilter == filter
                                )
                            )
                        }
                    }

                    // Gallery Grid of Reticles
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        val galleryShapes = CrosshairShape.values()
                        val colorPresets = listOf(
                            0xFFFF2E44, 0xFF00E676, 0xFF2979FF, 0xFF00E5FF, 0xFFFFD700, 0xFFFFFFFF
                        )

                        items(galleryShapes.size * 2) { index ->
                            val itemShape = galleryShapes[index % galleryShapes.size]
                            val itemColor = colorPresets[index % colorPresets.size]
                            val isSelected = shape == itemShape && selectedColor == itemColor

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(96.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .border(
                                        if (isSelected) 1.5.dp else 1.dp,
                                        if (isSelected) CrimsonGlow else PaseoBorderDark,
                                        RoundedCornerShape(14.dp)
                                    )
                                    .clickable {
                                        shape = itemShape
                                        selectedColor = itemColor
                                        activeTab = 1 // Switch to Editor
                                    },
                                colors = CardDefaults.cardColors(containerColor = PaseoSurfaceCard)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Canvas(modifier = Modifier.size(46.dp)) {
                                        drawCrosshairCanvas(
                                            shape = itemShape,
                                            color = Color(itemColor),
                                            size = size.minDimension,
                                            thickness = 2.5f.dp.toPx(),
                                            gap = 5.dp.toPx()
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Bottom "+ Buat Crosshair" button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Button(
                            onClick = { activeTab = 1 },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .testTag("btn_buat_crosshair"),
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Buat / Edit Crosshair",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            } else {
                // EDITOR VIEW
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Live Game Preview Card
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .border(1.dp, PaseoBorderDark, RoundedCornerShape(18.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF13151A))
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                // Background game grid canvas
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val w = size.width
                                    val h = size.height
                                    // Grid lines
                                    drawLine(Color(0xFF20232C), Offset(w / 2, 0f), Offset(w / 2, h), 1f)
                                    drawLine(Color(0xFF20232C), Offset(0f, h / 2), Offset(w, h / 2), 1f)
                                }

                                // Reticle in center with offset
                                Canvas(modifier = Modifier.size(sizeDp.dp)) {
                                    val centerOffset = Offset(
                                        x = size.width / 2 + offsetX.dp.toPx(),
                                        y = size.height / 2 + offsetY.dp.toPx()
                                    )
                                    drawCrosshairCanvas(
                                        shape = shape,
                                        color = Color(selectedColor).copy(alpha = opacity),
                                        size = sizeDp.dp.toPx(),
                                        thickness = thicknessDp.dp.toPx(),
                                        gap = 4.dp.toPx(),
                                        centerOverride = centerOffset
                                    )
                                }

                                // Preview Badge
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(10.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF1E2028))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Visibility,
                                            contentDescription = null,
                                            tint = CrimsonGlow,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Preview",
                                            color = TextSecondary,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Sliders
                    item {
                        SliderRow(
                            label = "Ukuran",
                            value = sizeDp,
                            range = 14f..54f,
                            onValueChange = { sizeDp = it }
                        )
                    }

                    item {
                        SliderRow(
                            label = "Posisi X",
                            value = offsetX,
                            range = -50f..50f,
                            onValueChange = { offsetX = it }
                        )
                    }

                    item {
                        SliderRow(
                            label = "Posisi Y",
                            value = offsetY,
                            range = -50f..50f,
                            onValueChange = { offsetY = it }
                        )
                    }

                    // Color Picker Row
                    item {
                        Text(
                            text = "Warna",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            listOf(
                                0xFFFF2E44, // Crimson Red
                                0xFF00E676, // Emerald Green
                                0xFF2979FF, // Electric Blue
                                0xFF00E5FF, // Cyber Cyan
                                0xFFFFD700, // Gold Yellow
                                0xFFFFFFFF  // Pure White
                            ).forEach { cVal ->
                                val isChosen = selectedColor == cVal
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(cVal))
                                        .border(
                                            if (isChosen) 2.5.dp else 1.dp,
                                            if (isChosen) Color.White else Color.Transparent,
                                            CircleShape
                                        )
                                        .clickable { selectedColor = cVal }
                                )
                            }
                        }
                    }

                    // Kecerahan / Opacity
                    item {
                        SliderRow(
                            label = "Kecerahan / Opacity",
                            value = opacity * 100f,
                            range = 20f..100f,
                            onValueChange = { opacity = it / 100f }
                        )
                    }

                    // Ketebalan / Blur
                    item {
                        SliderRow(
                            label = "Ketebalan Garis",
                            value = thicknessDp,
                            range = 1f..6f,
                            onValueChange = { thicknessDp = it }
                        )
                    }

                    // Action Buttons: Reset & Simpan
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    sizeDp = 26f
                                    offsetX = 0f
                                    offsetY = 0f
                                    selectedColor = 0xFFFF2E44
                                    opacity = 0.95f
                                    thicknessDp = 2.5f
                                    viewModel.showMessage("Pengaturan crosshair direset.")
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22222D))
                            ) {
                                Text("Reset", color = TextSecondary)
                            }

                            Button(
                                onClick = {
                                    val updated = currentConfig.copy(
                                        shape = shape,
                                        sizeDp = sizeDp.toInt(),
                                        offsetX = offsetX.toInt(),
                                        offsetY = offsetY.toInt(),
                                        colorValue = selectedColor,
                                        opacity = opacity,
                                        thicknessDp = thicknessDp
                                    )
                                    viewModel.updateCrosshairConfig(updated)
                                    viewModel.showMessage("Crosshair berhasil disimpan.")
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
                            ) {
                                Text("Simpan", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Export, Import, and Preset Save Buttons
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val current = currentConfig.copy(
                                        shape = shape,
                                        sizeDp = sizeDp.toInt(),
                                        offsetX = offsetX.toInt(),
                                        offsetY = offsetY.toInt(),
                                        colorValue = selectedColor,
                                        opacity = opacity,
                                        thicknessDp = thicknessDp
                                    )
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("CrosshairJSON", current.toJson()))
                                    viewModel.showMessage("Format JSON berhasil disalin ke clipboard!")
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1F29))
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Ekspor", fontSize = 11.sp)
                            }

                            Button(
                                onClick = { showImportDialog = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1F29))
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Impor", fontSize = 11.sp)
                            }

                            Button(
                                onClick = { showSavePresetDialog = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1F29))
                            ) {
                                Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Preset", fontSize = 11.sp)
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
            }
        }

        // Import Dialog
        if (showImportDialog) {
            AlertDialog(
                onDismissRequest = { showImportDialog = false },
                title = { Text("Impor Crosshair dari JSON", color = Color.White) },
                text = {
                    OutlinedTextField(
                        value = importJsonText,
                        onValueChange = { importJsonText = it },
                        label = { Text("Tempel JSON di sini") },
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
                            val parsed = com.example.model.CrosshairConfig.fromJson(importJsonText)
                            if (parsed != null) {
                                shape = parsed.shape
                                sizeDp = parsed.sizeDp.toFloat()
                                offsetX = parsed.offsetX.toFloat()
                                offsetY = parsed.offsetY.toFloat()
                                selectedColor = parsed.colorValue
                                opacity = parsed.opacity
                                thicknessDp = parsed.thicknessDp
                                viewModel.updateCrosshairConfig(parsed)
                                viewModel.showMessage("Crosshair berhasil diimpor!")
                                showImportDialog = false
                                importJsonText = ""
                            } else {
                                viewModel.showMessage("Format JSON tidak valid!")
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

        // Save Custom Preset Dialog
        if (showSavePresetDialog) {
            AlertDialog(
                onDismissRequest = { showSavePresetDialog = false },
                title = { Text("Simpan Sebagai Preset Kustom", color = Color.White) },
                text = {
                    OutlinedTextField(
                        value = presetNameInput,
                        onValueChange = { presetNameInput = it },
                        label = { Text("Nama Preset (misal: Crimson Sniper Dot)") },
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
                            if (presetNameInput.isNotBlank()) {
                                val current = currentConfig.copy(
                                    shape = shape,
                                    sizeDp = sizeDp.toInt(),
                                    offsetX = offsetX.toInt(),
                                    offsetY = offsetY.toInt(),
                                    colorValue = selectedColor,
                                    opacity = opacity,
                                    thicknessDp = thicknessDp
                                )
                                val newPreset = com.example.model.CrosshairPreset(
                                    id = "ch_cust_" + java.util.UUID.randomUUID().toString().take(8),
                                    name = presetNameInput.trim(),
                                    config = current,
                                    isCustom = true
                                )
                                viewModel.preferencesRepository.saveCustomCrosshairPreset(newPreset)
                                viewModel.showMessage("Preset '${newPreset.name}' berhasil disimpan!")
                                showSavePresetDialog = false
                                presetNameInput = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
                    ) {
                        Text("Simpan")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSavePresetDialog = false }) {
                        Text("Batal", color = TextMuted)
                    }
                },
                containerColor = PaseoSurfaceCard
            )
        }
    }
}

@Composable
fun SliderRow(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${value.toInt()}",
                color = TextPrimary,
                fontSize = 12.sp,
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
                inactiveTrackColor = Color(0xFF262634)
            )
        )
    }
}

fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCrosshairCanvas(
    shape: CrosshairShape,
    color: Color,
    size: Float,
    thickness: Float,
    gap: Float,
    centerOverride: Offset? = null
) {
    val centerPt = centerOverride ?: Offset(this.size.width / 2, this.size.height / 2)
    val armLength = (size - gap) / 2

    when (shape) {
        CrosshairShape.DOT -> {
            drawCircle(color = color, radius = size / 3, center = centerPt)
        }
        CrosshairShape.CLASSIC_CROSS -> {
            // Horizontal arms
            drawLine(color, Offset(centerPt.x - gap / 2 - armLength, centerPt.y), Offset(centerPt.x - gap / 2, centerPt.y), thickness)
            drawLine(color, Offset(centerPt.x + gap / 2, centerPt.y), Offset(centerPt.x + gap / 2 + armLength, centerPt.y), thickness)
            // Vertical arms
            drawLine(color, Offset(centerPt.x, centerPt.y - gap / 2 - armLength), Offset(centerPt.x, centerPt.y - gap / 2), thickness)
            drawLine(color, Offset(centerPt.x, centerPt.y + gap / 2), Offset(centerPt.x, centerPt.y + gap / 2 + armLength), thickness)
            drawCircle(color, radius = thickness * 0.8f, center = centerPt)
        }
        CrosshairShape.CIRCLE_CROSS -> {
            drawCircle(color, radius = size / 2, center = centerPt, style = Stroke(width = thickness))
            drawLine(color, Offset(centerPt.x - size / 2, centerPt.y), Offset(centerPt.x + size / 2, centerPt.y), thickness)
            drawLine(color, Offset(centerPt.x, centerPt.y - size / 2), Offset(centerPt.x, centerPt.y + size / 2), thickness)
        }
        CrosshairShape.TACTICAL_T -> {
            drawLine(color, Offset(centerPt.x - gap / 2 - armLength, centerPt.y), Offset(centerPt.x - gap / 2, centerPt.y), thickness)
            drawLine(color, Offset(centerPt.x + gap / 2, centerPt.y), Offset(centerPt.x + gap / 2 + armLength, centerPt.y), thickness)
            drawLine(color, Offset(centerPt.x, centerPt.y + gap / 2), Offset(centerPt.x, centerPt.y + gap / 2 + armLength), thickness)
            drawCircle(color, radius = thickness, center = centerPt)
        }
        CrosshairShape.SNIPER_CIRCLE -> {
            drawCircle(color, radius = size / 2, center = centerPt, style = Stroke(width = thickness))
            drawCircle(color, radius = thickness * 1.5f, center = centerPt)
        }
        CrosshairShape.DIAMOND -> {
            drawCircle(color, radius = size / 2.5f, center = centerPt, style = Stroke(width = thickness))
            drawCircle(color, radius = thickness, center = centerPt)
        }
    }
}
