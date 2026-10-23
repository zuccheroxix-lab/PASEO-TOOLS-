package com.example.model.mobilador

import android.view.KeyEvent
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

enum class MappingElementType(val displayName: String, val defaultKey: String, val defaultKeyCode: Int) {
    MOVEMENT_WASD("Movement (WASD)", "WASD", 0),
    FIRE("Fire / Tembak (L-Click)", "M_LEFT", 0),
    AIM("Aim / Scope (R-Click)", "M_RIGHT", 0),
    ACTION_KEY("Action Key", "E", KeyEvent.KEYCODE_E),
    JUMP("Jump / Lompat", "SPACE", KeyEvent.KEYCODE_SPACE),
    CROUCH("Crouch / Jongkok", "C", KeyEvent.KEYCODE_C),
    PRONE("Prone / Tiarap", "Z", KeyEvent.KEYCODE_Z),
    RELOAD("Reload / Isi Peluru", "R", KeyEvent.KEYCODE_R),
    INTERACT("Interact / Loot", "F", KeyEvent.KEYCODE_F),
    INVENTORY("Inventory / Tas", "TAB", KeyEvent.KEYCODE_TAB),
    MEDKIT("Medkit / Heal", "4", KeyEvent.KEYCODE_4),
    WEAPON_1("Weapon 1", "1", KeyEvent.KEYCODE_1),
    WEAPON_2("Weapon 2", "2", KeyEvent.KEYCODE_2),
    MOUSE_LOOK("Mouse Look / Kamera", "MOUSE", 0)
}

data class MappingElement(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: MappingElementType,
    val keyBinding: String,
    val keyCode: Int = 0,
    val normalizedX: Float, // 0.0f .. 1.0f on game screen preview
    val normalizedY: Float, // 0.0f .. 1.0f on game screen preview
    val sizeDp: Float = 50f,
    val opacity: Float = 0.85f,
    val sensitivity: Float = 1.0f
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("name", name)
            put("type", type.name)
            put("keyBinding", keyBinding)
            put("keyCode", keyCode)
            put("normalizedX", normalizedX.toDouble())
            put("normalizedY", normalizedY.toDouble())
            put("sizeDp", sizeDp.toDouble())
            put("opacity", opacity.toDouble())
            put("sensitivity", sensitivity.toDouble())
        }
    }

    companion object {
        fun fromJson(obj: JSONObject): MappingElement {
            val typeStr = obj.optString("type", MappingElementType.ACTION_KEY.name)
            val type = runCatching { MappingElementType.valueOf(typeStr) }.getOrDefault(MappingElementType.ACTION_KEY)
            return MappingElement(
                id = obj.optString("id", UUID.randomUUID().toString()),
                name = obj.optString("name", type.displayName),
                type = type,
                keyBinding = obj.optString("keyBinding", type.defaultKey),
                keyCode = obj.optInt("keyCode", type.defaultKeyCode),
                normalizedX = obj.optDouble("normalizedX", 0.5).toFloat(),
                normalizedY = obj.optDouble("normalizedY", 0.5).toFloat(),
                sizeDp = obj.optDouble("sizeDp", 50.0).toFloat(),
                opacity = obj.optDouble("opacity", 0.85).toFloat(),
                sensitivity = obj.optDouble("sensitivity", 1.0).toFloat()
            )
        }
    }
}

