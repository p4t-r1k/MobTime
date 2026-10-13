package com.example.phonetimer.model

import android.graphics.drawable.Drawable
import java.util.concurrent.TimeUnit

data class AppTimerItem(
    val packageName: String,
    val appName: String,
    val appIcon: Drawable? = null,
    val usageTimeMillis: Long,
    val limitMinutes: Int? = null,
    val category: String = "General"
) {
    val usageMinutes: Int
        get() = TimeUnit.MILLISECONDS.toMinutes(usageTimeMillis).toInt()

    val isLimitSet: Boolean
        get() = limitMinutes != null && limitMinutes > 0

    val isLimitExceeded: Boolean
        get() = isLimitSet && usageMinutes >= (limitMinutes ?: 0)

    val remainingMinutes: Int
        get() = if (isLimitSet) maxOf(0, (limitMinutes ?: 0) - usageMinutes) else 0

    val progressRatio: Float
        get() = when {
            isLimitSet -> (usageMinutes.toFloat() / (limitMinutes ?: 1)).coerceIn(0f, 1f)
            else -> 0f
        }

    fun formattedUsageTime(): String = formatDuration(usageTimeMillis)

    fun formattedLimitTime(): String = when {
        limitMinutes == null || limitMinutes <= 0 -> "No limit"
        limitMinutes >= 60 -> "${limitMinutes / 60}h ${limitMinutes % 60}m".replace(" 0m", "")
        else -> "${limitMinutes}m"
    }

    companion object {
        fun formatDuration(millis: Long): String {
            val hours = TimeUnit.MILLISECONDS.toHours(millis)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
            val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
            return when {
                hours > 0 -> "${hours}h ${minutes}m"
                minutes > 0 -> "${minutes}m ${seconds}s"
                else -> "${seconds}s"
            }
        }
    }
}
