package com.example.myapplication.models

data class Orbite(
    val idOrbite: Int,
    val typeOrbite: String,
    val altitude: Int,
    val inclinaison: Double,
    val zoneCouverture: String? = null
)