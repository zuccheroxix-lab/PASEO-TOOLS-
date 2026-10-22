#ifndef PASEO_SENSI_ENGINE_H
#define PASEO_SENSI_ENGINE_H

#include <cmath>
#include <string>
#include <algorithm>

namespace paseosensi {

enum class CurveType {
    LINEAR = 0,
    DYNAMIC = 1,
    EXPONENTIAL = 2,
    CONTROL_SMOOTH = 3
};

struct TransformedPoint {
    float x;
    float y;
    float velocity;
    float appliedMultiplierX;
    float appliedMultiplierY;
};

class SensitivityEngine {
public:
    SensitivityEngine();
    ~SensitivityEngine() = default;

    /**
     * Transforms an input displacement or coordinate point using X/Y sensitivity multipliers,
     * response curve, deadzone threshold, smoothing filter, and velocity acceleration.
     * Pure, portable C++17 implementation without any OS-specific dependencies.
     */
    TransformedPoint calculateTransformedPoint(
        float rawDeltaX,
        float rawDeltaY,
        float sensiMultiplierX,
        float sensiMultiplierY,
        CurveType curveType,
        float smoothingFactor,
        float deadzoneThreshold,
        float accelerationFactor,
        float deltaTimeMs
    );

    /**
     * Calculates input velocity in pixels/ms or normalized units.
     */
    float calculateVelocity(float deltaX, float deltaY, float deltaTimeMs);

    /**
     * Applies response curve to a normalized magnitude [0.0, 1.0+].
     */
    float evaluateCurve(float magnitude, CurveType curveType);

    /**
     * Returns the engine core version identifier string.
     */
    static const char* getVersion();

private:
    float prevFilteredX_;
    float prevFilteredY_;
    float prevVelocity_;
};

} // namespace paseosensi

#endif // PASEO_SENSI_ENGINE_H
