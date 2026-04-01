package com.example.myapplication.model

data class Orbite(
    val idOrbite: Int,
    val typeOrbite: String,
    val altitude: Int,
    val inclinaison: Double,
    val zoneCouverture: String? = null
)