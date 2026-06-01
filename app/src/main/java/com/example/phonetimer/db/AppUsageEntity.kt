package com.example.phonetimer.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_usage")
data class AppUsageEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val usageTimeMillis: Long,
    val idleTimeMillis: Long,
    val lastUpdated: Long
)
