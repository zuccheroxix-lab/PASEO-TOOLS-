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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.GameLabsCategory
import com.example.ui.MainTab
import com.example.ui.MainViewModel
import com.example.ui.SubScreen
import com.example.ui.components.PaseoBottomBar
import com.example.ui.components.PaseoHeader
import com.example.ui.screens.CrosshairStudioScreen
import com.example.ui.screens.GameDetailScreen
import com.example.ui.screens.GameLabsScreen
import com.example.ui.screens.GameLauncherScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ShizukuScreen
import com.example.ui.screens.SplashScreen
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
    var showSplash by remember { mutableStateOf(true) }

    if (showSplash) {
        SplashScreen(onDismiss = { showSplash = false })
        return
    }

    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val currentSubScreen by viewModel.currentSubScreen.collectAsStateWithLifecycle()
    val selectedGame by viewModel.selectedGame.collectAsStateWithLifecycle()
    val shizukuInfo by viewModel.shizukuInfo.collectAsStateWithLifecycle()
    val deviceInfo by viewModel.deviceInfo.collectAsStateWithLifecycle()
    val filteredApps by viewModel.filteredApps.collectAsStateWithLifecycle()
    val appSearchQuery by viewModel.appSearchQuery.collectAsStateWithLifecycle()
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

    // Handle system back navigation: pop subscreen first, or return to Beranda tab
    BackHandler(enabled = currentSubScreen != SubScreen.NONE || currentTab != MainTab.BERANDA) {
        if (currentSubScreen != SubScreen.NONE) {
            viewModel.closeSubScreen()
        } else {
            viewModel.selectTab(MainTab.BERANDA)
        }
    }

    if (currentSubScreen != SubScreen.NONE) {
        when (currentSubScreen) {
            SubScreen.GAME_DETAIL -> {
                val game = selectedGame
                if (game != null) {
                    GameDetailScreen(
                        game = game,
                        viewModel = viewModel,
                        onBack = { viewModel.closeSubScreen() }
                    )
                } else {
                    viewModel.closeSubScreen()
                }
            }
            SubScreen.CROSSHAIR_GALLERY, SubScreen.CROSSHAIR_EDITOR -> {
                CrosshairStudioScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.closeSubScreen() }
                )
            }
            SubScreen.SHIZUKU_PAGE -> {
                ShizukuScreen(
                    viewModel = viewModel,
                    shizukuInfo = shizukuInfo,
                    onBack = { viewModel.closeSubScreen() }
                )
            }
            SubScreen.SETTINGS_PAGE -> {
                SettingsScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.closeSubScreen() }
                )
            }
            SubScreen.NONE -> {}
        }
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = PaseoDarkBackground,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                PaseoHeader(
                    shizukuInfo = shizukuInfo,
                    onStatusClick = { viewModel.navigateToSubScreen(SubScreen.SHIZUKU_PAGE) }
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
                    MainTab.BERANDA -> {
                        HomeScreen(
                            viewModel = viewModel,
                            shizukuInfo = shizukuInfo,
                            deviceInfo = deviceInfo
                        )
                    }
                    MainTab.TOOLS -> {
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
                    MainTab.GAME -> {
                        GameLauncherScreen(
                            viewModel = viewModel,
                            apps = filteredApps,
                            searchQuery = appSearchQuery,
                            onSearchQueryChange = { viewModel.setAppSearchQuery(it) },
                            isLoading = isAppsLoading,
                            onGameClick = { viewModel.openGameDetail(it) }
                        )
                    }
                    MainTab.PROFIL -> {
                        ProfileScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
