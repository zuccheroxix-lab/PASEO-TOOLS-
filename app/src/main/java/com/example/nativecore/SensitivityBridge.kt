package com.example.nativecore

import android.util.Log
import com.example.model.SensitivityCurveType
import com.example.model.SensitivityProfile
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * PASEO SENSI — Native C++ JNI Bridge & Fallback Math Core
 * Developer: ZUCCHERO XANN
 *
 * Bridges Kotlin UI/Services to the native C++ SensitivityEngine.
 * Seamlessly provides 100% mathematical parity when running in unit tests (JVM/Robolectric)
 * or when the native .so library is not dynamically linked.
 */
class SensitivityBridge {

    companion object {
        private const val TAG = "SensitivityBridge"
        private var isNativeLibLoaded = false

        init {
            try {
                System.loadLibrary("sensitivity_engine")
                isNativeLibLoaded = true
                Log.i(TAG, "Native C++ sensitivity_engine loaded successfully.")
            } catch (e: Throwable) {
                isNativeLibLoaded = false
                Log.w(TAG, "Native library not loaded; using high-fidelity Kotlin math core: ${e.message}")
            }
        }

        fun isNativeAvailable(): Boolean = isNativeLibLoaded
    }

    private var nativeEnginePtr: Long = 0
    private var currentProfile: SensitivityProfile = SensitivityProfile.PRESETS.first()

    // Fallback math state (parity with C++ SensitivityEngine)
    private var prevFilteredDx: Float = 0.0f
    private var prevFilteredDy: Float = 0.0f
    private var hasPreviousSample: Boolean = false

    init {
        if (isNativeLibLoaded) {
            try {
                nativeEnginePtr = createEngineNative(
                    currentProfile.sensiX,
                    currentProfile.sensiY,
                    currentProfile.deadzone,
                    currentProfile.smoothing,
                    currentProfile.acceleration,
                    curveTypeToInt(currentProfile.curveType)
                )
            } catch (e: Throwable) {
                nativeEnginePtr = 0
            }
        }
    }

    private fun curveTypeToInt(type: SensitivityCurveType): Int {
        return when (type) {
            SensitivityCurveType.LINEAR -> 0
            SensitivityCurveType.DYNAMIC -> 1
            SensitivityCurveType.EXPONENTIAL -> 2
            SensitivityCurveType.CONTROL_SMOOTH -> 3
        }
    }

    fun setProfile(profile: SensitivityProfile) {
        this.currentProfile = profile
        if (isNativeLibLoaded && nativeEnginePtr != 0L) {
            try {
                updateConfigNative(
                    nativeEnginePtr,
                    profile.sensiX,
                    profile.sensiY,
                    profile.deadzone,
                    profile.smoothing,
                    profile.acceleration,
                    curveTypeToInt(profile.curveType)
                )
                return
            } catch (e: Throwable) {
                // Fallback to Kotlin engine below
            }
        }
    }

    fun processDelta(rawDx: Float, rawDy: Float, dtSec: Float = 0.0166f): Pair<Float, Float> {
        if (isNativeLibLoaded && nativeEnginePtr != 0L) {
            try {
                val result = processDeltaNative(nativeEnginePtr, rawDx, rawDy, dtSec)
                if (result != null && result.size >= 2) {
                    return Pair(result[0], result[1])
                }
            } catch (e: Throwable) {
                // Fall back
            }
        }
        return processDeltaKotlinFallback(rawDx, rawDy, dtSec)
    }

    fun reset() {
        if (isNativeLibLoaded && nativeEnginePtr != 0L) {
            try {
                resetNative(nativeEnginePtr)
            } catch (_: Throwable) {}
        }
        prevFilteredDx = 0.0f
        prevFilteredDy = 0.0f
        hasPreviousSample = false
    }

    fun destroy() {
        if (isNativeLibLoaded && nativeEnginePtr != 0L) {
            try {
                destroyEngineNative(nativeEnginePtr)
            } catch (_: Throwable) {}
            nativeEnginePtr = 0L
        }
    }

