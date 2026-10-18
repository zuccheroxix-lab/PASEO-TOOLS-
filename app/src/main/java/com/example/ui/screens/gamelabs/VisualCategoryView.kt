package com.example.ui.screens.gamelabs

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CrosshairConfig
import com.example.model.CrosshairShape
import com.example.model.DeviceInfo
import com.example.model.ShizukuInfo
import com.example.model.ShizukuStatus
import com.example.ui.MainViewModel
import com.example.ui.components.CrosshairPreview
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CrimsonGlow
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.PaseoBorderGreen
import com.example.ui.theme.PaseoBorderRed
import com.example.ui.theme.PaseoSurfaceCard
import com.example.ui.theme.PaseoSurfaceDark
import com.example.ui.theme.PaseoSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VisualCategoryView(
    viewModel: MainViewModel,
    shizukuInfo: ShizukuInfo,
    deviceInfo: DeviceInfo?,
    crosshairConfig: CrosshairConfig,
    lastTelemetryTime: Long
) {
    val context = LocalContext.current

    // Resolution State
    var showResolutionDialog by remember { mutableStateOf(false) }
    var selectedResWidth by remember { mutableIntStateOf(deviceInfo?.screenWidthPx ?: 1080) }
    var selectedResHeight by remember { mutableIntStateOf(deviceInfo?.screenHeightPx ?: 2400) }

    // DPI State
    var targetDpi by remember(deviceInfo?.screenDensityDpi) {
        mutableFloatStateOf((deviceInfo?.screenDensityDpi ?: 420).toFloat())
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {

        // ==========================================
        // SECTION A: RESOLUTION MANAGER
        // ==========================================
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PaseoSurfaceDark),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, PaseoBorderGreen.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SectionHeader(icon = Icons.Default.AspectRatio, title = "RESOLUTION MANAGER", badge = "SISTEM LAYAR")

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Ubah resolusi render display secara native melalui perintah ADB Shizuku untuk meningkatkan framerate saat bermain game berat.",
                    color = TextSecondary,
                    fontSize = 11.5.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(PaseoSurfaceCard)
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Resolusi Layar Saat Ini:", color = TextMuted, fontSize = 11.sp)
                        Text(
                            text = "${deviceInfo?.screenWidthPx ?: "-"} x ${deviceInfo?.screenHeightPx ?: "-"} px",
                            color = CrimsonGlow,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Presets
                Text(text = "Pilih Preset Resolusi Valid:", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                val presets = listOf(
                    1080 to 2400,
                    900 to 2000,
                    720 to 1600,
                    1080 to 1920,
                    720 to 1280
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presets.take(3).forEach { (w, h) ->
                        val isSelected = selectedResWidth == w && selectedResHeight == h
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) CrimsonPrimary else PaseoSurfaceElevated)
                                .border(1.dp, if (isSelected) CrimsonGlow else PaseoBorderGreen, RoundedCornerShape(8.dp))
                                .clickable {
                                    selectedResWidth = w
                                    selectedResHeight = h
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${w}x${h}",
                                color = if (isSelected) Color.White else TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Apply & Reset Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showResolutionDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_apply_resolution")
                    ) {
                        Text("Terapkan ${selectedResWidth}x${selectedResHeight}", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { viewModel.resetResolution() },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_reset_resolution")
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp), tint = TextSecondary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset Default", color = TextPrimary, fontSize = 11.5.sp)
                    }
                }
            }
        }

        // ==========================================
        // SECTION B: DPI MANAGER
        // ==========================================
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PaseoSurfaceDark),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, PaseoBorderGreen.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SectionHeader(icon = Icons.Default.Tune, title = "DPI MANAGER", badge = "SENSITIVITAS SCREEN")

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Sesuaikan kepadatan piksel (DPI) layar perangkat. Nilai DPI lebih tinggi memperlebar area pandang dan mempercepat drag-aiming.",
                    color = TextSecondary,
                    fontSize = 11.5.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(PaseoSurfaceCard)
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "DPI Terbaca Saat Ini:", color = TextMuted, fontSize = 11.sp)
                        Text(
                            text = "${deviceInfo?.screenDensityDpi ?: "-"} DPI",
                            color = EmeraldAccent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Target DPI: ${targetDpi.toInt()} DPI", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Batas Aman: 200 - 800", color = TextMuted, fontSize = 10.sp)
                }

                Slider(
                    value = targetDpi,
                    onValueChange = { targetDpi = it },
                    valueRange = 200f..800f,
                    steps = 60,
                    colors = SliderDefaults.colors(
                        thumbColor = CrimsonGlow,
                        activeTrackColor = CrimsonPrimary,
                        inactiveTrackColor = PaseoSurfaceElevated
                    ),
                    modifier = Modifier.testTag("slider_dpi")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.applyDpi(targetDpi.toInt()) },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_apply_dpi")
                    ) {
                        Text("Terapkan ${targetDpi.toInt()} DPI", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { viewModel.resetDpi() },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_reset_dpi")
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp), tint = TextSecondary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset DPI", color = TextPrimary, fontSize = 11.5.sp)
                    }
                }
            }
        }

        // ==========================================
        // SECTION C: ROG MONITORING
        // ==========================================
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PaseoSurfaceDark),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, PaseoBorderGreen.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SectionHeader(icon = Icons.Default.Memory, title = "ROG MONITORING", badge = "TELEMETRI REALTIME")

                Spacer(modifier = Modifier.height(8.dp))
                val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                Text(
                    text = "Pembaruan Terakhir: ${timeFormat.format(Date(lastTelemetryTime))}",
                    color = TextMuted,
                    fontSize = 10.5.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Telemetry Metrics Grid
                val ramTotalGb = String.format(Locale.US, "%.1f", (deviceInfo?.totalRamBytes ?: 0) / (1024.0 * 1024 * 1024))
                val ramAvailGb = String.format(Locale.US, "%.1f", (deviceInfo?.availableRamBytes ?: 0) / (1024.0 * 1024 * 1024))
                val ramUsedGb = String.format(
                    Locale.US, "%.1f",
                    ((deviceInfo?.totalRamBytes ?: 0) - (deviceInfo?.availableRamBytes ?: 0)) / (1024.0 * 1024 * 1024)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricMiniCard(
                        title = "RAM DIGUNAKAN",
                        value = "$ramUsedGb GB",
                        subValue = "Dari $ramTotalGb GB",
                        color = CrimsonGlow,
                        modifier = Modifier.weight(1f)
                    )
                    MetricMiniCard(
                        title = "SUHU BATERAI",
                        value = "${String.format(Locale.US, "%.1f", deviceInfo?.batteryTemperatureCelsius ?: 0f)}°C",
                        subValue = if (deviceInfo?.isCharging == true) "Mengisi Daya" else "Baterai Aktif",
                        color = if ((deviceInfo?.batteryTemperatureCelsius ?: 0f) > 42f) AmberWarning else EmeraldAccent,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricMiniCard(
                        title = "CPU CORES",
                        value = "${deviceInfo?.cpuCores ?: 0} Cores",
                        subValue = deviceInfo?.primaryAbi ?: "-",
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricMiniCard(
                        title = "REFRESH RATE",
                        value = "${deviceInfo?.refreshRateHz?.toInt() ?: 60} Hz",
                        subValue = "Display Refresh",
                        color = EmeraldAccent,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(PaseoSurfaceCard)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Catatan Sistem: Berdasarkan kebijakan keamanan SELinux Android, pembacaan /proc/stat CPU per-prosesor dibatasi pada level aplikasi pihak ketiga. Data RAM, baterai, dan display diperoleh langsung dari Android System Service asli.",
                        color = TextMuted,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }

        // ==========================================
        // SECTION D: AIM ASSISTANT / CUSTOM CROSSHAIR
        // ==========================================
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PaseoSurfaceDark),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, PaseoBorderRed, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SectionHeader(icon = Icons.Default.CenterFocusStrong, title = "AIM ASSISTANT / CUSTOM CROSSHAIR", badge = "OVERLAY RESMI")

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Tampilkan overlay crosshair presisi di atas game. Menggunakan Android WindowManager resmi yang tidak mengintervensi memori game atau melanggar aturan.",
                    color = TextSecondary,
                    fontSize = 11.5.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Permission Warning if overlay not granted
                val canDraw = Settings.canDrawOverlays(context)
                if (!canDraw) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF331805))
                            .border(1.dp, AmberWarning, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Izin Overlay Diperlukan", color = AmberWarning, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Aplikasi memerlukan izin 'Tampilkan di atas aplikasi lain' untuk memunculkan crosshair saat game dimainkan.",
                                color = TextSecondary,
                                fontSize = 10.5.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    val intent = Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:${context.packageName}")
                                    )
                                    context.startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AmberWarning),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.testTag("btn_request_overlay_permission")
                            ) {
                                Text("Buka Pengaturan Izin Overlay", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Main Toggle Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(PaseoSurfaceElevated)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Aktifkan Crosshair Layar", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (crosshairConfig.isEnabled) "Status: Aktif melayang di layar" else "Status: Nonaktif",
                            color = if (crosshairConfig.isEnabled) EmeraldAccent else TextMuted,
                            fontSize = 11.sp
                        )
                    }

                    Switch(
                        checked = crosshairConfig.isEnabled,
                        onCheckedChange = { viewModel.toggleCrosshairOverlay(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = CrimsonPrimary,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = PaseoSurfaceDark
                        ),
                        modifier = Modifier.testTag("switch_crosshair")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Interactive Live Preview
                CrosshairPreview(
                    config = crosshairConfig,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Shape Selector
                Text(text = "Bentuk Reticle Crosshair:", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CrosshairShape.values().take(3).forEach { shape ->
                        val isSelected = crosshairConfig.shape == shape
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) CrimsonPrimary else PaseoSurfaceElevated)
                                .border(1.dp, if (isSelected) CrimsonGlow else PaseoBorderGreen, RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.updateCrosshairConfig(crosshairConfig.copy(shape = shape))
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = shape.displayName.substringBefore(" "),
                                color = if (isSelected) Color.White else TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CrosshairShape.values().drop(3).take(3).forEach { shape ->
                        val isSelected = crosshairConfig.shape == shape
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) CrimsonPrimary else PaseoSurfaceElevated)
                                .border(1.dp, if (isSelected) CrimsonGlow else PaseoBorderGreen, RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.updateCrosshairConfig(crosshairConfig.copy(shape = shape))
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = shape.displayName.substringBefore(" "),
                                color = if (isSelected) Color.White else TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Color Selection
                Text(text = "Pilihan Warna Crosshair:", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                val colorsList = listOf(
                    0xFFFF2E44 to "Crimson Glow",
                    0xFF00E676 to "Neon Emerald",
                    0xFF00E5FF to "Cyber Cyan",
                    0xFFFFEA00 to "Electric Yellow",
                    0xFFFFFFFF to "Pure White"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    colorsList.forEach { (colorVal, _) ->
                        val isSelected = crosshairConfig.colorValue == colorVal
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(colorVal))
                                .border(
                                    if (isSelected) 3.dp else 1.dp,
                                    if (isSelected) Color.White else PaseoBorderGreen,
                                    CircleShape
                                )
                                .clickable {
                                    viewModel.updateCrosshairConfig(crosshairConfig.copy(colorValue = colorVal))
                                }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Size Slider
                CrosshairSlider(
                    label = "Ukuran Crosshair",
                    value = crosshairConfig.sizeDp.toFloat(),
                    range = 14f..54f,
                    displayValue = "${crosshairConfig.sizeDp} dp",
                    onValueChange = {
                        viewModel.updateCrosshairConfig(crosshairConfig.copy(sizeDp = it.toInt()))
                    }
                )

                // Thickness Slider
                CrosshairSlider(
                    label = "Ketebalan Garis",
                    value = crosshairConfig.thicknessDp,
                    range = 1f..6f,
                    displayValue = String.format(Locale.US, "%.1f px", crosshairConfig.thicknessDp),
                    onValueChange = {
                        viewModel.updateCrosshairConfig(crosshairConfig.copy(thicknessDp = it))
                    }
                )

                // Gap Slider
                CrosshairSlider(
                    label = "Jarak Garis (Center Gap)",
                    value = crosshairConfig.gapDp,
                    range = 0f..14f,
                    displayValue = "${crosshairConfig.gapDp.toInt()} dp",
                    onValueChange = {
                        viewModel.updateCrosshairConfig(crosshairConfig.copy(gapDp = it))
                    }
                )

                // Opacity Slider
                CrosshairSlider(
                    label = "Transparansi (Opacity)",
                    value = crosshairConfig.opacity,
                    range = 0.2f..1f,
                    displayValue = "${(crosshairConfig.opacity * 100).toInt()}%",
                    onValueChange = {
                        viewModel.updateCrosshairConfig(crosshairConfig.copy(opacity = it))
                    }
                )

                // Offset X Slider
                CrosshairSlider(
                    label = "Koreksi Sumbu X (Horizontal)",
                    value = crosshairConfig.offsetX.toFloat(),
                    range = -80f..80f,
                    displayValue = "${crosshairConfig.offsetX} px",
                    onValueChange = {
                        viewModel.updateCrosshairConfig(crosshairConfig.copy(offsetX = it.toInt()))
                    }
                )

                // Offset Y Slider
                CrosshairSlider(
                    label = "Koreksi Sumbu Y (Vertikal)",
                    value = crosshairConfig.offsetY.toFloat(),
                    range = -80f..80f,
                    displayValue = "${crosshairConfig.offsetY} px",
                    onValueChange = {
                        viewModel.updateCrosshairConfig(crosshairConfig.copy(offsetY = it.toInt()))
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Reset Offset Button
                OutlinedButton(
                    onClick = {
                        viewModel.updateCrosshairConfig(crosshairConfig.copy(offsetX = 0, offsetY = 0))
                        viewModel.showMessage("Posisi crosshair dikembalikan tepat ke titik tengah layar.")
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp), tint = TextSecondary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reset Posisi ke Tengah (0, 0)", color = TextPrimary, fontSize = 11.5.sp)
                }
            }
        }
    }

    // Resolution Confirmation Dialog
    if (showResolutionDialog) {
        AlertDialog(
            onDismissRequest = { showResolutionDialog = false },
            title = {
                Text("Konfirmasi Perubahan Resolusi", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Text(
                    "Apakah Anda yakin ingin mengubah resolusi ke ${selectedResWidth}x${selectedResHeight} px?\n\nJika layar mengalami ketidakcocokan aspek rasio, Anda dapat menekan tombol 'Reset Default' kapan saja.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResolutionDialog = false
                        viewModel.applyResolution(selectedResWidth, selectedResHeight)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
                ) {
                    Text("Terapkan", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResolutionDialog = false }) {
                    Text("Batal", color = TextSecondary)
                }
            },
            containerColor = PaseoSurfaceElevated
        )
    }
}

@Composable
private fun SectionHeader(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, badge: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = CrimsonGlow, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF1B2822))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(text = badge, color = EmeraldAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun MetricMiniCard(title: String, value: String, subValue: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(PaseoSurfaceCard)
            .border(0.5.dp, PaseoBorderGreen.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Column {
            Text(text = title, color = TextMuted, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(3.dp))
            Text(text = value, color = color, fontSize = 14.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subValue, color = TextSecondary, fontSize = 9.5.sp)
        }
    }
}

@Composable
private fun CrosshairSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    displayValue: String,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, color = TextSecondary, fontSize = 11.sp)
            Text(text = displayValue, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = CrimsonGlow,
                activeTrackColor = CrimsonPrimary,
                inactiveTrackColor = PaseoSurfaceElevated
            )
        )
    }
}
