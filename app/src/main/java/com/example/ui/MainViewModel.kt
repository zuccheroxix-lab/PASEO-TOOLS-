package com.example.ui

import android.app.Application
import android.content.Context
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.CrosshairConfig
import com.example.model.DeviceInfo
import com.example.model.InstalledApp
import com.example.model.SensitivityProfile
import com.example.model.ShizukuInfo
import com.example.repository.AppLauncherRepository
import com.example.repository.PreferencesRepository
import com.example.repository.SystemMonitorRepository
import com.example.service.CrosshairService
import com.example.shizuku.ShizukuManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class MainTab {
    BERANDA,
    TOOLS,
    GAME,
    PROFIL;

    companion object {
        val HOME = BERANDA
        val APPS = GAME
        val GAME_LABS = TOOLS
    }
}

enum class SubScreen {
    NONE,
    GAME_DETAIL,
    CROSSHAIR_GALLERY,
    CROSSHAIR_EDITOR,
    SHIZUKU_PAGE,
    SETTINGS_PAGE,
    SENSITIVITY_STUDIO
}

enum class GameLabsCategory {
    ALL,
    VISUAL,
    CONFIG
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context get() = getApplication<Application>().applicationContext

    val shizukuManager = ShizukuManager(context)
    val preferencesRepository = PreferencesRepository(context)
    val authRepository = com.example.auth.AuthRepository(context)
    val sensitivityRepository = com.example.repository.SensitivityRepository(context)
    val appLauncherRepository = AppLauncherRepository(context)
    val systemMonitorRepository = SystemMonitorRepository(context)

    // Security & App Lock State
    private val _isAppLocked = MutableStateFlow(authRepository.hasAnyAccount() && authRepository.isAppLocked())
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    fun unlockApp() {
        authRepository.setAppLocked(false)
        _isAppLocked.value = false
    }

    fun lockApp() {
        authRepository.setAppLocked(true)
        _isAppLocked.value = true
    }

    fun logout() {
        lockApp()
        _currentSubScreen.value = SubScreen.NONE
        _currentTab.value = MainTab.BERANDA
    }

    // Navigation & Screen State
    private val _currentTab = MutableStateFlow(MainTab.BERANDA)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    private val _currentSubScreen = MutableStateFlow(SubScreen.NONE)
    val currentSubScreen: StateFlow<SubScreen> = _currentSubScreen.asStateFlow()

    private val _selectedGame = MutableStateFlow<InstalledApp?>(null)
    val selectedGame: StateFlow<InstalledApp?> = _selectedGame.asStateFlow()

    private val _showSplash = MutableStateFlow(true)
    val showSplash: StateFlow<Boolean> = _showSplash.asStateFlow()

    // Game Optimizer Toggles State
    private val _optPerformance = MutableStateFlow(preferencesRepository.isOptPerformanceEnabled())
    val optPerformance: StateFlow<Boolean> = _optPerformance.asStateFlow()

    private val _optTouch = MutableStateFlow(preferencesRepository.isOptTouchEnabled())
    val optTouch: StateFlow<Boolean> = _optTouch.asStateFlow()

    private val _optSmartDpi = MutableStateFlow(preferencesRepository.isOptSmartDpiEnabled())
    val optSmartDpi: StateFlow<Boolean> = _optSmartDpi.asStateFlow()

    private val _optLatency = MutableStateFlow(preferencesRepository.isOptLatencyEnabled())
    val optLatency: StateFlow<Boolean> = _optLatency.asStateFlow()

    private val _optFastLaunch = MutableStateFlow(preferencesRepository.isOptFastLaunchEnabled())
    val optFastLaunch: StateFlow<Boolean> = _optFastLaunch.asStateFlow()

    private val _optRogMonitoring = MutableStateFlow(preferencesRepository.isOptRogMonitoringEnabled())
    val optRogMonitoring: StateFlow<Boolean> = _optRogMonitoring.asStateFlow()

    // Cache Size State
    private val _cacheSizeFormatted = MutableStateFlow("0.0 KB")
    val cacheSizeFormatted: StateFlow<String> = _cacheSizeFormatted.asStateFlow()

    private val _gameLabsCategory = MutableStateFlow(GameLabsCategory.ALL)
    val gameLabsCategory: StateFlow<GameLabsCategory> = _gameLabsCategory.asStateFlow()

