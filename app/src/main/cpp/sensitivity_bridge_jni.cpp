#include <jni.h>
#include <string>
#include <memory>
#include "sensitivity_engine.h"

static std::unique_ptr<paseosensi::SensitivityEngine> g_engine = nullptr;

extern "C" {

JNIEXPORT jstring JNICALL
Java_com_example_nativecore_SensitivityBridge_nativeGetEngineVersion(
    JNIEnv* env,
    jobject /* this */
) {
    return env->NewStringUTF(paseosensi::SensitivityEngine::getVersion());
}

JNIEXPORT jfloatArray JNICALL
Java_com_example_nativecore_SensitivityBridge_nativeCalculateTransformedPoint(
    JNIEnv* env,
    jobject /* this */,
    jfloat rawDeltaX,
    jfloat rawDeltaY,
    jfloat sensiMultiplierX,
    jfloat sensiMultiplierY,
    jint curveTypeInt,
    jfloat smoothingFactor,
    jfloat deadzoneThreshold,
    jfloat accelerationFactor,
    jfloat deltaTimeMs
) {
    if (!g_engine) {
        g_engine = std::make_unique<paseosensi::SensitivityEngine>();
    }

    paseosensi::CurveType curve = static_cast<paseosensi::CurveType>(curveTypeInt);
    paseosensi::TransformedPoint result = g_engine->calculateTransformedPoint(
        rawDeltaX,
        rawDeltaY,
        sensiMultiplierX,
        sensiMultiplierY,
        curve,
        smoothingFactor,
        deadzoneThreshold,
        accelerationFactor,
        deltaTimeMs
    );

    jfloatArray outputArray = env->NewFloatArray(5);
    if (!outputArray) {
        return nullptr;
    }

    jfloat outBuf[5] = {
        result.x,
        result.y,
        result.velocity,
        result.appliedMultiplierX,
        result.appliedMultiplierY
    };

    env->SetFloatArrayRegion(outputArray, 0, 5, outBuf);
    return outputArray;
}

JNIEXPORT jfloat JNICALL
Java_com_example_nativecore_SensitivityBridge_nativeCalculateVelocity(
    JNIEnv* /* env */,
    jobject /* this */,
    jfloat deltaX,
    jfloat deltaY,
    jfloat deltaTimeMs
) {
    if (!g_engine) {
        g_engine = std::make_unique<paseosensi::SensitivityEngine>();
    }
    return g_engine->calculateVelocity(deltaX, deltaY, deltaTimeMs);
}

} // extern "C"
