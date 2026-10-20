package com.example.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
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
import com.example.ui.theme.PaseoBorderDark
import com.example.ui.theme.PaseoSurfaceDark
import com.example.ui.theme.TextMuted

@Composable
fun PaseoBottomBar(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .border(0.5.dp, PaseoBorderDark),
        containerColor = PaseoSurfaceDark,
        tonalElevation = 8.dp
    ) {
        // Tab 1: Beranda
        val isBeranda = selectedTab == MainTab.BERANDA
        NavigationBarItem(
            selected = isBeranda,
            onClick = { onTabSelected(MainTab.BERANDA) },
            icon = {
                Icon(
                    imageVector = if (isBeranda) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Beranda"
                )
            },
            label = {
                Text(
                    text = "Beranda",
                    fontWeight = if (isBeranda) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 11.sp
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = CrimsonGlow,
                indicatorColor = CrimsonPrimary,
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextMuted
            ),
            modifier = Modifier.testTag("nav_tab_beranda")
        )

        // Tab 2: Tools
        val isTools = selectedTab == MainTab.TOOLS
        NavigationBarItem(
            selected = isTools,
            onClick = { onTabSelected(MainTab.TOOLS) },
            icon = {
                Icon(
                    imageVector = if (isTools) Icons.Filled.Build else Icons.Outlined.Build,
                    contentDescription = "Tools"
                )
            },
            label = {
                Text(
                    text = "Tools",
                    fontWeight = if (isTools) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 11.sp
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = CrimsonGlow,
                indicatorColor = CrimsonPrimary,
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextMuted
            ),
            modifier = Modifier.testTag("nav_tab_tools")
        )

        // Tab 3: Game (Game Launcher)
        val isGame = selectedTab == MainTab.GAME
        NavigationBarItem(
            selected = isGame,
            onClick = { onTabSelected(MainTab.GAME) },
            icon = {
                Icon(
                    imageVector = if (isGame) Icons.Filled.SportsEsports else Icons.Outlined.SportsEsports,
                    contentDescription = "Game"
                )
            },
            label = {
                Text(
                    text = "Game",
                    fontWeight = if (isGame) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 11.sp
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = CrimsonGlow,
                indicatorColor = CrimsonPrimary,
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextMuted
            ),
            modifier = Modifier.testTag("nav_tab_game")
        )

        // Tab 4: Profil (Developer & Partner)
        val isProfil = selectedTab == MainTab.PROFIL
        NavigationBarItem(
            selected = isProfil,
            onClick = { onTabSelected(MainTab.PROFIL) },
            icon = {
                Icon(
                    imageVector = if (isProfil) Icons.Filled.Person else Icons.Outlined.Person,
                    contentDescription = "Profil"
                )
            },
            label = {
                Text(
                    text = "Profil",
                    fontWeight = if (isProfil) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 11.sp
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = CrimsonGlow,
                indicatorColor = CrimsonPrimary,
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextMuted
            ),
            modifier = Modifier.testTag("nav_tab_profil")
        )
    }
}
