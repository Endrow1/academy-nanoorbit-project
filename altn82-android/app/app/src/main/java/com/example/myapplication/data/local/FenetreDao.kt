package com.example.myapplication.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface FenetreDao {

    @Query("SELECT * FROM fenetres_com ORDER BY datetimeDebut ASC")
    fun observeAll(): Flow<List<FenetreEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<FenetreEntity>)

    @Query("DELETE FROM fenetres_com")
    suspend fun clearAll()

    @Query("SELECT MAX(cachedAt) FROM fenetres_com")
    suspend fun getLastCachedAt(): LocalDateTime?
}