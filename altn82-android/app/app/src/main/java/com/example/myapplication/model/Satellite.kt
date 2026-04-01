package com.example.myapplication.model

import java.time.LocalDateTime

data class Satellite(
    val idSatellite: Int,
    val nomSatellite: String,
    val statut: StatutSatellite,
    val formatCubesat: String,
    val idOrbite: Int,
    val dateLancement: LocalDateTime? = null,
    val masse: Double? = null
)