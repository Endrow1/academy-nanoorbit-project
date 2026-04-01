package com.example.myapplication.model

import java.time.LocalDateTime

data class Mission(
    val idMission: Int,
    val nomMission: String,
    val objectif: String,
    val dateDebut: LocalDateTime,
    val statutMission: String,
    val dateFin: LocalDateTime? = null,
    val zoneGeoCible: String? = null
)