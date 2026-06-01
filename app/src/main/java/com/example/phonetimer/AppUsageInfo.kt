package com.example.phonetimer

import android.graphics.drawable.Drawable

data class AppUsageInfo(
    val packageName: String,
    val appName: String,
    val appIcon: Drawable?,
    val usageTimeMillis: Long,
    val idleTimeMillis: Long
)