    // Shizuku State
    val shizukuInfo: StateFlow<ShizukuInfo> = shizukuManager.shizukuInfo

    // Hardware & Telemetry State
    private val _deviceInfo = MutableStateFlow<DeviceInfo?>(null)
    val deviceInfo: StateFlow<DeviceInfo?> = _deviceInfo.asStateFlow()

    private val _lastTelemetryTimestamp = MutableStateFlow(System.currentTimeMillis())
    val lastTelemetryTimestamp: StateFlow<Long> = _lastTelemetryTimestamp.asStateFlow()

    // Apps & Launcher State
    private val _installedApps = MutableStateFlow<List<InstalledApp>>(emptyList())
    val installedApps: StateFlow<List<InstalledApp>> = _installedApps.asStateFlow()

    private val _appSearchQuery = MutableStateFlow("")
    val appSearchQuery: StateFlow<String> = _appSearchQuery.asStateFlow()

    private val _appFilterOnlyGames = MutableStateFlow(false)
    val appFilterOnlyGames: StateFlow<Boolean> = _appFilterOnlyGames.asStateFlow()

    private val _isAppsLoading = MutableStateFlow(false)
    val isAppsLoading: StateFlow<Boolean> = _isAppsLoading.asStateFlow()

    // Filtered apps combining search and game filter
    val filteredApps: StateFlow<List<InstalledApp>> = combine(
        _installedApps,
        _appSearchQuery,
        _appFilterOnlyGames
    ) { apps, query, onlyGames ->
        apps.filter { app ->
            val matchesQuery = query.isBlank() ||
                    app.appName.contains(query, ignoreCase = true) ||
                    app.packageName.contains(query, ignoreCase = true)
            val matchesGame = !onlyGames || app.isGame
            matchesQuery && matchesGame
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Crosshair State
    val crosshairConfig: StateFlow<CrosshairConfig> = preferencesRepository.crosshairConfig

    // Sensitivity State
    val activeProfile: StateFlow<SensitivityProfile> = sensitivityRepository.activeProfile

    // UI Feedback Message
    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    private val _isCommandExecuting = MutableStateFlow(false)
    val isCommandExecuting: StateFlow<Boolean> = _isCommandExecuting.asStateFlow()

    // Pointer Location State
    private val _pointerLocationActive = MutableStateFlow(false)
    val pointerLocationActive: StateFlow<Boolean> = _pointerLocationActive.asStateFlow()

    private var telemetryJob: Job? = null

    init {
        checkShizuku()
        loadDeviceInfo()
        loadApps()
        startTelemetryLoop()
        checkPointerLocationState()
        refreshCacheSize()
    }

    fun dismissSplash() {
        _showSplash.value = false
    }

    fun selectTab(tab: MainTab) {
        _currentTab.value = tab
        _currentSubScreen.value = SubScreen.NONE
    }

    fun navigateToSubScreen(screen: SubScreen) {
        _currentSubScreen.value = screen
    }

    fun closeSubScreen() {
        _currentSubScreen.value = SubScreen.NONE
        _selectedGame.value = null
    }

    fun openGameDetail(game: InstalledApp) {
        _selectedGame.value = game
        _currentSubScreen.value = SubScreen.GAME_DETAIL
    }

    fun setOptPerformance(enabled: Boolean) {
        _optPerformance.value = enabled
        preferencesRepository.setOptPerformanceEnabled(enabled)
    }

    fun setOptTouch(enabled: Boolean) {
        _optTouch.value = enabled
        preferencesRepository.setOptTouchEnabled(enabled)
    }

    fun setOptSmartDpi(enabled: Boolean) {
        _optSmartDpi.value = enabled
        preferencesRepository.setOptSmartDpiEnabled(enabled)
    }

    fun setOptLatency(enabled: Boolean) {
        _optLatency.value = enabled
        preferencesRepository.setOptLatencyEnabled(enabled)
    }

    fun setOptFastLaunch(enabled: Boolean) {
        _optFastLaunch.value = enabled
        preferencesRepository.setOptFastLaunchEnabled(enabled)
    }

    fun setOptRogMonitoring(enabled: Boolean) {
        _optRogMonitoring.value = enabled
        preferencesRepository.setOptRogMonitoringEnabled(enabled)
    }

    fun refreshCacheSize() {
        viewModelScope.launch {
            val bytes = preferencesRepository.getAppCacheSize(context)
            _cacheSizeFormatted.value = formatBytes(bytes)
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            val success = preferencesRepository.clearAppCache(context)
            refreshCacheSize()
            if (success) {
                showMessage("Cache aplikasi berhasil dibersihkan.")
            } else {
                showMessage("Cache sudah bersih.")
            }
        }
    }

    private fun formatBytes(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 -> String.format(java.util.Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0))
            bytes >= 1024 -> String.format(java.util.Locale.US, "%.1f KB", bytes / 1024.0)
            else -> "$bytes B"
        }
    }

