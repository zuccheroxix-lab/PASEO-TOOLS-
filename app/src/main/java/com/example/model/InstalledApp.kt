package com.example.model

import android.graphics.drawable.Drawable

data class InstalledApp(
    val packageName: String,
    val appName: String,
    val isGame: Boolean,
    val versionName: String,
    val icon: Drawable? = null
)
