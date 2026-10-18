package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.GameLabsCategory
import com.example.ui.MainTab
import com.example.ui.MainViewModel
import com.example.ui.components.PaseoBottomBar
import com.example.ui.components.PaseoHeader
import com.example.ui.screens.AppsScreen
import com.example.ui.screens.GameLabsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.CrimsonContainer
import com.example.ui.theme.CrimsonGlow
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PaseoDarkBackground

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                PaseoSensiApp(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkShizuku()
        viewModel.loadDeviceInfo()
    }
}

@Composable
fun PaseoSensiApp(viewModel: MainViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val shizukuInfo by viewModel.shizukuInfo.collectAsStateWithLifecycle()
    val deviceInfo by viewModel.deviceInfo.collectAsStateWithLifecycle()
    val filteredApps by viewModel.filteredApps.collectAsStateWithLifecycle()
    val appSearchQuery by viewModel.appSearchQuery.collectAsStateWithLifecycle()
    val appFilterOnlyGames by viewModel.appFilterOnlyGames.collectAsStateWithLifecycle()
    val isAppsLoading by viewModel.isAppsLoading.collectAsStateWithLifecycle()
    val crosshairConfig by viewModel.crosshairConfig.collectAsStateWithLifecycle()
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val gameLabsCategory by viewModel.gameLabsCategory.collectAsStateWithLifecycle()
    val pointerLocationActive by viewModel.pointerLocationActive.collectAsStateWithLifecycle()
    val lastTelemetryTime by viewModel.lastTelemetryTimestamp.collectAsStateWithLifecycle()
    val uiMessage by viewModel.uiMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiMessage) {
        uiMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUiMessage()
        }
    }

    // Handle system back navigation to return to Home tab first
    BackHandler(enabled = currentTab != MainTab.HOME) {
        viewModel.selectTab(MainTab.HOME)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = PaseoDarkBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            PaseoHeader(
                shizukuInfo = shizukuInfo,
                onStatusClick = { viewModel.selectTab(MainTab.HOME) }
            )
        },
        bottomBar = {
            PaseoBottomBar(
                selectedTab = currentTab,
                onTabSelected = { viewModel.selectTab(it) }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = CrimsonContainer,
                    contentColor = Color.White,
                    actionColor = CrimsonGlow
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(PaseoDarkBackground)
        ) {
            when (currentTab) {
                MainTab.HOME -> {
                    HomeScreen(
                        viewModel = viewModel,
                        shizukuInfo = shizukuInfo,
                        deviceInfo = deviceInfo
                    )
                }
                MainTab.APPS -> {
                    AppsScreen(
                        apps = filteredApps,
                        searchQuery = appSearchQuery,
                        onSearchQueryChange = { viewModel.setAppSearchQuery(it) },
                        onlyGames = appFilterOnlyGames,
                        onOnlyGamesChange = { viewModel.setAppFilterOnlyGames(it) },
                        isLoading = isAppsLoading,
                        onRefresh = { viewModel.loadApps() },
                        onLaunchApp = { viewModel.launchApp(it) }
                    )
                }
                MainTab.GAME_LABS -> {
                    GameLabsScreen(
                        viewModel = viewModel,
                        shizukuInfo = shizukuInfo,
                        deviceInfo = deviceInfo,
                        crosshairConfig = crosshairConfig,
                        activeProfile = activeProfile,
                        pointerLocationActive = pointerLocationActive,
                        selectedCategory = gameLabsCategory,
                        onCategorySelected = { viewModel.selectGameLabsCategory(it) },
                        lastTelemetryTime = lastTelemetryTime
                    )
                }
            }
        }
    }
}
