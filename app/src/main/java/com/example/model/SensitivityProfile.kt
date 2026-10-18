package com.example.model

data class SensitivityProfile(
    val id: String,
    val name: String,
    val description: String,
    val generalSensi: Int,
    val redDotSensi: Int,
    val scope2xSensi: Int,
    val scope4xSensi: Int,
    val sniperSensi: Int,
    val freeLookSensi: Int,
    val recommendedDpi: Int,
    val pointerSpeed: Int, // -7 to 7 Android pointer speed scale
    val aimTrickTuningPercent: Int = 15, // 0 to 15% dynamic micro tuning curve
    val isCustom: Boolean = false
) {
    companion object {
        val PRESETS = listOf(
            SensitivityProfile(
                id = "preset_aggressive",
                name = "Aggressive Rush (High)",
                description = "Responsivitas putaran kamera ultra cepat untuk pertempuran jarak dekat (Close Combat).",
                generalSensi = 98,
                redDotSensi = 92,
                scope2xSensi = 88,
                scope4xSensi = 82,
                sniperSensi = 65,
                freeLookSensi = 95,
                recommendedDpi = 580,
                pointerSpeed = 5,
                aimTrickTuningPercent = 15
            ),
            SensitivityProfile(
                id = "preset_balanced",
                name = "Balanced Precision (Medium)",
                description = "Keseimbangan ideal antara kecepatan drag-shot dan kestabilan recoil.",
                generalSensi = 85,
                redDotSensi = 80,
                scope2xSensi = 74,
                scope4xSensi = 68,
                sniperSensi = 55,
                freeLookSensi = 80,
                recommendedDpi = 460,
                pointerSpeed = 3,
                aimTrickTuningPercent = 12
            ),
            SensitivityProfile(
                id = "preset_sniper",
                name = "Sniper Control (Steady)",
                description = "Fokus pada presisi tinggi tembakan jarak jauh dengan micro-adjustment stabil.",
                generalSensi = 72,
                redDotSensi = 68,
                scope2xSensi = 62,
                scope4xSensi = 55,
                sniperSensi = 42,
                freeLookSensi = 70,
                recommendedDpi = 411,
                pointerSpeed = 1,
                aimTrickTuningPercent = 8
            ),
            SensitivityProfile(
                id = "preset_pro_tournament",
                name = "ZX Pro Tournament Spec",
                description = "Konfigurasi resmi ZUCCHERO XANN yang disetel khusus untuk rasio headshot konsisten.",
                generalSensi = 94,
                redDotSensi = 89,
                scope2xSensi = 84,
                scope4xSensi = 78,
                sniperSensi = 60,
                freeLookSensi = 90,
                recommendedDpi = 520,
                pointerSpeed = 4,
                aimTrickTuningPercent = 15
            )
        )
    }
}
