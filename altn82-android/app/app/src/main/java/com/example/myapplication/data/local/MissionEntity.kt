package com.example.myapplication.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myapplication.models.Mission
import java.time.LocalDateTime

@Entity(tableName = "missions")
data class MissionEntity(
    @PrimaryKey val idMission: Int,
    val nomMission: String,
    val objectif: String,
    val dateDebut: LocalDateTime,
    val statutMission: String,
    val dateFin: LocalDateTime?,
    val zoneGeoCible: String?,
    val cachedAt: LocalDateTime
)

fun Mission.toEntity(cachedAt: LocalDateTime): MissionEntity = MissionEntity(
    idMission = idMission,
    nomMission = nomMission,
    objectif = objectif,
    dateDebut = dateDebut,
    statutMission = statutMission,
    dateFin = dateFin,
    zoneGeoCible = zoneGeoCible,
    cachedAt = cachedAt
)

fun MissionEntity.toModel(): Mission = Mission(
    idMission = idMission,
    nomMission = nomMission,
    objectif = objectif,
    dateDebut = dateDebut,
    statutMission = statutMission,
    dateFin = dateFin,
    zoneGeoCible = zoneGeoCible
)