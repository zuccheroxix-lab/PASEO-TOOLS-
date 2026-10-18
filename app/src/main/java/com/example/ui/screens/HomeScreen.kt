package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DeviceInfo
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
import com.example.ui.theme.PaseoDarkBackground
import com.example.ui.theme.PaseoSurfaceCard
import com.example.ui.theme.PaseoSurfaceDark
import com.example.ui.theme.PaseoSurfaceElevated
import com.example.ui.theme.SilverBadge
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    shizukuInfo: ShizukuInfo,
    deviceInfo: DeviceInfo?
) {
    val context = LocalContext.current
    var isGuideExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PaseoDarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(6.dp)) }

        // Card 1: Status Shizuku & Perangkat
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PaseoSurfaceDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (shizukuInfo.status == ShizukuStatus.CONNECTED && shizukuInfo.isPermissionGranted) {
                            PaseoBorderGreen
                        } else {
                            PaseoBorderRed
                        },
                        RoundedCornerShape(16.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Security",
                                tint = CrimsonGlow,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "KONTROL LAYANAN SHIZUKU",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }

                        val statusBadgeColor = when (shizukuInfo.status) {
                            ShizukuStatus.CONNECTED -> EmeraldAccent
                            ShizukuStatus.DISCONNECTED -> AmberWarning
                            ShizukuStatus.UNSUPPORTED -> Color(0xFFFF5252)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(statusBadgeColor.copy(alpha = 0.15f))
                                .border(0.5.dp, statusBadgeColor, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = when (shizukuInfo.status) {
                                    ShizukuStatus.CONNECTED -> "CONNECTED"
                                    ShizukuStatus.DISCONNECTED -> "DISCONNECTED"
                                    ShizukuStatus.UNSUPPORTED -> "UNSUPPORTED"
                                },
                                color = statusBadgeColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Detail Metrics Grid
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(PaseoSurfaceCard)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        InfoRow(label = "Versi Android", value = "Android ${deviceInfo?.androidVersion ?: "Memeriksa..."} (API ${deviceInfo?.apiLevel ?: "-"})")
                        InfoRow(label = "Arsitektur ABI", value = deviceInfo?.primaryAbi ?: "Memeriksa...")
                        InfoRow(
                            label = "Versi API Shizuku",
                            value = if (shizukuInfo.status == ShizukuStatus.CONNECTED) "v${shizukuInfo.apiVersion}.${shizukuInfo.patchVersion}" else "Tidak Aktif"
                        )
                        InfoRow(
                            label = "Status Izin Shizuku",
                            value = if (shizukuInfo.isPermissionGranted) "Izin Diberikan (Granted)" else "Belum Diberikan (Denied)",
                            valueColor = if (shizukuInfo.isPermissionGranted) EmeraldAccent else AmberWarning
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = shizukuInfo.statusMessage,
                        color = TextSecondary,
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.checkShizuku() },
                            colors = ButtonDefaults.buttonColors(containerColor = PaseoSurfaceElevated),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_check_shizuku")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp), tint = TextPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cek Status", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.requestShizukuPermission() },
                            enabled = shizukuInfo.status == ShizukuStatus.CONNECTED && !shizukuInfo.isPermissionGranted,
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("btn_request_permission")
                        ) {
                            Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Minta Izin", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Card 2: Panduan Memulai Shizuku (Expandable)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PaseoSurfaceDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PaseoBorderGreen.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isGuideExpanded = !isGuideExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.HelpOutline, contentDescription = null, tint = CrimsonGlow, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Panduan Setup Shizuku (Wireless Debugging)",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Icon(
                            imageVector = if (isGuideExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Expand",
                            tint = TextSecondary
                        )
                    }

                    AnimatedVisibility(visible = isGuideExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GuideStep(
                                number = "1",
                                title = "Aktifkan Opsi Pengembang",
                                desc = "Buka Pengaturan HP > Tentang Ponsel > Ketuk 'Nomor Versi' 7 kali."
                            )
                            GuideStep(
                                number = "2",
                                title = "Aktifkan Wireless Debugging (Android 11+)",
                                desc = "Di Opsi Pengembang, hubungkan ke Wi-Fi dan aktifkan 'Proses debug nirkabel' (Wireless Debugging)."
                            )
                            GuideStep(
                                number = "3",
                                title = "Pairing di Aplikasi Shizuku",
                                desc = "Buka aplikasi Shizuku > Pilih 'Pairing' > Masukkan kode 6 digit dari Wireless Debugging."
                            )
                            GuideStep(
                                number = "4",
                                title = "Mulai Layanan (Start)",
                                desc = "Setelah pairing selesai, tekan tombol 'Start' di Shizuku, lalu kembali ke PASEO SENSI dan tekan 'Cek Status'."
                            )

                            // ADB Command for PC / Fallback
                            val adbCommand = "adb shell sh /sdcard/Android/data/moe.shizuku.privileged.api/start.sh"
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF090E0C))
                                    .border(0.5.dp, PaseoBorderGreen, RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "Perintah ADB Terminal (PC):", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy Command",
                                            tint = CrimsonGlow,
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clickable {
                                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                    clipboard.setPrimaryClip(ClipData.newPlainText("ADB Command", adbCommand))
                                                    viewModel.showMessage("Perintah ADB disalin ke clipboard.")
                                                }
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = adbCommand,
                                        color = EmeraldAccent,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Card 3: Profil Developer Resmi ZUCCHERO XANN
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PaseoSurfaceDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PaseoBorderRed, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(CrimsonPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "ZX",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "ZUCCHERO XANN",
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "TITLE: DEVELOPER",
                                    color = CrimsonGlow,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF1E2823))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "OFFICIAL BUILD",
                                color = SilverBadge,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Aplikasi PASEO SENSI dikembangkan secara eksklusif dengan arsitektur native Android Kotlin untuk memastikan performa gaming optimal, latensi sentuhan minimum, dan kalibrasi sistem nyata tanpa modifikasi ilegal.",
                        color = TextSecondary,
                        fontSize = 11.5.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        // Card 4: Support Me (Dukung Pengembang)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PaseoSurfaceDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PaseoBorderGreen.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Favorite, contentDescription = null, tint = CrimsonGlow, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DUKUNG PENGEMBANG (SUPPORT ME)",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Dukung pengembangan berkelanjutan PASEO SENSI melalui repositori GitHub resmi atau kanal donasi pengembang.",
                        color = TextSecondary,
                        fontSize = 11.5.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val url = viewModel.preferencesRepository.supportLink
                                try {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                } catch (e: Exception) {
                                    viewModel.showMessage("Tidak dapat membuka tautan: $url")
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(15.dp), tint = CrimsonGlow)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("GitHub Repo", color = TextPrimary, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = {
                                val url = viewModel.preferencesRepository.bugReportLink
                                try {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                } catch (e: Exception) {
                                    viewModel.showMessage("Tidak dapat membuka tautan bug report.")
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.BugReport, contentDescription = null, modifier = Modifier.size(15.dp), tint = AmberWarning)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Lapor Bug", color = TextPrimary, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun InfoRow(label: String, value: String, valueColor: Color = TextPrimary) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextMuted, fontSize = 11.5.sp)
        Text(text = value, color = valueColor, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun GuideStep(number: String, title: String, desc: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(CrimsonContainer)
                .border(0.5.dp, CrimsonGlow, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = number, color = CrimsonGlow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(text = desc, color = TextSecondary, fontSize = 11.sp, lineHeight = 15.sp)
        }
    }
}
