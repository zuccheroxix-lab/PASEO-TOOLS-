package com.example.nativecore

import com.example.model.SensitivityCurveType
import kotlin.math.ln1p
import kotlin.math.pow
import kotlin.math.sqrt

data class TransformedResult(
    val x: Float,
    val y: Float,
    val velocity: Float,
    val appliedMultiplierX: Float,
    val appliedMultiplierY: Float,
    val isNativeEngine: Boolean
)

object SensitivityBridge {

    private var isNativeLoaded: Boolean = false

    init {
        try {
            System.loadLibrary("paseosensi_native")
            isNativeLoaded = true
        } catch (t: Throwable) {
            isNativeLoaded = false
        }
    }

    fun isNativeModuleActive(): Boolean = isNativeLoaded

    fun getEngineVersion(): String {
        return if (isNativeLoaded) {
            try {
                nativeGetEngineVersion()
            } catch (e: UnsatisfiedLinkError) {
                "PASEO-SENSI-CORE-v2.0-KOTLIN-FALLBACK"
            }
        } else {
            "PASEO-SENSI-CORE-v2.0-KOTLIN-ALGORITHMIC"
        }
    }

    /**
     * Transforms an input coordinate / swipe vector using X/Y sensitivity, curve algorithm,
     * deadzone threshold, smoothing, and velocity acceleration.
     */
    fun calculateTransformedPoint(
        rawDeltaX: Float,
        rawDeltaY: Float,
        sensiMultiplierX: Float,
        sensiMultiplierY: Float,
        curveType: SensitivityCurveType,
        smoothingFactor: Float = 0.10f,
        deadzoneThreshold: Float = 0.02f,
        accelerationFactor: Float = 1.05f,
        deltaTimeMs: Float = 16.6f
    ): TransformedResult {
        if (isNativeLoaded) {
            try {
                val out = nativeCalculateTransformedPoint(
                    rawDeltaX,
                    rawDeltaY,
                    sensiMultiplierX,
                    sensiMultiplierY,
                    curveType.ordinal,
                    smoothingFactor,
                    deadzoneThreshold,
                    accelerationFactor,
                    deltaTimeMs
                )
                if (out.size >= 5) {
                    return TransformedResult(
                        x = out[0],
                        y = out[1],
                        velocity = out[2],
                        appliedMultiplierX = out[3],
                        appliedMultiplierY = out[4],
                        isNativeEngine = true
                    )
                }
            } catch (e: UnsatisfiedLinkError) {
                // Fall back to Kotlin implementation below
            }
        }

        // Pure Kotlin implementation with exact mathematical parity with C++ core
        return calculateTransformedPointInternal(
            rawDeltaX,
            rawDeltaY,
            sensiMultiplierX,
            sensiMultiplierY,
            curveType,
            smoothingFactor,
            deadzoneThreshold,
            accelerationFactor,
            deltaTimeMs
        )
    }

    fun calculateVelocity(deltaX: Float, deltaY: Float, deltaTimeMs: Float): Float {
        if (deltaTimeMs <= 0.001f) return 0.0f
        val distance = sqrt(deltaX * deltaX + deltaY * deltaY)
        return distance / deltaTimeMs
    }

    // --- Mathematical Parity Engine ---
    private var prevFilteredX = 0.0f
    private var prevFilteredY = 0.0f

    private fun calculateTransformedPointInternal(
        rawDeltaX: Float,
        rawDeltaY: Float,
        sensiMultiplierX: Float,
        sensiMultiplierY: Float,
        curveType: SensitivityCurveType,
        smoothingFactor: Float,
        deadzoneThreshold: Float,
        accelerationFactor: Float,
        deltaTimeMs: Float
    ): TransformedResult {
        val rawMagnitude = sqrt(rawDeltaX * rawDeltaX + rawDeltaY * rawDeltaY)

        if (rawMagnitude < deadzoneThreshold) {
            return TransformedResult(0.0f, 0.0f, 0.0f, sensiMultiplierX, sensiMultiplierY, false)
        }

        val dirX = if (rawMagnitude > 0.0001f) rawDeltaX / rawMagnitude else 0.0f
        val dirY = if (rawMagnitude > 0.0001f) rawDeltaY / rawMagnitude else 0.0f

        val activeMagnitude = (rawMagnitude - deadzoneThreshold) / (1.0f - deadzoneThreshold)
        val clampedActive = activeMagnitude.coerceAtLeast(0.0f)

        val curvedMagnitude = evaluateCurve(clampedActive, curveType)
        val velocity = calculateVelocity(rawDeltaX, rawDeltaY, deltaTimeMs)

        var dynamicAccel = 1.0f
        if (accelerationFactor > 1.0f && velocity > 0.5f) {
            val velocityRatio = ((velocity - 0.5f) / 4.0f).coerceIn(0.0f, 1.0f)
            dynamicAccel = 1.0f + ((accelerationFactor - 1.0f) * velocityRatio)
        }

        val targetX = dirX * curvedMagnitude * sensiMultiplierX * dynamicAccel
        val targetY = dirY * curvedMagnitude * sensiMultiplierY * dynamicAccel

        val alpha = (1.0f - smoothingFactor).coerceIn(0.05f, 1.0f)
        val filteredX = (alpha * targetX) + ((1.0f - alpha) * prevFilteredX)
        val filteredY = (alpha * targetY) + ((1.0f - alpha) * prevFilteredY)

        prevFilteredX = filteredX
        prevFilteredY = filteredY

        return TransformedResult(
            x = filteredX,
            y = filteredY,
            velocity = velocity,
            appliedMultiplierX = sensiMultiplierX * dynamicAccel,
            appliedMultiplierY = sensiMultiplierY * dynamicAccel,
            isNativeEngine = false
        )
    }

    private fun evaluateCurve(magnitude: Float, curveType: SensitivityCurveType): Float {
        if (magnitude <= 0.0001f) return 0.0f
        return when (curveType) {
            SensitivityCurveType.LINEAR -> magnitude
            SensitivityCurveType.DYNAMIC -> {
                val norm = magnitude.coerceIn(0.0f, 2.5f)
                val sCurve = (norm * norm) / (0.35f + norm * norm)
                (norm * 0.4f) + (sCurve * 0.9f)
            }
            SensitivityCurveType.EXPONENTIAL -> {
                val norm = magnitude.coerceIn(0.0f, 3.0f)
                norm.toDouble().pow(1.45).toFloat()
            }
            SensitivityCurveType.CONTROL_SMOOTH -> {
                val norm = magnitude.coerceIn(0.0f, 2.5f)
                (ln1p((norm * 1.5).toDouble()) * 0.95).toFloat()
            }
        }
    }

    // JNI Native Declarations
    private external fun nativeGetEngineVersion(): String

    private external fun nativeCalculateTransformedPoint(
        rawDeltaX: Float,
        rawDeltaY: Float,
        sensiMultiplierX: Float,
        sensiMultiplierY: Float,
        curveTypeInt: Int,
        smoothingFactor: Float,
        deadzoneThreshold: Float,
        accelerationFactor: Float,
        deltaTimeMs: Float
    ): FloatArray

    private external fun nativeCalculateVelocity(
        deltaX: Float,
        deltaY: Float,
        deltaTimeMs: Float
    ): Float
}