data class MobiladorProfile(
    val id: String,
    val name: String,
    val gamePackage: String? = null,
    val aspectRatio: String = "20:9", // "16:9", "20:9", "4:3"
    val mouseSensitivityX: Float = 1.0f,
    val mouseSensitivityY: Float = 1.0f,
    val isCursorHiddenInGame: Boolean = true,
    val elements: List<MappingElement>,
    val isCustom: Boolean = false
) {
    fun toJson(): String {
        return JSONObject().apply {
            put("id", id)
            put("name", name)
            put("gamePackage", gamePackage ?: "")
            put("aspectRatio", aspectRatio)
            put("mouseSensitivityX", mouseSensitivityX.toDouble())
            put("mouseSensitivityY", mouseSensitivityY.toDouble())
            put("isCursorHiddenInGame", isCursorHiddenInGame)
            put("isCustom", isCustom)
            val elemArray = JSONArray()
            elements.forEach { elemArray.put(it.toJson()) }
            put("elements", elemArray)
        }.toString(2)
    }

    companion object {
        fun fromJson(jsonStr: String): MobiladorProfile? {
            return try {
                val obj = JSONObject(jsonStr)
                val elemArray = obj.optJSONArray("elements") ?: JSONArray()
                val elemList = mutableListOf<MappingElement>()
                for (i in 0 until elemArray.length()) {
                    elemList.add(MappingElement.fromJson(elemArray.getJSONObject(i)))
                }
                MobiladorProfile(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    gamePackage = obj.optString("gamePackage", "").ifBlank { null },
                    aspectRatio = obj.optString("aspectRatio", "20:9"),
                    mouseSensitivityX = obj.optDouble("mouseSensitivityX", 1.0).toFloat(),
                    mouseSensitivityY = obj.optDouble("mouseSensitivityY", 1.0).toFloat(),
                    isCursorHiddenInGame = obj.optBoolean("isCursorHiddenInGame", true),
                    elements = elemList,
                    isCustom = obj.optBoolean("isCustom", true)
                )
            } catch (e: Exception) {
                null
            }
        }

        val DEFAULT_PROFILES = listOf(
            MobiladorProfile(
                id = "mob_preset_br",
                name = "Battle Royale Spec (Standard)",
                gamePackage = "com.dts.freefireth",
                aspectRatio = "20:9",
                mouseSensitivityX = 1.25f,
                mouseSensitivityY = 1.15f,
                isCursorHiddenInGame = true,
                isCustom = false,
                elements = listOf(
                    MappingElement(name = "WASD", type = MappingElementType.MOVEMENT_WASD, keyBinding = "W,A,S,D", normalizedX = 0.18f, normalizedY = 0.68f, sizeDp = 90f),
                    MappingElement(name = "Fire", type = MappingElementType.FIRE, keyBinding = "M_LEFT", normalizedX = 0.85f, normalizedY = 0.65f, sizeDp = 55f),
                    MappingElement(name = "Scope", type = MappingElementType.AIM, keyBinding = "M_RIGHT", normalizedX = 0.88f, normalizedY = 0.38f, sizeDp = 50f),
                    MappingElement(name = "Jump", type = MappingElementType.JUMP, keyBinding = "SPACE", keyCode = KeyEvent.KEYCODE_SPACE, normalizedX = 0.94f, normalizedY = 0.78f, sizeDp = 48f),
                    MappingElement(name = "Crouch", type = MappingElementType.CROUCH, keyBinding = "C", keyCode = KeyEvent.KEYCODE_C, normalizedX = 0.82f, normalizedY = 0.82f, sizeDp = 48f),
                    MappingElement(name = "Reload", type = MappingElementType.RELOAD, keyBinding = "R", keyCode = KeyEvent.KEYCODE_R, normalizedX = 0.72f, normalizedY = 0.22f, sizeDp = 45f),
                    MappingElement(name = "Loot", type = MappingElementType.INTERACT, keyBinding = "F", keyCode = KeyEvent.KEYCODE_F, normalizedX = 0.70f, normalizedY = 0.40f, sizeDp = 45f),
                    MappingElement(name = "Bag", type = MappingElementType.INVENTORY, keyBinding = "TAB", keyCode = KeyEvent.KEYCODE_TAB, normalizedX = 0.10f, normalizedY = 0.82f, sizeDp = 45f),
                    MappingElement(name = "Med", type = MappingElementType.MEDKIT, keyBinding = "4", keyCode = KeyEvent.KEYCODE_4, normalizedX = 0.12f, normalizedY = 0.35f, sizeDp = 45f),
                    MappingElement(name = "Camera Look", type = MappingElementType.MOUSE_LOOK, keyBinding = "MOUSE", normalizedX = 0.55f, normalizedY = 0.45f, sizeDp = 75f)
                )
            ),
            MobiladorProfile(
                id = "mob_preset_tactical",
                name = "Tactical FPS Spec (CODM / Standoff)",
                gamePackage = "com.activision.callofduty.shooter",
                aspectRatio = "20:9",
                mouseSensitivityX = 1.10f,
                mouseSensitivityY = 1.05f,
                isCursorHiddenInGame = true,
                isCustom = false,
                elements = listOf(
                    MappingElement(name = "WASD", type = MappingElementType.MOVEMENT_WASD, keyBinding = "W,A,S,D", normalizedX = 0.18f, normalizedY = 0.70f, sizeDp = 85f),
                    MappingElement(name = "Shoot", type = MappingElementType.FIRE, keyBinding = "M_LEFT", normalizedX = 0.86f, normalizedY = 0.62f, sizeDp = 55f),
                    MappingElement(name = "Aim Scope", type = MappingElementType.AIM, keyBinding = "M_RIGHT", normalizedX = 0.88f, normalizedY = 0.42f, sizeDp = 52f),
                    MappingElement(name = "Jump", type = MappingElementType.JUMP, keyBinding = "SPACE", keyCode = KeyEvent.KEYCODE_SPACE, normalizedX = 0.92f, normalizedY = 0.78f, sizeDp = 48f),
                    MappingElement(name = "Slide/Crouch", type = MappingElementType.CROUCH, keyBinding = "C", keyCode = KeyEvent.KEYCODE_C, normalizedX = 0.80f, normalizedY = 0.80f, sizeDp = 48f),
                    MappingElement(name = "Prone", type = MappingElementType.PRONE, keyBinding = "Z", keyCode = KeyEvent.KEYCODE_Z, normalizedX = 0.75f, normalizedY = 0.88f, sizeDp = 46f),
                    MappingElement(name = "Reload", type = MappingElementType.RELOAD, keyBinding = "R", keyCode = KeyEvent.KEYCODE_R, normalizedX = 0.76f, normalizedY = 0.30f, sizeDp = 45f),
                    MappingElement(name = "Primary Gun", type = MappingElementType.WEAPON_1, keyBinding = "1", keyCode = KeyEvent.KEYCODE_1, normalizedX = 0.55f, normalizedY = 0.85f, sizeDp = 45f),
                    MappingElement(name = "Secondary Gun", type = MappingElementType.WEAPON_2, keyBinding = "2", keyCode = KeyEvent.KEYCODE_2, normalizedX = 0.64f, normalizedY = 0.85f, sizeDp = 45f),
                    MappingElement(name = "Mouse Aim", type = MappingElementType.MOUSE_LOOK, keyBinding = "MOUSE", normalizedX = 0.60f, normalizedY = 0.45f, sizeDp = 75f)
                )
            )
        )
    }
}

data class ConnectedInputDeviceInfo(
    val id: Int,
    val name: String,
    val isKeyboard: Boolean,
    val isMouse: Boolean,
    val isBluetooth: Boolean,
    val isUsbOtg: Boolean
)
