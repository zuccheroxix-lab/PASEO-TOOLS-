package com.example.service

import android.content.Context
import android.util.Log
import com.example.shizuku.ShizukuManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class InjectionCapability(
    val title: String,
    val description: String,
    val canInjectCrossApp: Boolean
) {
    SHIZUKU_PRIVILEGED(
        title = "Privileged Touch Injection (Shizuku Aktif)",
        description = "Perintah 'input tap/swipe' berjalan dengan izin shell ADB privileged. Input sentuh dapat diinjeksikan langsung ke dalam game apapun tanpa root.",
        canInjectCrossApp = true
    ),
    HARDWARE_CONVERTER(
        title = "Hardware Converter HID (Kugamer/Mix/Flydigi)",
        description = "Konverter fisik keyboard & mouse terdeteksi. Konverter mentranslasikan tombol keyboard & mouse langsung menjadi protokol layar sentuh melalui kernel Android.",
        canInjectCrossApp = true
    ),
    VISUAL_HUD_ONLY(
        title = "Visual HUD Mapping (Injeksi Memerlukan Shizuku / Konverter)",
        description = "Overlay menampilkan panduan visual tata letak tombol di atas game. Untuk injeksi otomatis sentuhan lintas aplikasi, hubungkan Shizuku atau gunakan perangkat konverter hardware OTG/Bluetooth.",
        canInjectCrossApp = false
    )
}

class InputInjectionManager(
    private val context: Context,
    private val shizukuManager: ShizukuManager
) {
    companion object {
        private const val TAG = "InputInjectionManager"
    }

    fun getCapability(hasHardwareConverter: Boolean): InjectionCapability {
        return when {
            shizukuManager.isAuthorized() -> InjectionCapability.SHIZUKU_PRIVILEGED
            hasHardwareConverter -> InjectionCapability.HARDWARE_CONVERTER
            else -> InjectionCapability.VISUAL_HUD_ONLY
        }
    }

    suspend fun injectTap(
        normalizedX: Float,
        normalizedY: Float,
        screenWidthPx: Int,
        screenHeightPx: Int
    ): Result<String> = withContext(Dispatchers.IO) {
        val pixelX = (normalizedX * screenWidthPx).toInt().coerceIn(0, screenWidthPx)
        val pixelY = (normalizedY * screenHeightPx).toInt().coerceIn(0, screenHeightPx)

        if (shizukuManager.isAuthorized()) {
            val cmd = "input tap $pixelX $pixelY"
            val res = shizukuManager.executeSafeCommand(cmd)
            res.map {
                Log.d(TAG, "Shizuku touch tap injected at ($pixelX, $pixelY)")
                "Injeksi sentuhan sukses di ($pixelX, $pixelY)"
            }
        } else {
            Result.failure(
                IllegalStateException(
                    "Touch injection lintas aplikasi memerlukan izin Shizuku (ADB) aktif atau perangkat konverter hardware terhubung."
                )
            )
        }
    }

    suspend fun injectSwipe(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        screenWidthPx: Int,
        screenHeightPx: Int,
        durationMs: Long = 150
    ): Result<String> = withContext(Dispatchers.IO) {
        val px1 = (x1 * screenWidthPx).toInt().coerceIn(0, screenWidthPx)
        val py1 = (y1 * screenHeightPx).toInt().coerceIn(0, screenHeightPx)
        val px2 = (x2 * screenWidthPx).toInt().coerceIn(0, screenWidthPx)
        val py2 = (y2 * screenHeightPx).toInt().coerceIn(0, screenHeightPx)

        if (shizukuManager.isAuthorized()) {
            val cmd = "input swipe $px1 $py1 $px2 $py2 $durationMs"
            shizukuManager.executeSafeCommand(cmd)
        } else {
            Result.failure(
                IllegalStateException("Privileged swipe memerlukan Shizuku.")
            )
        }
    }
}
