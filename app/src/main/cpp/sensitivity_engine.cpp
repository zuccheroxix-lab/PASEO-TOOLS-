/**
 * PASEO SENSI — Native C++ Sensitivity Engine Implementation
 * Developer: ZUCCHERO XANN
 * Project: PASEO-TOOLS-
 */

#include "sensitivity_engine.h"

namespace paseo {

SensitivityEngine::SensitivityEngine()
    : mConfig(), mPrevFilteredDx(0.0f), mPrevFilteredDy(0.0f), mHasPreviousSample(false) {}

SensitivityEngine::SensitivityEngine(const EngineConfig& config)
    : mConfig(config), mPrevFilteredDx(0.0f), mPrevFilteredDy(0.0f), mHasPreviousSample(false) {}

void SensitivityEngine::setConfig(const EngineConfig& config) {
    mConfig = config;
    // Bounds clamping
    if (mConfig.sensiX < 0.1f) mConfig.sensiX = 0.1f;
    if (mConfig.sensiX > 5.0f) mConfig.sensiX = 5.0f;
    if (mConfig.sensiY < 0.1f) mConfig.sensiY = 0.1f;
    if (mConfig.sensiY > 5.0f) mConfig.sensiY = 5.0f;
    if (mConfig.deadzone < 0.0f) mConfig.deadzone = 0.0f;
    if (mConfig.deadzone > 0.5f) mConfig.deadzone = 0.5f;
    if (mConfig.smoothing < 0.0f) mConfig.smoothing = 0.0f;
    if (mConfig.smoothing > 0.9f) mConfig.smoothing = 0.9f;
    if (mConfig.acceleration < 0.5f) mConfig.acceleration = 0.5f;
    if (mConfig.acceleration > 3.0f) mConfig.acceleration = 3.0f;
}

const EngineConfig& SensitivityEngine::getConfig() const {
    return mConfig;
}

void SensitivityEngine::reset() {
    mPrevFilteredDx = 0.0f;
    mPrevFilteredDy = 0.0f;
    mHasPreviousSample = false;
}

float SensitivityEngine::applyDeadzone(float value, float deadzoneThreshold) {
    float absVal = std::abs(value);
    if (absVal <= deadzoneThreshold) {
        return 0.0f;
    }
    // Continuous slope after deadzone to prevent discontinuity jump
    float sign = (value > 0.0f) ? 1.0f : -1.0f;
    return sign * (absVal - deadzoneThreshold) / (1.0f - deadzoneThreshold);
}

float SensitivityEngine::applyCurve(float normalizedSpeed, int32_t curveType, float accelFactor) {
    if (normalizedSpeed <= 0.0f) return 0.0f;

    switch (curveType) {
        case CURVE_LINEAR:
            return normalizedSpeed;

        case CURVE_DYNAMIC_S: {
            // S-Curve: smooth ramp at low velocity (fine aiming), accelerating at medium-high flick velocity
            // Normalized around pivot 1.0
            float sigmoid = 1.0f / (1.0f + std::exp(-3.0f * (normalizedSpeed - 1.0f)));
            float factor = 0.8f + (1.2f * sigmoid * accelFactor);
            return normalizedSpeed * factor;
        }

        case CURVE_EXPONENTIAL: {
            // Power curve: speed ^ (1.0 + (accel - 1.0) * 0.5)
            float exponent = 1.0f + ((accelFactor - 1.0f) * 0.5f);
            if (exponent < 0.8f) exponent = 0.8f;
            return std::pow(normalizedSpeed, exponent);
        }

        case CURVE_CONTROL_SMOOTH: {
            // Dampened response for high stability
            return normalizedSpeed * (1.0f / (1.0f + 0.1f * normalizedSpeed));
        }

        default:
            return normalizedSpeed;
    }
}

Vector2D SensitivityEngine::processDelta(float rawDx, float rawDy, float dtSec) {
    if (dtSec <= 0.0001f) {
        dtSec = 0.0166f; // Default ~60Hz delta time
    }

    // 1. Deadzone Filtering
    float dzX = applyDeadzone(rawDx, mConfig.deadzone * 10.0f);
    float dzY = applyDeadzone(rawDy, mConfig.deadzone * 10.0f);

    // 2. Exponential Moving Average Smoothing
    float filteredX = dzX;
    float filteredY = dzY;

    if (mHasPreviousSample && mConfig.smoothing > 0.0f) {
        float alpha = 1.0f - mConfig.smoothing;
        filteredX = alpha * dzX + mConfig.smoothing * mPrevFilteredDx;
        filteredY = alpha * dzY + mConfig.smoothing * mPrevFilteredDy;
    }

    mPrevFilteredDx = filteredX;
    mPrevFilteredDy = filteredY;
    mHasPreviousSample = true;

    // 3. Compute Velocity and Magnitude
    float speed = std::sqrt(filteredX * filteredX + filteredY * filteredY) / dtSec;
    // Typical reference flick velocity ~ 1000 px/s
    float normalizedSpeed = speed / 1000.0f;

    // 4. Apply Dynamic Response Curve
    float curveMultiplier = 1.0f;
    if (normalizedSpeed > 0.001f) {
        float curvedSpeed = applyCurve(normalizedSpeed, mConfig.curveType, mConfig.acceleration);
        curveMultiplier = curvedSpeed / normalizedSpeed;
    }

    // 5. Apply Independent X / Y Multipliers and Output Vector
    float finalDx = filteredX * mConfig.sensiX * curveMultiplier;
    float finalDy = filteredY * mConfig.sensiY * curveMultiplier;

    return Vector2D(finalDx, finalDy);
}

} // namespace paseo