    // Pure Kotlin mathematical parity implementation of C++ SensitivityEngine
    private fun processDeltaKotlinFallback(rawDx: Float, rawDy: Float, dtSec: Float): Pair<Float, Float> {
        val dt = if (dtSec <= 0.0001f) 0.0166f else dtSec

        // 1. Deadzone filtering
        val dzX = applyDeadzone(rawDx, currentProfile.deadzone * 10.0f)
        val dzY = applyDeadzone(rawDy, currentProfile.deadzone * 10.0f)

        // 2. EMA Smoothing
        var filteredX = dzX
        var filteredY = dzY
        if (hasPreviousSample && currentProfile.smoothing > 0.0f) {
            val alpha = 1.0f - currentProfile.smoothing
            filteredX = alpha * dzX + currentProfile.smoothing * prevFilteredDx
            filteredY = alpha * dzY + currentProfile.smoothing * prevFilteredDy
        }
        prevFilteredDx = filteredX
        prevFilteredDy = filteredY
        hasPreviousSample = true

        // 3. Velocity
        val speed = sqrt(filteredX * filteredX + filteredY * filteredY) / dt
        val normalizedSpeed = speed / 1000.0f

        // 4. Dynamic Curve
        var curveMultiplier = 1.0f
        if (normalizedSpeed > 0.001f) {
            val curvedSpeed = applyCurve(normalizedSpeed, currentProfile.curveType, currentProfile.acceleration)
            curveMultiplier = curvedSpeed / normalizedSpeed
        }

        // 5. Asymmetric X/Y Multipliers
        val outX = filteredX * currentProfile.sensiX * curveMultiplier
        val outY = filteredY * currentProfile.sensiY * curveMultiplier

        return Pair(outX, outY)
    }

    private fun applyDeadzone(value: Float, deadzoneThreshold: Float): Float {
        val absVal = abs(value)
        if (absVal <= deadzoneThreshold) return 0.0f
        val sign = if (value > 0.0f) 1.0f else -1.0f
        return sign * (absVal - deadzoneThreshold) / (1.0f - deadzoneThreshold)
    }

    private fun applyCurve(normalizedSpeed: Float, curveType: SensitivityCurveType, accelFactor: Float): Float {
        if (normalizedSpeed <= 0.0f) return 0.0f
        return when (curveType) {
            SensitivityCurveType.LINEAR -> normalizedSpeed
            SensitivityCurveType.DYNAMIC -> {
                val sigmoid = 1.0f / (1.0f + exp(-3.0f * (normalizedSpeed - 1.0f)))
                val factor = 0.8f + (1.2f * sigmoid * accelFactor)
                normalizedSpeed * factor
            }
            SensitivityCurveType.EXPONENTIAL -> {
                val exponent = (1.0f + ((accelFactor - 1.0f) * 0.5f)).coerceAtLeast(0.8f)
                normalizedSpeed.toDouble().pow(exponent.toDouble()).toFloat()
            }
            SensitivityCurveType.CONTROL_SMOOTH -> {
                normalizedSpeed * (1.0f / (1.0f + 0.1f * normalizedSpeed))
            }
        }
    }

    // Native declarations
    private external fun createEngineNative(
        sensiX: Float,
        sensiY: Float,
        deadzone: Float,
        smoothing: Float,
        acceleration: Float,
        curveType: Int
    ): Long

    private external fun updateConfigNative(
        enginePtr: Long,
        sensiX: Float,
        sensiY: Float,
        deadzone: Float,
        smoothing: Float,
        acceleration: Float,
        curveType: Int
    )

    private external fun processDeltaNative(
        enginePtr: Long,
        rawDx: Float,
        rawDy: Float,
        dtSec: Float
    ): FloatArray?

    private external fun resetNative(enginePtr: Long)

    private external fun destroyEngineNative(enginePtr: Long)
}
