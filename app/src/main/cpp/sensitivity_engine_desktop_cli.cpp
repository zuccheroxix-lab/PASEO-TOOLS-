/**
 * PASEO SENSI — Desktop CLI & Mathematics Verification Harness
 * Developer: ZUCCHERO XANN
 * Project: PASEO-TOOLS-
 *
 * Standalone executable to verify C++ calculations, deadzone thresholds,
 * curve transformations, and benchmark latency on desktop environments.
 */

#include <iostream>
#include <iomanip>
#include <vector>
#include <cassert>
#include "sensitivity_engine.h"

using namespace paseo;

void runTests() {
    std::cout << "============================================" << std::endl;
    std::cout << "  PASEO SENSI — Native C++ Core Unit Tests  " << std::endl;
    std::cout << "  Developer: ZUCCHERO XANN                  " << std::endl;
    std::cout << "============================================" << std::endl;

    // Test 1: Deadzone clamping
    {
        float dz = 0.05f;
        float smallInput = 0.03f;
        float filtered = SensitivityEngine::applyDeadzone(smallInput, dz);
        assert(filtered == 0.0f);
        std::cout << "[PASS] Deadzone filter suppressed sub-threshold micro-movement.\n";

        float largeInput = 0.20f;
        float filteredLarge = SensitivityEngine::applyDeadzone(largeInput, dz);
        assert(filteredLarge > 0.0f && filteredLarge <= largeInput);
        std::cout << "[PASS] Deadzone continuous slope verified: " << filteredLarge << "\n";
    }

    // Test 2: Curves verification
    {
        float speed = 1.2f;
        float linear = SensitivityEngine::applyCurve(speed, CURVE_LINEAR, 1.0f);
        assert(std::abs(linear - 1.2f) < 0.001f);
        std::cout << "[PASS] Linear response curve parity verified.\n";

        float sCurve = SensitivityEngine::applyCurve(speed, CURVE_DYNAMIC_S, 1.2f);
        assert(sCurve > 0.0f);
        std::cout << "[PASS] Dynamic S-Curve computed: " << sCurve << "\n";

        float exponential = SensitivityEngine::applyCurve(speed, CURVE_EXPONENTIAL, 1.3f);
        assert(exponential > 0.0f);
        std::cout << "[PASS] Exponential curve computed: " << exponential << "\n";
    }

    // Test 3: Engine stateful processing
    {
        EngineConfig cfg;
        cfg.sensiX = 1.30f;
        cfg.sensiY = 1.10f;
        cfg.deadzone = 0.02f;
        cfg.smoothing = 0.15f;
        cfg.acceleration = 1.10f;
        cfg.curveType = CURVE_DYNAMIC_S;

        SensitivityEngine engine(cfg);
        Vector2D res1 = engine.processDelta(10.0f, 5.0f, 0.016f);
        assert(res1.x > 0.0f);
        assert(res1.y > 0.0f);
        // sensiX is higher than sensiY, so transformed X/Y ratio should reflect it
        assert((res1.x / 10.0f) > (res1.y / 5.0f));
        std::cout << "[PASS] Asymmetric X/Y multiplier scaling confirmed.\n";
        std::cout << "       Raw (10.0, 5.0) -> Transformed (" << res1.x << ", " << res1.y << ")\n";
    }

    std::cout << "\nAll native C++ tests passed successfully.\n";
}

int main(int argc, char** argv) {
    runTests();
    return 0;
}
