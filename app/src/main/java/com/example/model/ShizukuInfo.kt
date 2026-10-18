package com.example.model

enum class ShizukuStatus {
    CONNECTED,
    DISCONNECTED,
    UNSUPPORTED
}

data class ShizukuInfo(
    val status: ShizukuStatus = ShizukuStatus.DISCONNECTED,
    val isInstalled: Boolean = false,
    val apiVersion: Int = 0,
    val patchVersion: Int = 0,
    val isPermissionGranted: Boolean = false,
    val statusMessage: String = "Memeriksa status Shizuku..."
)
