package com.example.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.model.SensitivityCurveType
import com.example.model.SensitivityProfile
import com.example.nativecore.SensitivityBridge
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * PASEO SENSI — Sensitivity Repository
 * Developer: ZUCCHERO XANN
 *
 * Manages persistence, active profile selection, preset galleries, custom user tuning,
 * and JSON configuration import/export. Connected directly to SensitivityBridge.
 */
class SensitivityRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("paseo_sensitivity_prefs", Context.MODE_PRIVATE)

    val bridge = SensitivityBridge()

    private val _allProfiles = MutableStateFlow<List<SensitivityProfile>>(emptyList())
    val allProfiles: StateFlow<List<SensitivityProfile>> = _allProfiles.asStateFlow()

    private val _activeProfile = MutableStateFlow<SensitivityProfile>(SensitivityProfile.PRESETS.first())
    val activeProfile: StateFlow<SensitivityProfile> = _activeProfile.asStateFlow()

    init {
        loadProfiles()
    }

    private fun loadProfiles() {
        val customJson = prefs.getString("custom_profiles_json", null)
        val customList = mutableListOf<SensitivityProfile>()

        if (!customJson.isNullOrBlank()) {
            try {
                val array = JSONArray(customJson)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    customList.add(deserializeProfile(obj))
                }
            } catch (e: Exception) {
                // If corrupted, ignore custom
            }
        }

        val combined = SensitivityProfile.PRESETS + customList
        _allProfiles.value = combined

        val activeId = prefs.getString("active_profile_id", null)
        val matched = combined.find { it.id == activeId } ?: SensitivityProfile.PRESETS.first()
        _activeProfile.value = matched
        bridge.setProfile(matched)
    }

    fun setActiveProfile(profile: SensitivityProfile) {
        _activeProfile.value = profile
        prefs.edit().putString("active_profile_id", profile.id).apply()
        bridge.setProfile(profile)
    }

    fun saveCustomProfile(profile: SensitivityProfile): SensitivityProfile {
        val prepared = if (profile.id.isBlank() || !profile.isCustom) {
            profile.copy(
                id = "custom_" + UUID.randomUUID().toString().take(8),
                isCustom = true
            )
        } else {
            profile.copy(isCustom = true)
        }

        val currentList = _allProfiles.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == prepared.id }
        if (index >= 0) {
            currentList[index] = prepared
        } else {
            currentList.add(prepared)
        }

        _allProfiles.value = currentList
        persistCustomProfiles(currentList.filter { it.isCustom })

        if (_activeProfile.value.id == prepared.id) {
            _activeProfile.value = prepared
            bridge.setProfile(prepared)
        }
        return prepared
    }

    fun duplicateProfile(profile: SensitivityProfile): SensitivityProfile {
        val dup = profile.copy(
            id = "custom_" + UUID.randomUUID().toString().take(8),
            name = "${profile.name} (Copy)",
            isCustom = true
        )
        return saveCustomProfile(dup)
    }

    fun deleteProfile(id: String): Boolean {
        val target = _allProfiles.value.find { it.id == id } ?: return false
        if (!target.isCustom) return false // Cannot delete built-in presets

        val updated = _allProfiles.value.filter { it.id != id }
        _allProfiles.value = updated
        persistCustomProfiles(updated.filter { it.isCustom })

        if (_activeProfile.value.id == id) {
            val fallback = SensitivityProfile.PRESETS.first()
            setActiveProfile(fallback)
        }
        return true
    }

    fun getProfileForGame(packageName: String): SensitivityProfile? {
        return _allProfiles.value.find { it.gamePackage == packageName }
    }

    fun saveProfileForGame(packageName: String, gameName: String, profile: SensitivityProfile): SensitivityProfile {
        val existing = getProfileForGame(packageName)
        val profileToSave = profile.copy(
            id = existing?.id ?: ("game_" + UUID.randomUUID().toString().take(8)),
            name = if (profile.name.isBlank()) "$gameName Spec" else profile.name,
            gamePackage = packageName,
            isCustom = true
        )
        return saveCustomProfile(profileToSave)
    }

    fun resetToDefaults() {
        prefs.edit().remove("custom_profiles_json").remove("active_profile_id").apply()
        _allProfiles.value = SensitivityProfile.PRESETS
        val defaultProfile = SensitivityProfile.PRESETS.first()
        setActiveProfile(defaultProfile)
    }

    private fun persistCustomProfiles(customList: List<SensitivityProfile>) {
        val array = JSONArray()
        for (p in customList) {
            array.put(serializeProfile(p))
        }
        prefs.edit().putString("custom_profiles_json", array.toString()).apply()
    }

    fun exportProfilesJson(): String {
        val array = JSONArray()
        for (p in _allProfiles.value) {
            array.put(serializeProfile(p))
        }
        return array.toString(2)
    }

    fun importProfilesJson(json: String): Result<Int> {
        return runCatching {
            val array = JSONArray(json)
            var count = 0
            val importedList = mutableListOf<SensitivityProfile>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val prof = deserializeProfile(obj).copy(
                    id = "imported_" + UUID.randomUUID().toString().take(8),
                    isCustom = true
                )
                importedList.add(prof)
                count++
            }
            if (importedList.isNotEmpty()) {
                val combined = _allProfiles.value.toMutableList()
                combined.addAll(importedList)
                _allProfiles.value = combined
                persistCustomProfiles(combined.filter { it.isCustom })
            }
            count
        }
    }

    private fun serializeProfile(p: SensitivityProfile): JSONObject {
        return JSONObject().apply {
            put("id", p.id)
            put("name", p.name)
            put("description", p.description)
            put("gamePackage", p.gamePackage ?: "")
            put("sensiX", p.sensiX.toDouble())
            put("sensiY", p.sensiY.toDouble())
            put("generalSensi", p.generalSensi)
            put("redDotSensi", p.redDotSensi)
            put("scope2xSensi", p.scope2xSensi)
            put("scope4xSensi", p.scope4xSensi)
            put("sniperSensi", p.sniperSensi)
            put("freeLookSensi", p.freeLookSensi)
            put("recommendedDpi", p.recommendedDpi)
            put("pointerSpeed", p.pointerSpeed)
            put("aimTrickTuningPercent", p.aimTrickTuningPercent)
            put("curveType", p.curveType.name)
            put("deadzone", p.deadzone.toDouble())
            put("smoothing", p.smoothing.toDouble())
            put("acceleration", p.acceleration.toDouble())
            put("isCustom", p.isCustom)
        }
    }

    private fun deserializeProfile(obj: JSONObject): SensitivityProfile {
        val curveName = obj.optString("curveType", SensitivityCurveType.DYNAMIC.name)
        val curve = runCatching { SensitivityCurveType.valueOf(curveName) }.getOrDefault(SensitivityCurveType.DYNAMIC)
        val gamePkg = obj.optString("gamePackage", "").ifBlank { null }

        return SensitivityProfile(
            id = obj.optString("id", UUID.randomUUID().toString()),
            name = obj.optString("name", "Custom Profile"),
            description = obj.optString("description", "Profil sensitivitas kustom"),
            gamePackage = gamePkg,
            sensiX = obj.optDouble("sensiX", 1.0).toFloat(),
            sensiY = obj.optDouble("sensiY", 1.0).toFloat(),
            generalSensi = obj.optInt("generalSensi", 90),
            redDotSensi = obj.optInt("redDotSensi", 85),
            scope2xSensi = obj.optInt("scope2xSensi", 75),
            scope4xSensi = obj.optInt("scope4xSensi", 68),
            sniperSensi = obj.optInt("sniperSensi", 50),
            freeLookSensi = obj.optInt("freeLookSensi", 80),
            recommendedDpi = obj.optInt("recommendedDpi", 480),
            pointerSpeed = obj.optInt("pointerSpeed", 3),
            aimTrickTuningPercent = obj.optInt("aimTrickTuningPercent", 15),
            curveType = curve,
            deadzone = obj.optDouble("deadzone", 0.02).toFloat(),
            smoothing = obj.optDouble("smoothing", 0.10).toFloat(),
            acceleration = obj.optDouble("acceleration", 1.05).toFloat(),
            isCustom = obj.optBoolean("isCustom", true)
        )
    }
}
