/**
 * PASEO SENSI — Native C++ Sensitivity Engine
 * Developer: ZUCCHERO XANN
 * Project: PASEO-TOOLS-
 *
 * High-performance, low-latency mathematical transformation core for touch
 * input vectors, X/Y axes calibration, smoothing filters, and acceleration curves.
 * Decoupled from Android SDK for cross-platform and desktop compatibility.
 */

#ifndef PASEO_SENSI_SENSITIVITY_ENGINE_H
#define PASEO_SENSI_SENSITIVITY_ENGINE_H

#include <cstdint>
#include <cmath>
#include <algorithm>

namespace paseo {

enum CurveType : int32_t {
    CURVE_LINEAR = 0,
    CURVE_DYNAMIC_S = 1,
    CURVE_EXPONENTIAL = 2,
    CURVE_CONTROL_SMOOTH = 3
};

struct EngineConfig {
    float sensiX = 1.0f;
    float sensiY = 1.0f;
    float deadzone = 0.02f;
    float smoothing = 0.10f;
    float acceleration = 1.05f;
    int32_t curveType = CURVE_DYNAMIC_S;
};

struct Vector2D {
    float x = 0.0f;
    float y = 0.0f;

    Vector2D() : x(0.0f), y(0.0f) {}
    Vector2D(float _x, float _y) : x(_x), y(_y) {}

    float magnitude() const {
        return std::sqrt(x * x + y * y);
    }
};

class SensitivityEngine {
public:
    SensitivityEngine();
    explicit SensitivityEngine(const EngineConfig& config);
    ~SensitivityEngine() = default;

    void setConfig(const EngineConfig& config);
    const EngineConfig& getConfig() const;

    // Transforms input delta displacement (rawDx, rawDy) given elapsed time dtSec
    Vector2D processDelta(float rawDx, float rawDy, float dtSec);

    // Resets internal smoothing history and velocity state
    void reset();

    // Pure math helpers for unit testing & desktop harness
    static float applyDeadzone(float value, float deadzoneThreshold);
    static float applyCurve(float normalizedVelocity, int32_t curveType, float accelFactor);

private:
    EngineConfig mConfig;
    float mPrevFilteredDx;
    float mPrevFilteredDy;
    bool mHasPreviousSample;
};

} // namespace paseo

#endif // PASEO_SENSI_SENSITIVITY_ENGINE_H
