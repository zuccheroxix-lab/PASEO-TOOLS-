# PASEO SENSI — Native C++ Core Engine
**Developer:** ZUCCHERO XANN  
**Project:** PASEO-TOOLS-

## Architecture Overview
The C++ Native Engine (`sensitivity_engine.cpp`, `sensitivity_engine.h`) provides zero-overhead, low-latency mathematics for aiming transformations.

### Separation of Concerns
1. **`SensitivityProfile` (Kotlin)**: Data structure storing X/Y factors, presets, deadzones, and acceleration properties.
2. **`SensitivityEngine` (C++)**: Pure mathematical engine computing vector transformations, deadzones, smoothing filter (EMA), and non-linear response curves (Linear, Dynamic S-Curve, Exponential, Smooth Control).
3. **`SensitivityBridge` (JNI / Kotlin)**: JNI gateway connecting Kotlin components to the native C++ library with automated fallback parity when running on JVM/Robolectric test runners.
4. **`SensitivityRepository` (Kotlin)**: Persistent repository managing active and custom profile states.

### Standalone Desktop Compilation
To compile and execute the test harness on any Linux / macOS / Windows desktop:
```bash
g++ -std=c++17 -O3 sensitivity_engine.cpp sensitivity_engine_desktop_cli.cpp -o sensitivity_engine_cli
./sensitivity_engine_cli
```
