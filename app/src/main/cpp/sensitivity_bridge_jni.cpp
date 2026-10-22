/**
 * PASEO SENSI — JNI Bridge to C++ Sensitivity Engine
 * Developer: ZUCCHERO XANN
 * Project: PASEO-TOOLS-
 */

#include <jni.h>
#include "sensitivity_engine.h"

using namespace paseo;

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_example_nativecore_SensitivityBridge_createEngineNative(
        JNIEnv* env,
        jobject thiz,
        jfloat sensiX,
        jfloat sensiY,
        jfloat deadzone,
        jfloat smoothing,
        jfloat acceleration,
        jint curveType) {
    EngineConfig cfg;
    cfg.sensiX = sensiX;
    cfg.sensiY = sensiY;
    cfg.deadzone = deadzone;
    cfg.smoothing = smoothing;
    cfg.acceleration = acceleration;
    cfg.curveType = curveType;

    auto* engine = new SensitivityEngine(cfg);
    return reinterpret_cast<jlong>(engine);
}

JNIEXPORT void JNICALL
Java_com_example_nativecore_SensitivityBridge_updateConfigNative(
        JNIEnv* env,
        jobject thiz,
        jlong enginePtr,
        jfloat sensiX,
        jfloat sensiY,
        jfloat deadzone,
        jfloat smoothing,
        jfloat acceleration,
        jint curveType) {
    if (enginePtr == 0) return;
    auto* engine = reinterpret_cast<SensitivityEngine*>(enginePtr);
    EngineConfig cfg;
    cfg.sensiX = sensiX;
    cfg.sensiY = sensiY;
    cfg.deadzone = deadzone;
    cfg.smoothing = smoothing;
    cfg.acceleration = acceleration;
    cfg.curveType = curveType;
    engine->setConfig(cfg);
}

JNIEXPORT jfloatArray JNICALL
Java_com_example_nativecore_SensitivityBridge_processDeltaNative(
        JNIEnv* env,
        jobject thiz,
        jlong enginePtr,
        jfloat rawDx,
        jfloat rawDy,
        jfloat dtSec) {
    jfloatArray result = env->NewFloatArray(2);
    if (result == nullptr) return nullptr;

    if (enginePtr == 0) {
        jfloat defaultVals[2] = { rawDx, rawDy };
        env->SetFloatArrayRegion(result, 0, 2, defaultVals);
        return result;
    }

    auto* engine = reinterpret_cast<SensitivityEngine*>(enginePtr);
    Vector2D transformed = engine->processDelta(rawDx, rawDy, dtSec);

    jfloat outputVals[2] = { transformed.x, transformed.y };
    env->SetFloatArrayRegion(result, 0, 2, outputVals);
    return result;
}

JNIEXPORT void JNICALL
Java_com_example_nativecore_SensitivityBridge_resetNative(
        JNIEnv* env,
        jobject thiz,
        jlong enginePtr) {
    if (enginePtr == 0) return;
    auto* engine = reinterpret_cast<SensitivityEngine*>(enginePtr);
    engine->reset();
}

JNIEXPORT void JNICALL
Java_com_example_nativecore_SensitivityBridge_destroyEngineNative(
        JNIEnv* env,
        jobject thiz,
        jlong enginePtr) {
    if (enginePtr == 0) return;
    auto* engine = reinterpret_cast<SensitivityEngine*>(enginePtr);
    delete engine;
}

} // extern "C"
