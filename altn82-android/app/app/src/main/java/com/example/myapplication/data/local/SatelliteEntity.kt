package com.example.myapplication.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myapplication.models.MissionDTO
import com.example.myapplication.models.Satellite
import com.example.myapplication.models.StatutSatellite
import java.time.LocalDateTime

@Entity(tableName = "satellites")
data class SatelliteEntity(
    @PrimaryKey val idSatellite: Int,
    val nomSatellite: String,
    val statut: StatutSatellite,
    val formatCubesat: String,
    val battery: Int,
    val idOrbite: Int,
    val dateLancement: LocalDateTime?,
    val masse: Double,
    val missions: List<MissionDTO>,
    val cachedAt: LocalDateTime
)

fun Satellite.toEntity(cachedAt: LocalDateTime): SatelliteEntity = SatelliteEntity(
    idSatellite = idSatellite,
    nomSatellite = nomSatellite,
    statut = statut,
    formatCubesat = formatCubesat,
    battery = battery,
    idOrbite = idOrbite,
    dateLancement = dateLancement,
    masse = masse,
    missions = missions,
    cachedAt = cachedAt
)

fun SatelliteEntity.toModel(): Satellite = Satellite(
    idSatellite = idSatellite,
    nomSatellite = nomSatellite,
    statut = statut,
    formatCubesat = formatCubesat,
    idOrbite = idOrbite,
    dateLancement = dateLancement,
    masse = masse,
    battery = battery,
    missions = missions
)