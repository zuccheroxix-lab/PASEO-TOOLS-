#include "sensitivity_engine.h"

namespace paseosensi {

static constexpr const char* ENGINE_VERSION = "PASEO-SENSI-CORE-v2.0-NATIVE";

SensitivityEngine::SensitivityEngine()
    : prevFilteredX_(0.0f),
      prevFilteredY_(0.0f),
      prevVelocity_(0.0f) {}

float SensitivityEngine::calculateVelocity(float deltaX, float deltaY, float deltaTimeMs) {
    if (deltaTimeMs <= 0.001f) {
        return 0.0f;
    }
    float distance = std::sqrt(deltaX * deltaX + deltaY * deltaY);
    return distance / deltaTimeMs;
}

float SensitivityEngine::evaluateCurve(float magnitude, CurveType curveType) {
    if (magnitude <= 0.0001f) {
        return 0.0f;
    }

    switch (curveType) {
        case CurveType::LINEAR:
            return magnitude;

        case CurveType::DYNAMIC: {
            // S-Curve: smooth ramp up from fine aiming to rapid swipe
            // Uses algebraic sigmoidal transition: f(x) = (x^2) / (0.35 + x^2) scaled
            float normalized = std::clamp(magnitude, 0.0f, 2.5f);
            float sCurve = (normalized * normalized) / (0.35f + normalized * normalized);
            return (normalized * 0.4f) + (sCurve * 0.9f);
        }

        case CurveType::EXPONENTIAL: {
            // Power curve: high precision for low deflections, parabolic growth for rapid turns
            float normalized = std::clamp(magnitude, 0.0f, 3.0f);
            return std::pow(normalized, 1.45f);
        }

        case CurveType::CONTROL_SMOOTH: {
            // Damped curve to stabilize recoil and hand tremors
            float normalized = std::clamp(magnitude, 0.0f, 2.5f);
            return std::log1p(normalized * 1.5f) * 0.95f;
        }

        default:
            return magnitude;
    }
}

TransformedPoint SensitivityEngine::calculateTransformedPoint(
    float rawDeltaX,
    float rawDeltaY,
    float sensiMultiplierX,
    float sensiMultiplierY,
    CurveType curveType,
    float smoothingFactor,
    float deadzoneThreshold,
    float accelerationFactor,
    float deltaTimeMs
) {
    // 1. Calculate raw input magnitude
    float rawMagnitude = std::sqrt(rawDeltaX * rawDeltaX + rawDeltaY * rawDeltaY);

    // 2. Deadzone filtering
    if (rawMagnitude < deadzoneThreshold) {
        return TransformedPoint{0.0f, 0.0f, 0.0f, sensiMultiplierX, sensiMultiplierY};
    }

    // Normalized direction vector
    float dirX = (rawMagnitude > 0.0001f) ? (rawDeltaX / rawMagnitude) : 0.0f;
    float dirY = (rawMagnitude > 0.0001f) ? (rawDeltaY / rawMagnitude) : 0.0f;

    // Rescale magnitude past deadzone for seamless continuity
    float activeMagnitude = (rawMagnitude - deadzoneThreshold) / (1.0f - deadzoneThreshold);
    if (activeMagnitude < 0.0f) activeMagnitude = 0.0f;

    // 3. Curve transformation
    float curvedMagnitude = evaluateCurve(activeMagnitude, curveType);

    // 4. Velocity and dynamic acceleration factor
    float velocity = calculateVelocity(rawDeltaX, rawDeltaY, deltaTimeMs);
    float dynamicAccel = 1.0f;
    if (accelerationFactor > 1.0f && velocity > 0.5f) {
        float velocityRatio = std::clamp((velocity - 0.5f) / 4.0f, 0.0f, 1.0f);
        dynamicAccel = 1.0f + ((accelerationFactor - 1.0f) * velocityRatio);
    }

    // 5. Apply separate X and Y sensitivity multipliers
    float targetX = dirX * curvedMagnitude * sensiMultiplierX * dynamicAccel;
    float targetY = dirY * curvedMagnitude * sensiMultiplierY * dynamicAccel;

    // 6. Exponential smoothing filter (Jitter reduction)
    float alpha = std::clamp(1.0f - smoothingFactor, 0.05f, 1.0f);
    float filteredX = (alpha * targetX) + ((1.0f - alpha) * prevFilteredX_);
    float filteredY = (alpha * targetY) + ((1.0f - alpha) * prevFilteredY_);

    prevFilteredX_ = filteredX;
    prevFilteredY_ = filteredY;
    prevVelocity_ = velocity;

    return TransformedPoint{
        filteredX,
        filteredY,
        velocity,
        sensiMultiplierX * dynamicAccel,
        sensiMultiplierY * dynamicAccel
    };
}

const char* SensitivityEngine::getVersion() {
    return ENGINE_VERSION;
}

} // namespace paseosensi
