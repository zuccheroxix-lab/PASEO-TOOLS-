package com.example.model

data class DeviceInfo(
    val androidVersion: String,
    val apiLevel: Int,
    val manufacturer: String,
    val model: String,
    val deviceName: String,
    val primaryAbi: String,
    val supportedAbis: List<String>,
    val cpuCores: Int,
    val totalRamBytes: Long,
    val availableRamBytes: Long,
    val batteryLevel: Int,
    val batteryTemperatureCelsius: Float,
    val batteryVoltageMv: Int,
    val isCharging: Boolean,
    val totalStorageBytes: Long,
    val freeStorageBytes: Long,
    val screenWidthPx: Int,
    val screenHeightPx: Int,
    val screenDensityDpi: Int,
    val refreshRateHz: Float
)
