package com.example.myapplication.models

import java.time.LocalDateTime

enum class StatutSatellite {
    /*
    Q2
    Une enum est préférable à une String car elle limite les
    valeurs possibles aux seuls statuts autorisés. Cela évite les fautes de frappe
    qui casseraient la logique d’affichage des couleurs
    Avec une enum, on gagne :
    la sécurité de typage
    l’autocomplétion
    des tests plus fiables
    */
    OPERATIONNEL, EN_VEILLE, DEFAILLANT, DESORBITE;

    val color: String
        get() = when (this) {
            OPERATIONNEL -> "FF4CAF50"
            EN_VEILLE -> "FFFF9800"
            DEFAILLANT -> "FFF44336"
            DESORBITE -> "FF888888"
        }

    fun getLabel(): String = name.lowercase().replaceFirstChar { it.uppercase() }.replace("_", " ")
}

enum class StatutFenetre {
    REALISEE,
    PLANIFIEE;

    val color: String
        get() = when (this) {
            REALISEE -> "FF4CAF50"
            PLANIFIEE -> "FF2196F3"
        }
}

data class Satellite(
    val idSatellite: Int,
    val nomSatellite: String,
    val statut: StatutSatellite,
    val formatCubesat: String,
    val idOrbite: Int,
    val dateLancement: LocalDateTime? = null,
    val masse: Double? = null
)

data class FenetreCom(
    val idFenetre: Int,
    val datetimeDebut: LocalDateTime,
    val duree: Int,
    val statut: StatutFenetre,
    val idSatellite: Int,
    val codeStation: String,
    val volumeDonnees: Double? = null
)

data class Orbite(
    val idOrbite: Int,
    val typeOrbite: String,
    val altitude: Int,
    val inclinaison: Double,
    val zoneCouverture: String? = null
)

data class Mission(
    val idMission: Int,
    val nomMission: String,
    val objectif: String,
    val dateDebut: LocalDateTime,
    val statutMission: String,
    val dateFin: LocalDateTime? = null,
    val zoneGeoCible: String? = null
)

data class StationSol(
    val codeStation: String,
    val nomStation: String,
    val latitude: Double,
    val longitude: Double,
    val diametreAntenne: Double? = null,
    val debitMax: Double? = null
)

data class Instrument(
    val refInstrument: String,
    val typeInstrument: String,
    val modele: String,
    val resolution: String? = null,
    val consommation: Double? = null
)
