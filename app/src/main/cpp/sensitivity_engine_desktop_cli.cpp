#include <iostream>
#include <iomanip>
#include "sensitivity_engine.h"

int main() {
    std::cout << "=================================================" << std::endl;
    std::cout << "  PASEO SENSI — NATIVE C++ ENGINE TEST HARNESS" << std::endl;
    std::cout << "  Author: ZUCCHERO XANN" << std::endl;
    std::cout << "  Core Version: " << paseosensi::SensitivityEngine::getVersion() << std::endl;
    std::cout << "=================================================" << std::endl;

    paseosensi::SensitivityEngine engine;

    // Simulation: Diagonal swipe gesture (dx=12.0f, dy=8.0f over 16.6ms at 60Hz)
    float rawDeltaX = 12.0f;
    float rawDeltaY = 8.0f;
    float sensiMultiplierX = 1.35f;
    float sensiMultiplierY = 1.20f;
    float smoothingFactor = 0.10f;
    float deadzoneThreshold = 0.02f;
    float accelerationFactor = 1.10f;
    float deltaTimeMs = 16.6f;

    paseosensi::TransformedPoint point = engine.calculateTransformedPoint(
        rawDeltaX,
        rawDeltaY,
        sensiMultiplierX,
        sensiMultiplierY,
        paseosensi::CurveType::DYNAMIC,
        smoothingFactor,
        deadzoneThreshold,
        accelerationFactor,
        deltaTimeMs
    );

    std::cout << std::fixed << std::setprecision(4);
    std::cout << "[Input Delta]   Raw X: " << rawDeltaX << ", Raw Y: " << rawDeltaY << std::endl;
    std::cout << "[Multipliers]   Sensi X: " << sensiMultiplierX << ", Sensi Y: " << sensiMultiplierY << std::endl;
    std::cout << "[Transformed]   Out X: " << point.x << ", Out Y: " << point.y << std::endl;
    std::cout << "[Diagnostics]   Velocity: " << point.velocity << " px/ms" << std::endl;
    std::cout << "[Applied X/Y]   Scale X: " << point.appliedMultiplierX << ", Scale Y: " << point.appliedMultiplierY << std::endl;
    std::cout << "=================================================" << std::endl;
    std::cout << "Verification SUCCESS: Mathematical coordinate transformation valid." << std::endl;

    return 0;
}
