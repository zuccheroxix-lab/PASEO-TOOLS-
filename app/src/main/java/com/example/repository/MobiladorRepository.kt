package com.example.repository

import android.content.Context
import android.content.SharedPreferences
import android.view.InputDevice
import com.example.model.mobilador.ConnectedInputDeviceInfo
import com.example.model.mobilador.MobiladorProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import java.util.UUID

class MobiladorRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("paseo_mobilador_prefs", Context.MODE_PRIVATE)

    private val _allProfiles = MutableStateFlow<List<MobiladorProfile>>(emptyList())
    val allProfiles: StateFlow<List<MobiladorProfile>> = _allProfiles.asStateFlow()

    private val _activeProfile = MutableStateFlow(MobiladorProfile.DEFAULT_PROFILES.first())
    val activeProfile: StateFlow<MobiladorProfile> = _activeProfile.asStateFlow()

    private val _connectedDevices = MutableStateFlow<List<ConnectedInputDeviceInfo>>(emptyList())
    val connectedDevices: StateFlow<List<ConnectedInputDeviceInfo>> = _connectedDevices.asStateFlow()

    // Undo / Redo history for Mapping Editor
    private val undoStack = mutableListOf<MobiladorProfile>()
    private val redoStack = mutableListOf<MobiladorProfile>()

    init {
        loadProfiles()
        scanConnectedDevices()
    }

    fun scanConnectedDevices(): List<ConnectedInputDeviceInfo> {
        val list = mutableListOf<ConnectedInputDeviceInfo>()
        val deviceIds = InputDevice.getDeviceIds()

        for (id in deviceIds) {
            val device = InputDevice.getDevice(id) ?: continue
            // Skip purely internal virtual navigation devices
            if (device.isVirtual) continue

            val sources = device.sources
            val hasKeyboard = (sources and InputDevice.SOURCE_KEYBOARD) == InputDevice.SOURCE_KEYBOARD && device.keyboardType == InputDevice.KEYBOARD_TYPE_ALPHABETIC
            val hasMouse = (sources and InputDevice.SOURCE_MOUSE) == InputDevice.SOURCE_MOUSE

            if (hasKeyboard || hasMouse) {
                val nameLower = device.name.lowercase()
                val isBt = nameLower.contains("bluetooth") || nameLower.contains("bt") || nameLower.contains("wireless")
                val isUsb = nameLower.contains("usb") || nameLower.contains("otg") || !isBt

                list.add(
                    ConnectedInputDeviceInfo(
                        id = id,
                        name = device.name,
                        isKeyboard = hasKeyboard,
                        isMouse = hasMouse,
                        isBluetooth = isBt,
                        isUsbOtg = isUsb
                    )
                )
            }
        }

        _connectedDevices.value = list
        return list
    }

    private fun loadProfiles() {
        val customRaw = prefs.getString("custom_mobilador_profiles_json", null)
        val customList = mutableListOf<MobiladorProfile>()

        if (!customRaw.isNullOrBlank()) {
            try {
                val array = JSONArray(customRaw)
                for (i in 0 until array.length()) {
                    val p = MobiladorProfile.fromJson(array.getJSONObject(i).toString())
                    if (p != null) {
                        customList.add(p)
                    }
                }
            } catch (_: Exception) {}
        }

        val combined = MobiladorProfile.DEFAULT_PROFILES + customList
        _allProfiles.value = combined

        val activeId = prefs.getString("active_mobilador_id", null)
        val matched = combined.find { it.id == activeId } ?: MobiladorProfile.DEFAULT_PROFILES.first()
        _activeProfile.value = matched
    }

    fun setActiveProfile(profile: MobiladorProfile) {
        _activeProfile.value = profile
        prefs.edit().putString("active_mobilador_id", profile.id).apply()
        clearHistory()
    }

    fun updateEditorProfile(profile: MobiladorProfile) {
        // Record undo state before modification
        pushUndo(_activeProfile.value)
        _activeProfile.value = profile
    }

    fun saveCustomProfile(profile: MobiladorProfile): MobiladorProfile {
        val prepared = if (profile.id.isBlank() || !profile.isCustom) {
            profile.copy(
                id = "mob_cust_" + UUID.randomUUID().toString().take(8),
                isCustom = true
            )
        } else {
            profile.copy(isCustom = true)
        }

        val list = _allProfiles.value.toMutableList()
        val index = list.indexOfFirst { it.id == prepared.id }
        if (index >= 0) {
            list[index] = prepared
        } else {
            list.add(prepared)
        }

        _allProfiles.value = list
        persistCustomProfiles(list.filter { it.isCustom })
        _activeProfile.value = prepared
        prefs.edit().putString("active_mobilador_id", prepared.id).apply()
        return prepared
    }

    fun duplicateProfile(profile: MobiladorProfile): MobiladorProfile {
        val duplicated = profile.copy(
            id = "mob_cust_" + UUID.randomUUID().toString().take(8),
            name = "${profile.name} (Copy)",
            isCustom = true
        )
        return saveCustomProfile(duplicated)
    }

    fun renameProfile(id: String, newName: String): Boolean {
        val target = _allProfiles.value.find { it.id == id } ?: return false
        if (!target.isCustom) return false
        val renamed = target.copy(name = newName.trim())
        saveCustomProfile(renamed)
        return true
    }

    fun deleteProfile(id: String): Boolean {
        val target = _allProfiles.value.find { it.id == id } ?: return false
        if (!target.isCustom) return false

        val list = _allProfiles.value.filter { it.id != id }
        _allProfiles.value = list
        persistCustomProfiles(list.filter { it.isCustom })

        if (_activeProfile.value.id == id) {
            val fallback = MobiladorProfile.DEFAULT_PROFILES.first()
            setActiveProfile(fallback)
        }
        return true
    }

    fun resetToDefaults() {
        prefs.edit().remove("custom_mobilador_profiles_json").remove("active_mobilador_id").apply()
        _allProfiles.value = MobiladorProfile.DEFAULT_PROFILES
        val fallback = MobiladorProfile.DEFAULT_PROFILES.first()
        setActiveProfile(fallback)
    }

    // Undo / Redo
    fun canUndo(): Boolean = undoStack.isNotEmpty()
    fun canRedo(): Boolean = redoStack.isNotEmpty()

    fun undo(): MobiladorProfile? {
        if (undoStack.isEmpty()) return null
        val previous = undoStack.removeAt(undoStack.size - 1)
        redoStack.add(_activeProfile.value)
        _activeProfile.value = previous
        return previous
    }

    fun redo(): MobiladorProfile? {
        if (redoStack.isEmpty()) return null
        val next = redoStack.removeAt(redoStack.size - 1)
        undoStack.add(_activeProfile.value)
        _activeProfile.value = next
        return next
    }

    private fun pushUndo(profile: MobiladorProfile) {
        if (undoStack.size >= 25) {
            undoStack.removeAt(0)
        }
        undoStack.add(profile)
        redoStack.clear()
    }

    private fun clearHistory() {
        undoStack.clear()
        redoStack.clear()
    }

    private fun persistCustomProfiles(customs: List<MobiladorProfile>) {
        val array = JSONArray()
        for (p in customs) {
            array.put(org.json.JSONObject(p.toJson()))
        }
        prefs.edit().putString("custom_mobilador_profiles_json", array.toString()).apply()
    }

    fun exportProfileJson(profile: MobiladorProfile): String {
        return profile.toJson()
    }

    fun importProfileJson(json: String): Result<MobiladorProfile> {
        return runCatching {
            val parsed = MobiladorProfile.fromJson(json)
                ?: throw IllegalArgumentException("Format JSON profil Mobilador tidak valid.")
            val imported = parsed.copy(
                id = "mob_imp_" + UUID.randomUUID().toString().take(8),
                isCustom = true
            )
            saveCustomProfile(imported)
        }
    }
}
