package com.example.phonetimer.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AppUsageDao {
    @Query("SELECT * FROM app_usage")
    suspend fun getAll(): List<AppUsageEntity>

    @Query("SELECT * FROM app_usage WHERE packageName = :packageName")
    suspend fun getByPackageName(packageName: String): AppUsageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: AppUsageEntity)

    @Query("DELETE FROM app_usage WHERE packageName = :packageName")
    suspend fun deleteByPackageName(packageName: String)
}
