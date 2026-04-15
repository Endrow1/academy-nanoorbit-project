package com.example.myapplication.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface OrbiteDao {

    @Query("SELECT * FROM orbites ORDER BY idOrbite ASC")
    fun observeAll(): Flow<List<OrbiteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<OrbiteEntity>)

    @Query("DELETE FROM orbites")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM orbites")
    suspend fun count(): Int

    @Query("SELECT MAX(cachedAt) FROM orbites")
    suspend fun getLastCachedAt(): LocalDateTime?
}