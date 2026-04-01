package com.example.myapplication.model

import java.time.LocalDateTime

data class FenetreCom(
    val idFenetre: Int,
    val datetimeDebut: LocalDateTime,
    val duree: Int,
    val statut: String,
    val idSatellite: Int,
    val codeStation: String,
    val volumeDonnees: Double? = null
)