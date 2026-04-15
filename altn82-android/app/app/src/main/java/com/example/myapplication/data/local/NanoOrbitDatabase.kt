package com.example.myapplication.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        SatelliteEntity::class,
        FenetreEntity::class,
        OrbiteEntity::class,
        StationEntity::class,
        MissionEntity::class,
        InstrumentEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class NanoOrbitDatabase : RoomDatabase() {
    abstract fun satelliteDao(): SatelliteDao
    abstract fun fenetreDao(): FenetreDao
    abstract fun orbiteDao(): OrbiteDao
    abstract fun stationDao(): StationDao
    abstract fun missionDao(): MissionDao
    abstract fun instrumentDao(): InstrumentDao
}