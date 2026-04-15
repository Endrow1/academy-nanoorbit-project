package com.example.myapplication.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface MissionDao {

    @Query("SELECT * FROM missions ORDER BY dateDebut DESC")
    fun observeAll(): Flow<List<MissionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<MissionEntity>)

    @Query("DELETE FROM missions")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM missions")
    suspend fun count(): Int

    @Query("SELECT MAX(cachedAt) FROM missions")
    suspend fun getLastCachedAt(): LocalDateTime?
}