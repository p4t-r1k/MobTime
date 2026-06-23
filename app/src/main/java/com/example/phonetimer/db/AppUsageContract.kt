package com.example.phonetimer.db

import android.provider.BaseColumns

object AppUsageContract {
    object AppUsageEntry : BaseColumns {
        const val TABLE_NAME = "app_usage"
        const val COLUMN_PACKAGE_NAME = "packageName"
        const val COLUMN_APP_NAME = "appName"
        const val COLUMN_USAGE_TIME = "usageTimeMillis"
        const val COLUMN_IDLE_TIME = "idleTimeMillis"
        const val COLUMN_LAST_UPDATED = "lastUpdated"
    }
}
