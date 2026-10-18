package com.example.model

enum class CrosshairShape(val displayName: String) {
    CLASSIC_CROSS("Classic Cross"),
    DOT("Precision Dot"),
    CIRCLE_CROSS("Circle Crosshair"),
    TACTICAL_T("Tactical T"),
    SNIPER_CIRCLE("Sniper Scope"),
    DIAMOND("Holo Diamond")
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
)
