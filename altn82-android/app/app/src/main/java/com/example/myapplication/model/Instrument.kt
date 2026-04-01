package com.example.myapplication.model;

data class Instrument(
        val refInstrument: String,
        val typeInstrument: String,
        val modele: String,
        val resolution: String? = null,
        val consommation: Double? = null
)
