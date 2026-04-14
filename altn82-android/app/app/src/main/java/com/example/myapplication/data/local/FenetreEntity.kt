package com.example.myapplication.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myapplication.models.FenetreCom
import com.example.myapplication.models.StatutFenetre
import java.time.LocalDateTime

@Entity(tableName = "fenetres_com")
data class FenetreEntity(
    @PrimaryKey val idFenetre: Int,
    val datetimeDebut: LocalDateTime,
    val duree: Int,
    val statut: StatutFenetre,
    val idSatellite: Int,
    val codeStation: String,
    val volumeDonnees: Double?,
    val cachedAt: LocalDateTime
)

fun FenetreEntity.toModel(): FenetreCom = FenetreCom(
    idFenetre = idFenetre,
    datetimeDebut = datetimeDebut,
    duree = duree,
    statut = statut,
    idSatellite = idSatellite,
    codeStation = codeStation,
    volumeDonnees = volumeDonnees
)

fun FenetreCom.toEntity(cachedAt: LocalDateTime): FenetreEntity = FenetreEntity(
    idFenetre = idFenetre,
    datetimeDebut = datetimeDebut,
    duree = duree,
    statut = statut,
    idSatellite = idSatellite,
    codeStation = codeStation,
    volumeDonnees = volumeDonnees,
    cachedAt = cachedAt
)