package com.example.myapplication.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface SatelliteDao {

    @Query("SELECT * FROM satellites ORDER BY nomSatellite ASC")
    fun observeAll(): Flow<List<SatelliteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<SatelliteEntity>)

    @Query("DELETE FROM satellites")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM satellites")
    suspend fun count(): Int

    @Query("SELECT MAX(cachedAt) FROM satellites")
    suspend fun getLastCachedAt(): LocalDateTime?
}