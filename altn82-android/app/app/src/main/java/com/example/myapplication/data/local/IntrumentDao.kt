package com.example.myapplication.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface InstrumentDao {

    @Query("SELECT * FROM instruments ORDER BY refInstrument ASC")
    fun observeAll(): Flow<List<InstrumentEntity>>

    @Query("SELECT * FROM instruments WHERE idSatellite = :satelliteId ORDER BY refInstrument ASC")
    fun observeBySatelliteId(satelliteId: Int): Flow<List<InstrumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<InstrumentEntity>)

    @Query("DELETE FROM instruments")
    suspend fun clearAll()

    @Query("DELETE FROM instruments WHERE idSatellite = :satelliteId")
    suspend fun deleteBySatelliteId(satelliteId: Int)

    @Query("SELECT COUNT(*) FROM instruments")
    suspend fun count(): Int

    @Query("SELECT MAX(cachedAt) FROM instruments")
    suspend fun getLastCachedAt(): LocalDateTime?
}