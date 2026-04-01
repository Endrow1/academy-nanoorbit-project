package com.example.myapplication.models

data class StationSol(
    val codeStation: String,
    val nomStation: String,
    val latitude: Double,
    val longitude: Double,
    val diametreAntenne: Double? = null,
    val debitMax: Double? = null
)