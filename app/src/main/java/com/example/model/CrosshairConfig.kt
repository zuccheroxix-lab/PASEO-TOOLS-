package com.example.model

import org.json.JSONObject

enum class CrosshairShape(val displayName: String) {
    CLASSIC_CROSS("Classic Cross"),
    DOT("Precision Dot"),
    CIRCLE_CROSS("Circle Crosshair"),
    TACTICAL_T("Tactical T"),
    SNIPER_CIRCLE("Sniper Scope"),
    DIAMOND("Holo Diamond")
}

data class CrosshairPreset(
    val id: String,
    val name: String,
    val config: CrosshairConfig,
    val isCustom: Boolean = false
) {
    companion object {
        val DEFAULT_PRESETS = listOf(
            CrosshairPreset("preset_dot", "Red Precision Dot", CrosshairConfig(shape = CrosshairShape.DOT, colorValue = 0xFFFF1744, sizeDp = 18, thicknessDp = 3.0f)),
            CrosshairPreset("preset_classic", "Pro Tactical Cross", CrosshairConfig(shape = CrosshairShape.CLASSIC_CROSS, colorValue = 0xFF00E676, sizeDp = 24, thicknessDp = 2.0f, gapDp = 4.0f)),
            CrosshairPreset("preset_circle_cross", "Apex Circle Cross", CrosshairConfig(shape = CrosshairShape.CIRCLE_CROSS, colorValue = 0xFF00E5FF, sizeDp = 28, thicknessDp = 2.0f, gapDp = 5.0f)),
            CrosshairPreset("preset_tactical_t", "Counter T-Reticle", CrosshairConfig(shape = CrosshairShape.TACTICAL_T, colorValue = 0xFFFFD600, sizeDp = 22, thicknessDp = 2.5f)),
            CrosshairPreset("preset_diamond", "Holo Laser Diamond", CrosshairConfig(shape = CrosshairShape.DIAMOND, colorValue = 0xFFFF2E44, sizeDp = 26, thicknessDp = 2.0f)),
            CrosshairPreset("preset_sniper", "Long Range Scope", CrosshairConfig(shape = CrosshairShape.SNIPER_CIRCLE, colorValue = 0xFFFFFFFF, sizeDp = 32, thicknessDp = 1.5f))
        )
    }
}

data class CrosshairConfig(
    val isEnabled: Boolean = false,
    val shape: CrosshairShape = CrosshairShape.CLASSIC_CROSS,
    val colorValue: Long = 0xFFFF2E44, // Default Crimson Glow
    val sizeDp: Int = 26,
    val thicknessDp: Float = 2.5f,
    val gapDp: Float = 5.0f,
    val opacity: Float = 0.95f,
    val offsetX: Int = 0,
    val offsetY: Int = 0,
    val showCenterDot: Boolean = true,
    val centerDotRadius: Float = 2.0f
) {
    fun toJson(): String {
        return JSONObject().apply {
            put("isEnabled", isEnabled)
            put("shape", shape.name)
            put("colorValue", colorValue)
            put("sizeDp", sizeDp)
            put("thicknessDp", thicknessDp.toDouble())
            put("gapDp", gapDp.toDouble())
            put("opacity", opacity.toDouble())
            put("offsetX", offsetX)
            put("offsetY", offsetY)
            put("showCenterDot", showCenterDot)
            put("centerDotRadius", centerDotRadius.toDouble())
        }.toString(2)
    }

    companion object {
        fun fromJson(jsonStr: String): CrosshairConfig? {
            return try {
                val obj = JSONObject(jsonStr)
                val shapeName = obj.optString("shape", CrosshairShape.CLASSIC_CROSS.name)
                val shape = try {
                    CrosshairShape.valueOf(shapeName)
                } catch (e: Exception) {
                    CrosshairShape.CLASSIC_CROSS
                }
                CrosshairConfig(
                    isEnabled = obj.optBoolean("isEnabled", false),
                    shape = shape,
                    colorValue = obj.optLong("colorValue", 0xFFFF2E44),
                    sizeDp = obj.optInt("sizeDp", 26),
                    thicknessDp = obj.optDouble("thicknessDp", 2.5).toFloat(),
                    gapDp = obj.optDouble("gapDp", 5.0).toFloat(),
                    opacity = obj.optDouble("opacity", 0.95).toFloat(),
                    offsetX = obj.optInt("offsetX", 0),
                    offsetY = obj.optInt("offsetY", 0),
                    showCenterDot = obj.optBoolean("showCenterDot", true),
                    centerDotRadius = obj.optDouble("centerDotRadius", 2.0).toFloat()
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
