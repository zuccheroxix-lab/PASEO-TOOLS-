package com.example.model

enum class SensitivityCurveType(val displayName: String, val description: String) {
    LINEAR("Linear", "Rasio 1:1 langsung tanpa akselerasi buatan."),
    DYNAMIC("Dynamic / S-Curve", "Presisi micro-adjust di tengah dan responsivitas cepat saat drag jauh."),
    EXPONENTIAL("Exponential", "Pergerakan lambat untuk bidikan jarak jauh, sangat cepat pada swipe tajam."),
    CONTROL_SMOOTH("Controlled Smooth", "Menstabilkan tremor jari dan getaran sentuh mikro.")
}

data class SensitivityProfile(
    val id: String,
    val name: String,
    val description: String,
    val gamePackage: String? = null,
    val sensiX: Float = 1.0f, // Sensitivity multiplier for horizontal axis (e.g. 0.1x to 3.0x)
    val sensiY: Float = 1.0f, // Sensitivity multiplier for vertical axis
    val generalSensi: Int = 90,
    val redDotSensi: Int = 85,
    val scope2xSensi: Int = 75,
    val scope4xSensi: Int = 68,
    val sniperSensi: Int = 50,
    val freeLookSensi: Int = 80,
    val recommendedDpi: Int = 480,
    val pointerSpeed: Int = 3, // -7 to 7 Android pointer speed scale
    val aimTrickTuningPercent: Int = 15, // 0 to 15% dynamic micro tuning curve
    val curveType: SensitivityCurveType = SensitivityCurveType.DYNAMIC,
    val deadzone: Float = 0.02f, // 0.00 to 0.15 deadzone filter
    val smoothing: Float = 0.10f, // 0.00 to 0.50 jitter reduction
    val acceleration: Float = 1.05f, // velocity-based acceleration factor
    val isCustom: Boolean = false
) {
    companion object {
        val PRESETS = listOf(
            SensitivityProfile(
                id = "preset_aggressive",
                name = "Aggressive Rush (High)",
                description = "Responsivitas putaran kamera ultra cepat untuk pertempuran jarak dekat (Close Combat).",
                sensiX = 1.45f,
                sensiY = 1.35f,
                generalSensi = 98,
                redDotSensi = 92,
                scope2xSensi = 88,
                scope4xSensi = 82,
                sniperSensi = 65,
                freeLookSensi = 95,
                recommendedDpi = 580,
                pointerSpeed = 5,
                aimTrickTuningPercent = 15,
                curveType = SensitivityCurveType.EXPONENTIAL,
                deadzone = 0.01f,
                smoothing = 0.05f,
                acceleration = 1.20f
            ),
            SensitivityProfile(
                id = "preset_balanced",
                name = "Balanced Precision (Medium)",
                description = "Keseimbangan ideal antara kecepatan drag-shot dan kestabilan recoil.",
                sensiX = 1.15f,
                sensiY = 1.10f,
                generalSensi = 85,
                redDotSensi = 80,
                scope2xSensi = 74,
                scope4xSensi = 68,
                sniperSensi = 55,
                freeLookSensi = 80,
                recommendedDpi = 460,
                pointerSpeed = 3,
                aimTrickTuningPercent = 12,
                curveType = SensitivityCurveType.DYNAMIC,
                deadzone = 0.02f,
                smoothing = 0.12f,
                acceleration = 1.05f
            ),
            SensitivityProfile(
                id = "preset_sniper",
                name = "Sniper Control (Steady)",
                description = "Fokus pada presisi tinggi tembakan jarak jauh dengan micro-adjustment stabil.",
                sensiX = 0.85f,
                sensiY = 0.80f,
                generalSensi = 72,
                redDotSensi = 68,
                scope2xSensi = 62,
                scope4xSensi = 55,
                sniperSensi = 42,
                freeLookSensi = 70,
                recommendedDpi = 411,
                pointerSpeed = 1,
                aimTrickTuningPercent = 8,
                curveType = SensitivityCurveType.CONTROL_SMOOTH,
                deadzone = 0.03f,
                smoothing = 0.25f,
                acceleration = 1.00f
            ),
            SensitivityProfile(
                id = "preset_pro_tournament",
                name = "ZX Pro Tournament Spec",
                description = "Konfigurasi resmi ZUCCHERO XANN yang disetel khusus untuk rasio headshot konsisten.",
                sensiX = 1.30f,
                sensiY = 1.22f,
                generalSensi = 94,
                redDotSensi = 89,
                scope2xSensi = 84,
                scope4xSensi = 78,
                sniperSensi = 60,
                freeLookSensi = 90,
                recommendedDpi = 520,
                pointerSpeed = 4,
                aimTrickTuningPercent = 15,
                curveType = SensitivityCurveType.DYNAMIC,
                deadzone = 0.015f,
                smoothing = 0.08f,
                acceleration = 1.12f
            )
        )
    }
}
