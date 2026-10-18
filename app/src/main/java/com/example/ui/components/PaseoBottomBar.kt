package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainTab
import com.example.ui.theme.CrimsonGlow
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.PaseoBorderGreen
import com.example.ui.theme.PaseoDarkBackground
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun PaseoBottomBar(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .border(0.5.dp, PaseoBorderGreen.copy(alpha = 0.5f)),
        containerColor = Color(0xFF090E0C),
        tonalElevation = 8.dp
    ) {
        // Tab 1: Home
        val isHomeSelected = selectedTab == MainTab.HOME
        NavigationBarItem(
            selected = isHomeSelected,
            onClick = { onTabSelected(MainTab.HOME) },
            icon = {
                Icon(
                    imageVector = if (isHomeSelected) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Home Tab"
                )
            },
            label = {
                Text(
                    text = "Home",
                    fontWeight = if (isHomeSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 12.sp
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = CrimsonGlow,
                indicatorColor = CrimsonPrimary,
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextMuted
            ),
            modifier = Modifier.testTag("nav_tab_home")
        )

        // Tab 2: Apps (Game Launcher)
        val isAppsSelected = selectedTab == MainTab.APPS
        NavigationBarItem(
            selected = isAppsSelected,
            onClick = { onTabSelected(MainTab.APPS) },
            icon = {
                Icon(
                    imageVector = if (isAppsSelected) Icons.Filled.SportsEsports else Icons.Outlined.SportsEsports,
                    contentDescription = "Apps Launcher Tab"
                )
            },
            label = {
                Text(
                    text = "Apps",
                    fontWeight = if (isAppsSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 12.sp
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = CrimsonGlow,
                indicatorColor = CrimsonPrimary,
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextMuted
            ),
            modifier = Modifier.testTag("nav_tab_apps")
        )

        // Tab 3: Game-Labs
        val isLabsSelected = selectedTab == MainTab.GAME_LABS
        NavigationBarItem(
            selected = isLabsSelected,
            onClick = { onTabSelected(MainTab.GAME_LABS) },
            icon = {
                Icon(
                    imageVector = if (isLabsSelected) Icons.Filled.Science else Icons.Outlined.Science,
                    contentDescription = "Game Labs Tab"
                )
            },
            label = {
                Text(
                    text = "Game-Labs",
                    fontWeight = if (isLabsSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 12.sp
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = CrimsonGlow,
                indicatorColor = CrimsonPrimary,
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextMuted
            ),
            modifier = Modifier.testTag("nav_tab_game_labs")
        )
    }
}
