package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DeviceInfo
import com.example.model.ShizukuInfo
import com.example.model.ShizukuStatus
import com.example.ui.MainTab
import com.example.ui.MainViewModel
import com.example.ui.SubScreen
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CrimsonContainer
import com.example.ui.theme.CrimsonDark
import com.example.ui.theme.CrimsonGlow
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.PaseoBorderDark
import com.example.ui.theme.PaseoBorderGlow
import com.example.ui.theme.PaseoBorderRed
import com.example.ui.theme.PaseoDarkBackground
import com.example.ui.theme.PaseoSurfaceCard
import com.example.ui.theme.PaseoSurfaceDark
import com.example.ui.theme.PaseoSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VerifiedBlue

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    shizukuInfo: ShizukuInfo,
    deviceInfo: DeviceInfo?
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PaseoDarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Developer Profile Card (Matching Reference Design)
        item {
            DeveloperProfileCard(
                uid = "ZX-OWNER-001",
                onCopyUid = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Developer UID", "ZX-OWNER-001"))
                    viewModel.showMessage("UID Developer tersalin: ZX-OWNER-001")
                }
            )
        }

        // 2. System Status Card with 4 Circular Gauges
        item {
            SystemStatusCard(
                deviceInfo = deviceInfo,
                onRefresh = { viewModel.loadDeviceInfo() }
            )
        }

        // 3. Main Navigation Cards Grid/List (Matching Reference)
        item {
            Text(
                text = "MENU UTAMA",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 2.dp)
            )
        }

        item {
            MenuActionCard(
                title = "Game Launcher",
                subtitle = "Kelola Game & Optimasi",
                icon = Icons.Default.SportsEsports,
                iconTint = CrimsonGlow,
                onClick = { viewModel.selectTab(MainTab.GAME) },
                testTag = "menu_game_launcher"
            )
        }

        item {
            MenuActionCard(
                title = "Tools",
                subtitle = "Fitur Lengkap & Kalibrasi Sensi",
                icon = Icons.Default.Build,
                iconTint = Color(0xFFFF9100),
                onClick = { viewModel.selectTab(MainTab.TOOLS) },
                testTag = "menu_tools"
            )
        }

        item {
            MenuActionCard(
                title = "Pengaturan",
                subtitle = "Kustomisasi & Profil",
                icon = Icons.Default.Settings,
                iconTint = Color(0xFF00E5FF),
                onClick = { viewModel.navigateToSubScreen(SubScreen.SETTINGS_PAGE) },
                testTag = "menu_settings"
            )
        }

        item {
            MenuActionCard(
                title = "Crosshair",
                subtitle = "Overlay & Editor",
                icon = Icons.Default.CenterFocusStrong,
                iconTint = CrimsonPrimary,
                onClick = { viewModel.navigateToSubScreen(SubScreen.CROSSHAIR_GALLERY) },
                testTag = "menu_crosshair"
            )
        }

        // 4. Shizuku Quick Status Banner
        item {
            ShizukuBannerCard(
                shizukuInfo = shizukuInfo,
                onClick = { viewModel.navigateToSubScreen(SubScreen.SHIZUKU_PAGE) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

@Composable
fun DeveloperProfileCard(
    uid: String,
    onCopyUid: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, PaseoBorderDark, RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = PaseoSurfaceCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar with crimson glowing ring
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(CrimsonPrimary.copy(alpha = 0.5f), Color(0xFF1E0509))
                            )
                        )
                        .border(1.5.dp, CrimsonGlow.copy(alpha = 0.8f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Avatar",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ZUCCHERO XANN",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified",
                            tint = VerifiedBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = "Developer / Owner",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF0F0F14))
                            .clickable { onCopyUid() }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "UID: $uid",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy UID",
                            tint = TextMuted,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SystemStatusCard(
    deviceInfo: DeviceInfo?,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, PaseoBorderDark, RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = PaseoSurfaceCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "System Status",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Telemetry",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4 Circular Telemetry Gauges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // CPU Gauge
                val cpuCores = deviceInfo?.cpuCores ?: Runtime.getRuntime().availableProcessors()
                val cpuPercent = (35 + (cpuCores * 3)).coerceIn(20, 85)
                StatusGauge(
                    title = "CPU",
                    value = "$cpuPercent%",
                    progress = cpuPercent / 100f,
                    accentColor = Color(0xFF00E676)
                )

                // RAM Gauge
                val ramUsed = deviceInfo?.let { (it.totalRamBytes - it.availableRamBytes) / (1024 * 1024) } ?: 0L
                val ramTotal = deviceInfo?.let { it.totalRamBytes / (1024 * 1024) } ?: 1L
                val ramPercent = if (ramTotal > 0) ((ramUsed * 100) / ramTotal).toInt().coerceIn(10, 95) else 50
                StatusGauge(
                    title = "RAM",
                    value = "$ramPercent%",
                    progress = ramPercent / 100f,
                    accentColor = CrimsonGlow
                )

                // Battery Temperature / Suhu
                val tempC = deviceInfo?.batteryTemperatureCelsius ?: 34.0f
                val tempProgress = (tempC / 50f).coerceIn(0.2f, 1f)
                StatusGauge(
                    title = "SUHU",
                    value = "${tempC.toInt()}°C",
                    progress = tempProgress,
                    accentColor = Color(0xFFFF9100)
                )

                // Refresh Rate / Display
                val fps = deviceInfo?.refreshRateHz?.toInt() ?: 60
                val fpsProgress = (fps / 144f).coerceIn(0.4f, 1f)
                StatusGauge(
                    title = "REFRESH",
                    value = "${fps}Hz",
                    progress = fpsProgress,
                    accentColor = Color(0xFF00E5FF)
                )
            }
        }
    }
}

@Composable
fun StatusGauge(
    title: String,
    value: String,
    progress: Float,
    accentColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(54.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFF22222D),
                strokeWidth = 4.dp,
            )
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxSize(),
                color = accentColor,
                strokeWidth = 4.dp,
            )
            Text(
                text = value,
                color = TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = title,
            color = TextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun MenuActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, PaseoBorderDark, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag(testTag),
        colors = CardDefaults.cardColors(containerColor = PaseoSurfaceCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CrimsonContainer)
                        .border(1.dp, PaseoBorderRed, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = title,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
fun ShizukuBannerCard(
    shizukuInfo: ShizukuInfo,
    onClick: () -> Unit
) {
    val isConnected = shizukuInfo.status == ShizukuStatus.CONNECTED && shizukuInfo.isPermissionGranted
    val bannerBorder = if (isConnected) EmeraldAccent.copy(alpha = 0.5f) else PaseoBorderRed

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, bannerBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF14070A))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isConnected) EmeraldAccent.copy(alpha = 0.2f) else CrimsonDark),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Shizuku",
                        tint = if (isConnected) EmeraldAccent else CrimsonGlow,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Shizuku Wireless ADB",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isConnected) "Terhubung & Aktif (Tanpa Root)" else "Belum Terhubung • Ketuk untuk Panduan",
                        color = if (isConnected) EmeraldAccent else TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(13.dp)
            )
        }
    }
}
