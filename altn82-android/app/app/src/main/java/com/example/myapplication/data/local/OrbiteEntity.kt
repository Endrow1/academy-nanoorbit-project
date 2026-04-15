package com.example.myapplication.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myapplication.models.Orbite
import java.time.LocalDateTime

@Entity(tableName = "orbites")
data class OrbiteEntity(
    @PrimaryKey val idOrbite: Int,
    val typeOrbite: String,
    val altitude: Int,
    val inclinaison: Double,
    val zoneCouverture: String?,
    val cachedAt: LocalDateTime
)

fun Orbite.toEntity(cachedAt: LocalDateTime): OrbiteEntity = OrbiteEntity(
    idOrbite = idOrbite,
    typeOrbite = typeOrbite,
    altitude = altitude,
    inclinaison = inclinaison,
    zoneCouverture = zoneCouverture,
    cachedAt = cachedAt
)

fun OrbiteEntity.toModel(): Orbite = Orbite(
    idOrbite = idOrbite,
    typeOrbite = typeOrbite,
    altitude = altitude,
    inclinaison = inclinaison,
    zoneCouverture = zoneCouverture
)