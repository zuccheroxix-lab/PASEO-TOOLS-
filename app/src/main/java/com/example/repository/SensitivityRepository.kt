package com.example.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.model.SensitivityCurveType
import com.example.model.SensitivityProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class SensitivityRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _activeProfile = MutableStateFlow(loadActiveProfile())
    val activeProfile: StateFlow<SensitivityProfile> = _activeProfile.asStateFlow()

    private val _profilesList = MutableStateFlow(loadAllProfiles())
    val profilesList: StateFlow<List<SensitivityProfile>> = _profilesList.asStateFlow()

    companion object {
        private const val PREFS_NAME = "paseo_sensi_profiles_prefs"
        private const val KEY_CUSTOM_PROFILES = "custom_profiles_json"
        private const val KEY_ACTIVE_PROFILE_ID = "active_profile_id"
        private const val KEY_GAME_MAPPINGS = "game_profile_mappings_json"
    }

    fun getAllProfiles(): List<SensitivityProfile> {
        val customList = loadCustomProfiles()
        return SensitivityProfile.PRESETS + customList
    }

    private fun loadAllProfiles(): List<SensitivityProfile> {
        return getAllProfiles()
    }

    private fun loadCustomProfiles(): List<SensitivityProfile> {
        val raw = prefs.getString(KEY_CUSTOM_PROFILES, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            val list = mutableListOf<SensitivityProfile>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(parseProfileJson(obj))
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseProfileJson(obj: JSONObject): SensitivityProfile {
        val curveTypeName = obj.optString("curveType", SensitivityCurveType.DYNAMIC.name)
        val curve = try {
            SensitivityCurveType.valueOf(curveTypeName)
        } catch (e: Exception) {
            SensitivityCurveType.DYNAMIC
        }

        return SensitivityProfile(
            id = obj.getString("id"),
            name = obj.getString("name"),
            description = obj.optString("desc", ""),
            gamePackage = if (obj.has("gamePackage")) obj.optString("gamePackage", null) else null,
            sensiX = obj.optDouble("sensiX", 1.0).toFloat(),
            sensiY = obj.optDouble("sensiY", 1.0).toFloat(),
            generalSensi = obj.optInt("general", 90),
            redDotSensi = obj.optInt("redDot", 85),
            scope2xSensi = obj.optInt("scope2x", 75),
            scope4xSensi = obj.optInt("scope4x", 68),
            sniperSensi = obj.optInt("sniper", 50),
            freeLookSensi = obj.optInt("freeLook", 80),
            recommendedDpi = obj.optInt("dpi", 480),
            pointerSpeed = obj.optInt("pointerSpeed", 3),
            aimTrickTuningPercent = obj.optInt("aimTrick", 15),
            curveType = curve,
            deadzone = obj.optDouble("deadzone", 0.02).toFloat(),
            smoothing = obj.optDouble("smoothing", 0.10).toFloat(),
            acceleration = obj.optDouble("acceleration", 1.05).toFloat(),
            isCustom = obj.optBoolean("isCustom", true)
        )
    }

    private fun profileToJson(profile: SensitivityProfile): JSONObject {
        return JSONObject().apply {
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
    }

    private fun loadActiveProfile(): SensitivityProfile {
        val activeId = prefs.getString(KEY_ACTIVE_PROFILE_ID, null)
        val all = getAllProfiles()
        if (activeId != null) {
            val found = all.firstOrNull { it.id == activeId }
            if (found != null) return found
        }
        return SensitivityProfile.PRESETS.first()
    }

    fun setActiveProfile(profile: SensitivityProfile) {
        prefs.edit().putString(KEY_ACTIVE_PROFILE_ID, profile.id).apply()
        _activeProfile.value = profile
        // If it's a custom profile or edited preset, update storage
        if (profile.isCustom) {
            saveCustomProfile(profile)
        }
    }

    fun saveCustomProfile(profile: SensitivityProfile) {
        val customs = loadCustomProfiles().toMutableList()
        val index = customs.indexOfFirst { it.id == profile.id }
        if (index >= 0) {
            customs[index] = profile
        } else {
            customs.add(profile)
        }
        persistCustomProfiles(customs)
        _profilesList.value = getAllProfiles()
        if (_activeProfile.value.id == profile.id) {
            _activeProfile.value = profile
        }
    }

    fun deleteProfile(id: String): Boolean {
        // Cannot delete built-in presets
        if (SensitivityProfile.PRESETS.any { it.id == id }) return false

        val customs = loadCustomProfiles().toMutableList()
        val removed = customs.removeAll { it.id == id }
        if (removed) {
            persistCustomProfiles(customs)
            _profilesList.value = getAllProfiles()
            if (_activeProfile.value.id == id) {
                setActiveProfile(SensitivityProfile.PRESETS.first())
            }
        }
        return removed
    }

    fun duplicateProfile(id: String, newName: String): SensitivityProfile? {
        val target = getAllProfiles().firstOrNull { it.id == id } ?: return null
        val duplicated = target.copy(
            id = "custom_" + UUID.randomUUID().toString().take(8),
            name = newName.ifBlank { "${target.name} (Copy)" },
            description = "Salinan kustom dari ${target.name}",
            isCustom = true
        )
        saveCustomProfile(duplicated)
        return duplicated
    }

    fun exportProfileJson(profile: SensitivityProfile): String {
        return profileToJson(profile).toString(2)
    }

    fun importProfileJson(jsonStr: String): SensitivityProfile? {
        return try {
            val obj = JSONObject(jsonStr)
            val imported = parseProfileJson(obj).copy(
                id = "custom_imp_" + UUID.randomUUID().toString().take(8),
                name = obj.optString("name", "Imported Profile") + " (Import)",
                isCustom = true
            )
            saveCustomProfile(imported)
            setActiveProfile(imported)
            imported
        } catch (e: Exception) {
            null
        }
    }

    fun resetActiveToDefault(): SensitivityProfile {
        val defaultProfile = SensitivityProfile.PRESETS.first()
        setActiveProfile(defaultProfile)
        return defaultProfile
    }

    // --- Per-Game Sensitivity Mapping ---

    fun assignProfileToGame(profileId: String, packageName: String) {
        val mappings = loadGameMappings().toMutableMap()
        mappings[packageName] = profileId
        val obj = JSONObject()
        mappings.forEach { (pkg, pId) -> obj.put(pkg, pId) }
        prefs.edit().putString(KEY_GAME_MAPPINGS, obj.toString()).apply()
    }

    fun getProfileForGame(packageName: String): SensitivityProfile? {
        val mappings = loadGameMappings()
        val profileId = mappings[packageName] ?: return null
        return getAllProfiles().firstOrNull { it.id == profileId }
    }

    private fun loadGameMappings(): Map<String, String> {
        val raw = prefs.getString(KEY_GAME_MAPPINGS, null) ?: return emptyMap()
        return try {
            val obj = JSONObject(raw)
            val map = mutableMapOf<String, String>()
            val keys = obj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                map[key] = obj.getString(key)
            }
            map
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private fun persistCustomProfiles(customs: List<SensitivityProfile>) {
        val array = JSONArray()
        for (c in customs) {
            array.put(profileToJson(c))
        }
        prefs.edit().putString(KEY_CUSTOM_PROFILES, array.toString()).apply()
    }
}
