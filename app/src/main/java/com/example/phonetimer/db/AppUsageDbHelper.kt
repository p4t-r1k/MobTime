package com.example.phonetimer.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.phonetimer.db.AppUsageContract.AppUsageEntry

class AppUsageDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val SQL_CREATE_ENTRIES = "CREATE TABLE ${AppUsageEntry.TABLE_NAME} (" +
                "${AppUsageEntry.COLUMN_PACKAGE_NAME} TEXT PRIMARY KEY," +
                "${AppUsageEntry.COLUMN_APP_NAME} TEXT," +
                "${AppUsageEntry.COLUMN_USAGE_TIME} INTEGER," +
                "${AppUsageEntry.COLUMN_IDLE_TIME} INTEGER," +
                "${AppUsageEntry.COLUMN_LAST_UPDATED} INTEGER)"
        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS ${AppUsageEntry.TABLE_NAME}")
        onCreate(db)
    }

    fun insertOrUpdate(packageName: String, appName: String, usageTime: Long, idleTime: Long, lastUpdated: Long) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(AppUsageEntry.COLUMN_PACKAGE_NAME, packageName)
            put(AppUsageEntry.COLUMN_APP_NAME, appName)
            put(AppUsageEntry.COLUMN_USAGE_TIME, usageTime)
            put(AppUsageEntry.COLUMN_IDLE_TIME, idleTime)
            put(AppUsageEntry.COLUMN_LAST_UPDATED, lastUpdated)
        }
        db.insertWithOnConflict(
            AppUsageEntry.TABLE_NAME,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun getAllUsage(): Map<String, HistoricalUsage> {
        val db = readableDatabase
        val projection = arrayOf(
            AppUsageEntry.COLUMN_PACKAGE_NAME,
            AppUsageEntry.COLUMN_APP_NAME,
            AppUsageEntry.COLUMN_USAGE_TIME,
            AppUsageEntry.COLUMN_IDLE_TIME
        )

        val cursor = db.query(
            AppUsageEntry.TABLE_NAME,
            projection,
            null, null, null, null, null
        )

        val items = mutableMapOf<String, HistoricalUsage>()
        with(cursor) {
            while (moveToNext()) {
                val pkg = getString(getColumnIndexOrThrow(AppUsageEntry.COLUMN_PACKAGE_NAME))
                val name = getString(getColumnIndexOrThrow(AppUsageEntry.COLUMN_APP_NAME))
                val usage = getLong(getColumnIndexOrThrow(AppUsageEntry.COLUMN_USAGE_TIME))
                val idle = getLong(getColumnIndexOrThrow(AppUsageEntry.COLUMN_IDLE_TIME))
                items[pkg] = HistoricalUsage(pkg, name, usage, idle)
            }
        }
        cursor.close()
        return items
    }

    data class HistoricalUsage(
        val packageName: String,
        val appName: String,
        val usageTimeMillis: Long,
        val idleTimeMillis: Long
    )

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "AppUsage.db"
    }
}