    fun selectGameLabsCategory(category: GameLabsCategory) {
        _gameLabsCategory.value = category
    }

    fun setAppSearchQuery(query: String) {
        _appSearchQuery.value = query
    }

    fun setAppFilterOnlyGames(onlyGames: Boolean) {
        _appFilterOnlyGames.value = onlyGames
    }

    fun clearUiMessage() {
        _uiMessage.value = null
    }

    fun showMessage(message: String) {
        _uiMessage.value = message
    }

    fun checkShizuku() {
        shizukuManager.checkStatus()
    }

    fun requestShizukuPermission() {
        shizukuManager.requestPermission()
    }

    fun loadDeviceInfo() {
        viewModelScope.launch {
            try {
                val info = systemMonitorRepository.getDeviceInfo()
                _deviceInfo.value = info
                _lastTelemetryTimestamp.value = System.currentTimeMillis()

                // Save baseline resolution and DPI if not yet set
                if (preferencesRepository.originalResolution == null) {
                    preferencesRepository.originalResolution = "${info.screenWidthPx}x${info.screenHeightPx}"
                }
                if (preferencesRepository.originalDpi <= 0) {
                    preferencesRepository.originalDpi = info.screenDensityDpi
                }
            } catch (e: Exception) {
                // Ignore telemetry error
            }
        }
    }

    private fun startTelemetryLoop() {
        telemetryJob?.cancel()
        telemetryJob = viewModelScope.launch {
            while (isActive) {
                delay(3000)
                try {
                    _deviceInfo.value = systemMonitorRepository.getDeviceInfo()
                    _lastTelemetryTimestamp.value = System.currentTimeMillis()
                } catch (e: Exception) {
                    // Safe loop
                }
            }
        }
    }

    fun loadApps() {
        viewModelScope.launch {
            _isAppsLoading.value = true
            try {
                val apps = appLauncherRepository.getInstalledApps()
                _installedApps.value = apps
            } catch (e: Exception) {
                showMessage("Gagal memuat daftar aplikasi: ${e.localizedMessage}")
            } finally {
                _isAppsLoading.value = false
            }
        }
    }

    fun launchApp(packageName: String) {
        val result = appLauncherRepository.launchApp(packageName)
        result.onFailure {
            showMessage("Gagal membuka aplikasi: ${it.localizedMessage}")
        }
    }

    // Resolution Manager via Shizuku wm size
    fun applyResolution(width: Int, height: Int) {
        if (width < 320 || height < 480) {
            showMessage("Resolusi $width x $height terlalu kecil dan dapat merusak tampilan.")
            return
        }

        viewModelScope.launch {
            _isCommandExecuting.value = true
            val cmd = "wm size ${width}x${height}"
            val res = shizukuManager.executeSafeCommand(cmd)
            res.onSuccess {
                showMessage("Resolusi berhasil diubah menjadi ${width}x${height}.")
                loadDeviceInfo()
            }.onFailure { e ->
                showMessage("Gagal mengubah resolusi: ${e.localizedMessage}. Pastikan Shizuku aktif & diizinkan.")
            }
            _isCommandExecuting.value = false
        }
    }

    fun resetResolution() {
        viewModelScope.launch {
            _isCommandExecuting.value = true
            val cmd = "wm size reset"
            val res = shizukuManager.executeSafeCommand(cmd)
            res.onSuccess {
                showMessage("Resolusi layar berhasil dipulihkan ke default bawaan.")
                loadDeviceInfo()
            }.onFailure { e ->
                showMessage("Gagal memulihkan resolusi: ${e.localizedMessage}")
            }
            _isCommandExecuting.value = false
        }
    }

