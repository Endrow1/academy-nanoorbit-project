package com.example.myapplication.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface StationDao {

    @Query("SELECT * FROM stations ORDER BY nomStation ASC")
    fun observeAll(): Flow<List<StationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<StationEntity>)

    @Query("DELETE FROM stations")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM stations")
    suspend fun count(): Int

    @Query("SELECT MAX(cachedAt) FROM stations")
    suspend fun getLastCachedAt(): LocalDateTime?
}