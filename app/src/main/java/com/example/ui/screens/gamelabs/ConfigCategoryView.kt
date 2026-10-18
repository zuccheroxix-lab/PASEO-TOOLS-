package com.example.ui.screens.gamelabs

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SensitivityProfile
import com.example.model.ShizukuInfo
import com.example.model.ShizukuStatus
import com.example.ui.MainViewModel
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CrimsonContainer
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

@Composable
fun ConfigCategoryView(
    viewModel: MainViewModel,
    shizukuInfo: ShizukuInfo,
    activeProfile: SensitivityProfile,
    pointerLocationActive: Boolean
) {
    // Editable Sensitivity State
    var generalSensi by remember(activeProfile) { mutableIntStateOf(activeProfile.generalSensi) }
    var redDotSensi by remember(activeProfile) { mutableIntStateOf(activeProfile.redDotSensi) }
    var scope2xSensi by remember(activeProfile) { mutableIntStateOf(activeProfile.scope2xSensi) }
    var scope4xSensi by remember(activeProfile) { mutableIntStateOf(activeProfile.scope4xSensi) }
    var sniperSensi by remember(activeProfile) { mutableIntStateOf(activeProfile.sniperSensi) }
    var freeLookSensi by remember(activeProfile) { mutableIntStateOf(activeProfile.freeLookSensi) }
    var aimTrickTuning by remember(activeProfile) { mutableIntStateOf(activeProfile.aimTrickTuningPercent) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {

        // ==========================================
        // SECTION A: SENSI PACK (PROFIL SENSITIVITAS)
        // ==========================================
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PaseoSurfaceDark),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, PaseoBorderGreen.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SectionHeader(icon = Icons.Default.Bolt, title = "SENSI PACK — PROFIL", badge = "SENSITIVITAS")

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Konfigurasi profil sensitivitas drag-headshot dan recoil control. Dioptimalkan secara lokal untuk mapping sensitivitas sentuhan.",
                    color = TextSecondary,
                    fontSize = 11.5.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Presets Grid
                Text(text = "Pilih Preset Resmi ZUCCHERO XANN:", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                SensitivityProfile.PRESETS.forEach { preset ->
                    val isSelected = activeProfile.id == preset.id
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) CrimsonContainer else PaseoSurfaceCard)
                            .border(1.dp, if (isSelected) CrimsonGlow else PaseoBorderGreen.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .clickable { viewModel.selectSensitivityProfile(preset) }
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = preset.name,
                                    color = if (isSelected) CrimsonGlow else TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = preset.description,
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    maxLines = 1
                                )
                            }
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = "Active", tint = CrimsonGlow, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Sliders for 6 Sensitivity Axes
                Text(text = "Sesuaikan Parameter Sensitivitas (0 - 100):", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                SensiSlider(label = "Sensitivitas Umum (Lihat Sekitar)", value = generalSensi, onValueChange = { generalSensi = it })
                SensiSlider(label = "Red Dot Scope", value = redDotSensi, onValueChange = { redDotSensi = it })
                SensiSlider(label = "2x Scope", value = scope2xSensi, onValueChange = { scope2xSensi = it })
                SensiSlider(label = "4x Scope", value = scope4xSensi, onValueChange = { scope4xSensi = it })
                SensiSlider(label = "Sniper / AWM Scope", value = sniperSensi, onValueChange = { sniperSensi = it })
                SensiSlider(label = "Kamera Bebas (Free Look)", value = freeLookSensi, onValueChange = { freeLookSensi = it })

                Spacer(modifier = Modifier.height(10.dp))

                // Save Custom Profile Button
                Button(
                    onClick = {
                        val updated = activeProfile.copy(
                            generalSensi = generalSensi,
                            redDotSensi = redDotSensi,
                            scope2xSensi = scope2xSensi,
                            scope4xSensi = scope4xSensi,
                            sniperSensi = sniperSensi,
                            freeLookSensi = freeLookSensi,
                            aimTrickTuningPercent = aimTrickTuning,
                            isCustom = true
                        )
                        viewModel.updateActiveProfile(updated)
                        viewModel.showMessage("Profil sensitivitas berhasil disimpan!")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_save_sensi_profile")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Simpan Konfigurasi Profil", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "*Catatan: Profil disimpan secara lokal dan berlaku sebagai parameter kalibrasi panduan.",
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }
        }

        // ==========================================
        // SECTION B: TOUCH UP & POINTER LOCATION
        // ==========================================
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PaseoSurfaceDark),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, PaseoBorderGreen.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SectionHeader(icon = Icons.Default.TouchApp, title = "TOUCH UP & POINTER", badge = "SISTEM SENTUH")

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Optimalkan responsivitas sentuhan Android dan periksa akurasi koordinat sentuhan layar (Touch Tracker) via Shizuku.",
                    color = TextSecondary,
                    fontSize = 11.5.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Pointer Location Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(PaseoSurfaceElevated)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Tampilkan Pointer Location (ADB)", color = TextPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (pointerLocationActive) "Garis koordinat sentuh aktif" else "Koordinat layar nonaktif",
                            color = if (pointerLocationActive) EmeraldAccent else TextMuted,
                            fontSize = 10.5.sp
                        )
                    }

                    Switch(
                        checked = pointerLocationActive,
                        onCheckedChange = { viewModel.togglePointerLocation() },
                        enabled = shizukuInfo.status == ShizukuStatus.CONNECTED && shizukuInfo.isPermissionGranted,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = CrimsonPrimary,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = PaseoSurfaceDark
                        ),
                        modifier = Modifier.testTag("switch_pointer_location")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Touch optimization recommendations
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(PaseoSurfaceCard)
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "Rekomendasi Responsivitas Sentuhan Android:", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(text = "• Kecepatan Pointer Android: Setel pada level 4 atau 5.", color = TextSecondary, fontSize = 10.5.sp)
                        Text(text = "• Skala Animasi Jendela: Setel ke 0.5x di Opsi Pengembang untuk transisi instan.", color = TextSecondary, fontSize = 10.5.sp)
                        Text(text = "• Waktu tunggu sentuh & tahan: Singkat (Short).", color = TextSecondary, fontSize = 10.5.sp)
                    }
                }
            }
        }

        // ==========================================
        // SECTION C: AIM TRICK 15% (ETHICAL MICRO-TUNING)
        // ==========================================
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PaseoSurfaceDark),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, PaseoBorderRed, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SectionHeader(icon = Icons.Default.GpsFixed, title = "AIM TRICK 15% — MICRO CALIBRATION", badge = "DYNAMIC CURVE")

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Profil kalibrasi mikro-akselerasi sentuhan 15% dari ZUCCHERO XANN. Menghaluskan pergerakan pointer saat drag shot tanpa menyentuh file internal game atau menggunakan cheat ilegal.",
                    color = TextSecondary,
                    fontSize = 11.5.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Tuning Curve: $aimTrickTuning%", color = CrimsonGlow, fontSize = 13.sp, fontWeight = FontWeight.Black)
                    Text(text = "Maksimum Aman: 15%", color = TextMuted, fontSize = 10.5.sp)
                }

                Slider(
                    value = aimTrickTuning.toFloat(),
                    onValueChange = { aimTrickTuning = it.toInt() },
                    valueRange = 0f..15f,
                    steps = 14,
                    colors = SliderDefaults.colors(
                        thumbColor = CrimsonGlow,
                        activeTrackColor = CrimsonPrimary,
                        inactiveTrackColor = PaseoSurfaceElevated
                    ),
                    modifier = Modifier.testTag("slider_aim_trick")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F1B14))
                        .border(0.5.dp, EmeraldAccent.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Kebijakan Fair Play: Fitur ini tidak berisi memory injection, aimbot, atau modifikasi ilegal. Nilai tuning diterapkan sebagai profil akselerasi sentuh lokal terverifikasi.",
                        color = EmeraldAccent,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(icon: ImageVector, title: String, badge: String) {
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
private fun SensiSlider(label: String, value: Int, onValueChange: (Int) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, color = TextSecondary, fontSize = 11.sp)
            Text(text = "$value", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 0f..100f,
            colors = SliderDefaults.colors(
                thumbColor = CrimsonGlow,
                activeTrackColor = CrimsonPrimary,
                inactiveTrackColor = PaseoSurfaceElevated
            )
        )
    }
}