    // DPI Manager via Shizuku wm density
    fun applyDpi(dpi: Int) {
        if (dpi < 160 || dpi > 960) {
            showMessage("Nilai DPI ($dpi) di luar batas aman (160 - 960).")
            return
        }

        viewModelScope.launch {
            _isCommandExecuting.value = true
            val cmd = "wm density $dpi"
            val res = shizukuManager.executeSafeCommand(cmd)
            res.onSuccess {
                showMessage("DPI layar berhasil diterapkan ke $dpi DPI.")
                loadDeviceInfo()
            }.onFailure { e ->
                showMessage("Gagal menerapkan DPI: ${e.localizedMessage}. Pastikan Shizuku terhubung.")
            }
            _isCommandExecuting.value = false
        }
    }

    fun resetDpi() {
        viewModelScope.launch {
            _isCommandExecuting.value = true
            val cmd = "wm density reset"
            val res = shizukuManager.executeSafeCommand(cmd)
            res.onSuccess {
                showMessage("DPI layar berhasil dipulihkan ke default bawaan.")
                loadDeviceInfo()
            }.onFailure { e ->
                showMessage("Gagal memulihkan DPI: ${e.localizedMessage}")
            }
            _isCommandExecuting.value = false
        }
    }

    // Pointer Location Toggle
    private fun checkPointerLocationState() {
        viewModelScope.launch {
            try {
                val loc = Settings.System.getInt(context.contentResolver, "pointer_location", 0)
                _pointerLocationActive.value = loc == 1
            } catch (e: Exception) {
                _pointerLocationActive.value = false
            }
        }
    }

    fun togglePointerLocation() {
        viewModelScope.launch {
            _isCommandExecuting.value = true
            val nextVal = if (_pointerLocationActive.value) 0 else 1
            val cmd = "settings put system pointer_location $nextVal"
            val res = shizukuManager.executeSafeCommand(cmd)
            res.onSuccess {
                _pointerLocationActive.value = nextVal == 1
                showMessage("Pointer Location telah ${if (nextVal == 1) "diaktifkan" else "dimatikan"}.")
            }.onFailure { e ->
                showMessage("Gagal mengubah Pointer Location: ${e.localizedMessage}. Memerlukan Shizuku.")
            }
            _isCommandExecuting.value = false
        }
    }

    // Crosshair Settings
    fun updateCrosshairConfig(config: CrosshairConfig) {
        preferencesRepository.saveCrosshairConfig(config)
        if (config.isEnabled) {
            CrosshairService.start(context, config)
        }
    }

    fun toggleCrosshairOverlay(targetEnabled: Boolean) {
        if (targetEnabled) {
            if (!Settings.canDrawOverlays(context)) {
                showMessage("Izin 'Tampilkan di atas aplikasi lain' (Overlay) diperlukan!")
                return
            }
            val newConfig = crosshairConfig.value.copy(isEnabled = true)
            preferencesRepository.saveCrosshairConfig(newConfig)
            CrosshairService.start(context, newConfig)
            showMessage("Crosshair Aim Assistant aktif di layar.")
        } else {
            val newConfig = crosshairConfig.value.copy(isEnabled = false)
            preferencesRepository.saveCrosshairConfig(newConfig)
            CrosshairService.stop(context)
            showMessage("Crosshair Aim Assistant dimatikan.")
        }
    }

    // Sensitivity Settings
    fun selectSensitivityProfile(profile: SensitivityProfile) {
        sensitivityRepository.setActiveProfile(profile)
        preferencesRepository.saveActiveProfile(profile)
        showMessage("Profil sensitivitas '${profile.name}' dipilih.")
    }

    fun saveActiveProfile(profile: SensitivityProfile) {
        sensitivityRepository.setActiveProfile(profile)
        preferencesRepository.saveActiveProfile(profile)
    }

    fun openSensitivityStudio() {
        navigateToSubScreen(SubScreen.SENSITIVITY_STUDIO)
    }

    fun updateActiveProfile(profile: SensitivityProfile) {
        saveActiveProfile(profile)
    }

    override fun onCleared() {
        super.onCleared()
        telemetryJob?.cancel()
        shizukuManager.cleanUp()
    }
}
