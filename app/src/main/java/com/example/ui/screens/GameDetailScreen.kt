package com.example.ui.screens

import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.InstalledApp
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
fun GameDetailScreen(
    game: InstalledApp,
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Optimasi, 1 = Pengaturan

    val optPerf by viewModel.optPerformance.collectAsStateWithLifecycle()
    val optTouch by viewModel.optTouch.collectAsStateWithLifecycle()
    val optSmartDpi by viewModel.optSmartDpi.collectAsStateWithLifecycle()
    val optLatency by viewModel.optLatency.collectAsStateWithLifecycle()
    val optFastLaunch by viewModel.optFastLaunch.collectAsStateWithLifecycle()
    val optRog by viewModel.optRogMonitoring.collectAsStateWithLifecycle()

    val existingGameProfile = remember(game.packageName) {
        viewModel.sensitivityRepository.getProfileForGame(game.packageName) ?: viewModel.activeProfile.value
    }

    var generalSensi by remember(existingGameProfile) { mutableFloatStateOf(existingGameProfile.generalSensi.toFloat()) }
    var redDotSensi by remember(existingGameProfile) { mutableFloatStateOf(existingGameProfile.redDotSensi.toFloat()) }
    var scope2xSensi by remember(existingGameProfile) { mutableFloatStateOf(existingGameProfile.scope2xSensi.toFloat()) }
    var scope4xSensi by remember(existingGameProfile) { mutableFloatStateOf(existingGameProfile.scope4xSensi.toFloat()) }
    var sniperSensi by remember(existingGameProfile) { mutableFloatStateOf(existingGameProfile.sniperSensi.toFloat()) }

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
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                val bmp = (game.icon as? BitmapDrawable)?.bitmap
                if (bmp != null) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = game.appName,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF22080D)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SportsEsports,
                            contentDescription = null,
                            tint = CrimsonGlow,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = game.appName,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${game.versionName} • ${if (game.isGame) "Gaming Target" else "Aplikasi"}",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            // Tab Row: Optimasi / Pengaturan
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = PaseoSurfaceDark,
                contentColor = TextPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = CrimsonPrimary
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "Optimasi",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == 0) CrimsonGlow else TextSecondary
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "Pengaturan",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == 1) CrimsonGlow else TextSecondary
                        )
                    }
                )
            }

            // Tab Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (selectedTab == 0) {
                    // TAB OPTIMASI
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            OptimizationToggleCard(
                                title = "Mode Performance",
                                subtitle = "FPS Stabil & Smooth Frame Pacing",
                                icon = Icons.Default.Speed,
                                isChecked = optPerf,
                                onCheckedChange = { viewModel.setOptPerformance(it) }
                            )
                        }

                        item {
                            OptimizationToggleCard(
                                title = "Touch Optimize",
                                subtitle = "Responsif & Akurasi Sentuh Layar",
                                icon = Icons.Default.Sensors,
                                isChecked = optTouch,
                                onCheckedChange = { viewModel.setOptTouch(it) }
                            )
                        }

                        item {
                            OptimizationToggleCard(
                                title = "Smart DPI Realtime",
                                subtitle = "Auto Rekomendasi DPI Sesuai Game",
                                icon = Icons.Default.ElectricBolt,
                                isChecked = optSmartDpi,
                                onCheckedChange = { viewModel.setOptSmartDpi(it) }
                            )
                        }

                        item {
                            OptimizationToggleCard(
                                title = "Latency Tracker",
                                subtitle = "Monitor Stabilitas Jaringan & Ping",
                                icon = Icons.Default.NetworkCheck,
                                isChecked = optLatency,
                                onCheckedChange = { viewModel.setOptLatency(it) }
                            )
                        }

                        item {
                            OptimizationToggleCard(
                                title = "FastLaunch",
                                subtitle = "Prioritaskan Thread CPU Saat Game Dimulai",
                                icon = Icons.Default.RocketLaunch,
                                isChecked = optFastLaunch,
                                onCheckedChange = { viewModel.setOptFastLaunch(it) }
                            )
                        }

                        item {
                            OptimizationToggleCard(
                                title = "Rog Monitoring",
                                subtitle = "Telemetri CPU • GPU • Suhu Realtime",
                                icon = Icons.Default.Memory,
                                isChecked = optRog,
                                onCheckedChange = { viewModel.setOptRogMonitoring(it) }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(70.dp))
                        }
                    }
                } else {
                    // TAB PENGATURAN (Sensitivity per game)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Text(
                                text = "KALIBRASI SENSI: ${game.appName.uppercase()}",
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        item {
                            SensitivitySliderCard(
                                label = "Sensitivitas Umum",
                                value = generalSensi,
                                onValueChange = { generalSensi = it }
                            )
                        }

                        item {
                            SensitivitySliderCard(
                                label = "Red Dot Scope",
                                value = redDotSensi,
                                onValueChange = { redDotSensi = it }
                            )
                        }

                        item {
                            SensitivitySliderCard(
                                label = "2x Scope",
                                value = scope2xSensi,
                                onValueChange = { scope2xSensi = it }
                            )
                        }

                        item {
                            SensitivitySliderCard(
                                label = "4x Scope",
                                value = scope4xSensi,
                                onValueChange = { scope4xSensi = it }
                            )
                        }

                        item {
                            SensitivitySliderCard(
                                label = "Sniper Scope",
                                value = sniperSensi,
                                onValueChange = { sniperSensi = it }
                            )
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        generalSensi = existingGameProfile.generalSensi.toFloat()
                                        redDotSensi = existingGameProfile.redDotSensi.toFloat()
                                        scope2xSensi = existingGameProfile.scope2xSensi.toFloat()
                                        scope4xSensi = existingGameProfile.scope4xSensi.toFloat()
                                        sniperSensi = existingGameProfile.sniperSensi.toFloat()
                                        viewModel.showMessage("Pengaturan sensitivitas direset.")
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22222D))
                                ) {
                                    Text("Reset", color = TextSecondary)
                                }

                                Button(
                                    onClick = {
                                        val updatedProfile = existingGameProfile.copy(
                                            generalSensi = generalSensi.toInt(),
                                            redDotSensi = redDotSensi.toInt(),
                                            scope2xSensi = scope2xSensi.toInt(),
                                            scope4xSensi = scope4xSensi.toInt(),
                                            sniperSensi = sniperSensi.toInt()
                                        )
                                        viewModel.saveGameProfile(game.packageName, game.appName, updatedProfile)
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
                                ) {
                                    Text("Simpan Sensi", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(70.dp))
                        }
                    }
                }

                // Fixed Bottom Action: "+ Jalankan Optimasi"
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = {
                            val profileToApply = existingGameProfile.copy(
                                generalSensi = generalSensi.toInt(),
                                redDotSensi = redDotSensi.toInt(),
                                scope2xSensi = scope2xSensi.toInt(),
                                scope4xSensi = scope4xSensi.toInt(),
                                sniperSensi = sniperSensi.toInt()
                            )
                            viewModel.saveGameProfile(game.packageName, game.appName, profileToApply)
                            viewModel.showMessage("Optimasi ${game.appName} diterapkan. Membuka game...")
                            viewModel.launchApp(game.packageName)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .testTag("btn_jalankan_optimasi"),
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RocketLaunch,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Jalankan Optimasi & Luncurkan",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OptimizationToggleCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, PaseoBorderDark, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = PaseoSurfaceCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF22090E)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = CrimsonGlow,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        color = TextPrimary,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = CrimsonPrimary,
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = Color(0xFF20202A)
                )
            )
        }
    }
}

@Composable
fun SensitivitySliderCard(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, PaseoBorderDark, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = PaseoSurfaceCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${value.toInt()}",
                    color = CrimsonGlow,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = 0f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = CrimsonGlow,
                    activeTrackColor = CrimsonPrimary,
                    inactiveTrackColor = Color(0xFF262634)
                )
            )
        }
    }
}
