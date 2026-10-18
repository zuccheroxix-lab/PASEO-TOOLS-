package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CrosshairConfig
import com.example.model.DeviceInfo
import com.example.model.SensitivityProfile
import com.example.model.ShizukuInfo
import com.example.ui.GameLabsCategory
import com.example.ui.MainViewModel
import com.example.ui.screens.gamelabs.ConfigCategoryView
import com.example.ui.screens.gamelabs.VisualCategoryView
import com.example.ui.theme.CrimsonGlow
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.PaseoBorderGreen
import com.example.ui.theme.PaseoDarkBackground
import com.example.ui.theme.PaseoSurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun GameLabsScreen(
    viewModel: MainViewModel,
    shizukuInfo: ShizukuInfo,
    deviceInfo: DeviceInfo?,
    crosshairConfig: CrosshairConfig,
    activeProfile: SensitivityProfile,
    pointerLocationActive: Boolean,
    selectedCategory: GameLabsCategory,
    onCategorySelected: (GameLabsCategory) -> Unit,
    lastTelemetryTime: Long
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PaseoDarkBackground)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Category Filter Tab Selector: Semua / Visual / Konfigurasi
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(PaseoSurfaceDark)
                .border(1.dp, PaseoBorderGreen.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            CategoryTabItem(
                title = "Semua",
                isSelected = selectedCategory == GameLabsCategory.ALL,
                onClick = { onCategorySelected(GameLabsCategory.ALL) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("tab_labs_all")
            )
            CategoryTabItem(
                title = "Visual",
                isSelected = selectedCategory == GameLabsCategory.VISUAL,
                onClick = { onCategorySelected(GameLabsCategory.VISUAL) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("tab_labs_visual")
            )
            CategoryTabItem(
                title = "Konfigurasi",
                isSelected = selectedCategory == GameLabsCategory.CONFIG,
                onClick = { onCategorySelected(GameLabsCategory.CONFIG) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("tab_labs_config")
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Visual Category (Resolution, DPI, ROG Monitor, Aim Assistant Crosshair)
            if (selectedCategory == GameLabsCategory.ALL || selectedCategory == GameLabsCategory.VISUAL) {
                item {
                    VisualCategoryView(
                        viewModel = viewModel,
                        shizukuInfo = shizukuInfo,
                        deviceInfo = deviceInfo,
                        crosshairConfig = crosshairConfig,
                        lastTelemetryTime = lastTelemetryTime
                    )
                }
            }

            // Configuration Category (Sensi Pack, Touch Up, Aim Trick 15%)
            if (selectedCategory == GameLabsCategory.ALL || selectedCategory == GameLabsCategory.CONFIG) {
                item {
                    ConfigCategoryView(
                        viewModel = viewModel,
                        shizukuInfo = shizukuInfo,
                        activeProfile = activeProfile,
                        pointerLocationActive = pointerLocationActive
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(30.dp)) }
        }
    }
}

@Composable
private fun CategoryTabItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) CrimsonPrimary else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (isSelected) Color.White else TextMuted,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
