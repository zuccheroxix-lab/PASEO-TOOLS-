package com.example.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.model.CrosshairConfig
import com.example.model.CrosshairShape
import com.example.model.SensitivityProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

class PreferencesRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _crosshairConfig = MutableStateFlow(loadCrosshairConfig())
    val crosshairConfig: StateFlow<CrosshairConfig> = _crosshairConfig.asStateFlow()

    private val _activeProfile = MutableStateFlow(loadActiveProfile())
    val activeProfile: StateFlow<SensitivityProfile> = _activeProfile.asStateFlow()

    // Original display values for safe rollback
    var originalResolution: String?
        get() = prefs.getString(KEY_ORIGINAL_RES, null)
        set(value) {
            prefs.edit().putString(KEY_ORIGINAL_RES, value).apply()
        }

    var originalDpi: Int
        get() = prefs.getInt(KEY_ORIGINAL_DPI, -1)
        set(value) {
            prefs.edit().putInt(KEY_ORIGINAL_DPI, value).apply()
        }

    var supportLink: String
        get() = prefs.getString(KEY_SUPPORT_LINK, "https://github.com/zuccheroxix-lab/PASEO-TOOLS-") ?: "https://github.com/zuccheroxix-lab/PASEO-TOOLS-"
        set(value) {
            prefs.edit().putString(KEY_SUPPORT_LINK, value).apply()
        }

    var bugReportLink: String
        get() = prefs.getString(KEY_BUG_REPORT_LINK, "https://github.com/zuccheroxix-lab/PASEO-TOOLS-/issues") ?: "https://github.com/zuccheroxix-lab/PASEO-TOOLS-/issues"
        set(value) {
            prefs.edit().putString(KEY_BUG_REPORT_LINK, value).apply()
        }

    private val _favoritePackages = MutableStateFlow(getFavoritePackages())
    val favoritePackages: StateFlow<Set<String>> = _favoritePackages.asStateFlow()

    fun getFavoritePackages(): Set<String> {
        return prefs.getStringSet("favorite_game_packages", emptySet()) ?: emptySet()
    }

    fun toggleFavoritePackage(packageName: String): Boolean {
        val current = getFavoritePackages().toMutableSet()
        val isNowFavorite = if (current.contains(packageName)) {
            current.remove(packageName)
            false
        } else {
            current.add(packageName)
            true
        }
        prefs.edit().putStringSet("favorite_game_packages", current).apply()
        _favoritePackages.value = current
        return isNowFavorite
    }

    private fun loadCrosshairConfig(): CrosshairConfig {
        return try {
            val isEnabled = prefs.getBoolean(KEY_CH_ENABLED, false)
            val shapeName = prefs.getString(KEY_CH_SHAPE, CrosshairShape.CLASSIC_CROSS.name)
            val shape = try {
                CrosshairShape.valueOf(shapeName ?: CrosshairShape.CLASSIC_CROSS.name)
            } catch (e: Exception) {
                CrosshairShape.CLASSIC_CROSS
            }
            val color = prefs.getLong(KEY_CH_COLOR, 0xFFFF2E44)
            val size = prefs.getInt(KEY_CH_SIZE, 26)
            val thickness = prefs.getFloat(KEY_CH_THICKNESS, 2.5f)
            val gap = prefs.getFloat(KEY_CH_GAP, 5.0f)
            val opacity = prefs.getFloat(KEY_CH_OPACITY, 0.95f)
            val offsetX = prefs.getInt(KEY_CH_OFFSET_X, 0)
            val offsetY = prefs.getInt(KEY_CH_OFFSET_Y, 0)
            val centerDot = prefs.getBoolean(KEY_CH_CENTER_DOT, true)

            CrosshairConfig(
                isEnabled = isEnabled,
                shape = shape,
                colorValue = color,
                sizeDp = size,
                thicknessDp = thickness,
                gapDp = gap,
                opacity = opacity,
                offsetX = offsetX,
                offsetY = offsetY,
                showCenterDot = centerDot
            )
        } catch (e: Exception) {
            CrosshairConfig()
        }
    }

    fun saveCrosshairConfig(config: CrosshairConfig) {
        prefs.edit()
            .putBoolean(KEY_CH_ENABLED, config.isEnabled)
            .putString(KEY_CH_SHAPE, config.shape.name)
            .putLong(KEY_CH_COLOR, config.colorValue)
            .putInt(KEY_CH_SIZE, config.sizeDp)
            .putFloat(KEY_CH_THICKNESS, config.thicknessDp)
            .putFloat(KEY_CH_GAP, config.gapDp)
            .putFloat(KEY_CH_OPACITY, config.opacity)
            .putInt(KEY_CH_OFFSET_X, config.offsetX)
            .putInt(KEY_CH_OFFSET_Y, config.offsetY)
            .putBoolean(KEY_CH_CENTER_DOT, config.showCenterDot)
            .apply()

        _crosshairConfig.value = config
    }

    private val _crosshairPresets = MutableStateFlow(getAllCrosshairPresets())
    val crosshairPresets: StateFlow<List<com.example.model.CrosshairPreset>> = _crosshairPresets.asStateFlow()

    fun getAllCrosshairPresets(): List<com.example.model.CrosshairPreset> {
        val customs = loadCustomCrosshairPresets()
        return com.example.model.CrosshairPreset.DEFAULT_PRESETS + customs
    }

    private fun loadCustomCrosshairPresets(): List<com.example.model.CrosshairPreset> {
        val raw = prefs.getString("custom_crosshair_presets_json", null) ?: return emptyList()
        return try {
            val array = org.json.JSONArray(raw)
            val list = mutableListOf<com.example.model.CrosshairPreset>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val config = com.example.model.CrosshairConfig.fromJson(obj.getJSONObject("config").toString())
                if (config != null) {
                    list.add(
                        com.example.model.CrosshairPreset(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            config = config,
                            isCustom = true
                        )
                    )
                }
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveCustomCrosshairPreset(preset: com.example.model.CrosshairPreset) {
        val customs = loadCustomCrosshairPresets().toMutableList()
        val index = customs.indexOfFirst { it.id == preset.id }
        if (index >= 0) {
            customs[index] = preset
        } else {
            customs.add(preset)
        }
        val array = org.json.JSONArray()
        for (p in customs) {
            val obj = org.json.JSONObject().apply {
                put("id", p.id)
                put("name", p.name)
                put("config", org.json.JSONObject(p.config.toJson()))
                put("isCustom", true)
            }
            array.put(obj)
        }
        prefs.edit().putString("custom_crosshair_presets_json", array.toString()).apply()
        _crosshairPresets.value = getAllCrosshairPresets()
    }

    fun renameCrosshairPreset(id: String, newName: String): Boolean {
        if (com.example.model.CrosshairPreset.DEFAULT_PRESETS.any { it.id == id }) return false
        val customs = loadCustomCrosshairPresets().toMutableList()
        val index = customs.indexOfFirst { it.id == id }
        if (index < 0) return false
        val target = customs[index]
        customs[index] = target.copy(name = newName.trim())
        val array = org.json.JSONArray()
        for (p in customs) {
            val obj = org.json.JSONObject().apply {
                put("id", p.id)
                put("name", p.name)
                put("config", org.json.JSONObject(p.config.toJson()))
                put("isCustom", true)
            }
            array.put(obj)
        }
        prefs.edit().putString("custom_crosshair_presets_json", array.toString()).apply()
        _crosshairPresets.value = getAllCrosshairPresets()
        return true
    }

    fun deleteCrosshairPreset(id: String): Boolean {
        if (com.example.model.CrosshairPreset.DEFAULT_PRESETS.any { it.id == id }) return false
        val customs = loadCustomCrosshairPresets().toMutableList()
        val removed = customs.removeAll { it.id == id }
        if (removed) {
            val array = org.json.JSONArray()
            for (p in customs) {
                val obj = org.json.JSONObject().apply {
                    put("id", p.id)
                    put("name", p.name)
                    put("config", org.json.JSONObject(p.config.toJson()))
                    put("isCustom", true)
                }
                array.put(obj)
            }
            prefs.edit().putString("custom_crosshair_presets_json", array.toString()).apply()
            _crosshairPresets.value = getAllCrosshairPresets()
        }
        return removed
    }

    private fun loadActiveProfile(): SensitivityProfile {
        val rawJson = prefs.getString(KEY_ACTIVE_PROFILE_JSON, null)
        if (rawJson != null) {
            try {
                val json = JSONObject(rawJson)
                val curveName = json.optString("curveType", com.example.model.SensitivityCurveType.DYNAMIC.name)
                val curve = try {
                    com.example.model.SensitivityCurveType.valueOf(curveName)
                } catch (e: Exception) {
                    com.example.model.SensitivityCurveType.DYNAMIC
                }
                return SensitivityProfile(
                    id = json.getString("id"),
                    name = json.getString("name"),
                    description = json.optString("desc", ""),
                    gamePackage = if (json.has("gamePackage")) json.optString("gamePackage", null) else null,
                    sensiX = json.optDouble("sensiX", 1.0).toFloat(),
                    sensiY = json.optDouble("sensiY", 1.0).toFloat(),
                    generalSensi = json.getInt("general"),
                    redDotSensi = json.getInt("redDot"),
                    scope2xSensi = json.getInt("scope2x"),
                    scope4xSensi = json.getInt("scope4x"),
                    sniperSensi = json.getInt("sniper"),
                    freeLookSensi = json.getInt("freeLook"),
                    recommendedDpi = json.getInt("dpi"),
                    pointerSpeed = json.getInt("pointerSpeed"),
                    aimTrickTuningPercent = json.optInt("aimTrick", 15),
                    curveType = curve,
                    deadzone = json.optDouble("deadzone", 0.02).toFloat(),
                    smoothing = json.optDouble("smoothing", 0.10).toFloat(),
                    acceleration = json.optDouble("acceleration", 1.05).toFloat(),
                    isCustom = json.optBoolean("isCustom", true)
                )
            } catch (e: Exception) {
                // fall back to default preset
            }
        }
        return SensitivityProfile.PRESETS.first()
    }

    fun saveActiveProfile(profile: SensitivityProfile) {
        try {
            val json = JSONObject().apply {
                put("id", profile.id)
                put("name", profile.name)
                put("desc", profile.description)
                put("gamePackage", profile.gamePackage)
                put("sensiX", profile.sensiX.toDouble())
                put("sensiY", profile.sensiY.toDouble())
                put("general", profile.generalSensi)
                put("redDot", profile.redDotSensi)
                put("scope2x", profile.scope2xSensi)
                put("scope4x", profile.scope4xSensi)
                put("sniper", profile.sniperSensi)
                put("freeLook", profile.freeLookSensi)
                put("dpi", profile.recommendedDpi)
                put("pointerSpeed", profile.pointerSpeed)
                put("aimTrick", profile.aimTrickTuningPercent)
                put("curveType", profile.curveType.name)
                put("deadzone", profile.deadzone.toDouble())
                put("smoothing", profile.smoothing.toDouble())
                put("acceleration", profile.acceleration.toDouble())
                put("isCustom", profile.isCustom)
            }
            prefs.edit().putString(KEY_ACTIVE_PROFILE_JSON, json.toString()).apply()
            _activeProfile.value = profile
        } catch (e: Exception) {
            // Safe handling
        }
    }

    companion object {
        private const val PREFS_NAME = "paseo_sensi_prefs"
        private const val KEY_CH_ENABLED = "ch_enabled"
        private const val KEY_CH_SHAPE = "ch_shape"
        private const val KEY_CH_COLOR = "ch_color"
        private const val KEY_CH_SIZE = "ch_size"
        private const val KEY_CH_THICKNESS = "ch_thickness"
        private const val KEY_CH_GAP = "ch_gap"
        private const val KEY_CH_OPACITY = "ch_opacity"
        private const val KEY_CH_OFFSET_X = "ch_offset_x"
        private const val KEY_CH_OFFSET_Y = "ch_offset_y"
        private const val KEY_CH_CENTER_DOT = "ch_center_dot"

        private const val KEY_ACTIVE_PROFILE_JSON = "active_profile_json"
        private const val KEY_ORIGINAL_RES = "original_resolution"
        private const val KEY_ORIGINAL_DPI = "original_dpi"
        private const val KEY_SUPPORT_LINK = "support_link"
        private const val KEY_BUG_REPORT_LINK = "bug_report_link"

        private const val KEY_OPT_PERF = "opt_performance"
        private const val KEY_OPT_TOUCH = "opt_touch"
        private const val KEY_OPT_SMART_DPI = "opt_smart_dpi"
        private const val KEY_OPT_LATENCY = "opt_latency"
        private const val KEY_OPT_FAST_LAUNCH = "opt_fast_launch"
        private const val KEY_OPT_ROG_MONITORING = "opt_rog_monitoring"
    }

    // Game Optimizer Toggles
    fun isOptPerformanceEnabled(): Boolean = prefs.getBoolean(KEY_OPT_PERF, true)
    fun setOptPerformanceEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_OPT_PERF, enabled).apply()

    fun isOptTouchEnabled(): Boolean = prefs.getBoolean(KEY_OPT_TOUCH, true)
    fun setOptTouchEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_OPT_TOUCH, enabled).apply()

    fun isOptSmartDpiEnabled(): Boolean = prefs.getBoolean(KEY_OPT_SMART_DPI, false)
    fun setOptSmartDpiEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_OPT_SMART_DPI, enabled).apply()

    fun isOptLatencyEnabled(): Boolean = prefs.getBoolean(KEY_OPT_LATENCY, true)
    fun setOptLatencyEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_OPT_LATENCY, enabled).apply()

    fun isOptFastLaunchEnabled(): Boolean = prefs.getBoolean(KEY_OPT_FAST_LAUNCH, false)
    fun setOptFastLaunchEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_OPT_FAST_LAUNCH, enabled).apply()

    fun isOptRogMonitoringEnabled(): Boolean = prefs.getBoolean(KEY_OPT_ROG_MONITORING, true)
    fun setOptRogMonitoringEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_OPT_ROG_MONITORING, enabled).apply()

    // Safe Cache Calculator & Cleaner
    fun getAppCacheSize(context: Context): Long {
        return try {
            val cacheDir = context.cacheDir
            getFolderSize(cacheDir)
        } catch (e: Exception) {
            0L
        }
    }

    fun clearAppCache(context: Context): Boolean {
        return try {
            context.cacheDir.deleteRecursively()
        } catch (e: Exception) {
            false
        }
    }

    private fun getFolderSize(dir: java.io.File?): Long {
        if (dir == null || !dir.exists()) return 0L
        var total = 0L
        dir.listFiles()?.forEach { file ->
            total += if (file.isDirectory) getFolderSize(file) else file.length()
        }
        return total
    }
}
