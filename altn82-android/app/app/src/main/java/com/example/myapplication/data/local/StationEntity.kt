package com.example.myapplication.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myapplication.models.EtatStation
import com.example.myapplication.models.StationSol
import java.time.LocalDateTime

@Entity(tableName = "stations")
data class StationEntity(
    @PrimaryKey val codeStation: String,
    val nomStation: String,
    val latitude: Double,
    val longitude: Double,
    val diametreAntenne: Double?,
    val debitMax: Double?,
    val bandeFrequence: String,
    val etatStation: EtatStation,
    val cachedAt: LocalDateTime
)

fun StationSol.toEntity(cachedAt: LocalDateTime): StationEntity = StationEntity(
    codeStation = codeStation,
    nomStation = nomStation,
    latitude = latitude,
    longitude = longitude,
    diametreAntenne = diametreAntenne,
    debitMax = debitMax,
    bandeFrequence = bandeFrequence,
    etatStation = etatStation,
    cachedAt = cachedAt
)

fun StationEntity.toModel(): StationSol = StationSol(
    codeStation = codeStation,
    nomStation = nomStation,
    latitude = latitude,
    longitude = longitude,
    diametreAntenne = diametreAntenne,
    debitMax = debitMax,
    bandeFrequence = bandeFrequence,
    etatStation = etatStation
)