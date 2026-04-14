package com.example.myapplication.mock

import com.example.myapplication.models.FenetreCom
import com.example.myapplication.models.Instrument
import com.example.myapplication.models.Mission
import com.example.myapplication.models.MissionDTO
import com.example.myapplication.models.Orbite
import com.example.myapplication.models.Satellite
import com.example.myapplication.models.StationSol
import com.example.myapplication.models.StatutFenetre
import com.example.myapplication.models.StatutInstrument
import com.example.myapplication.models.StatutSatellite
import java.time.LocalDateTime

object MockData {

    val orbites = listOf(
        Orbite(1, "SSO", 800, 98.6, "Globale"),
        Orbite(2, "SSO", 600, 97.8, "Polaire"),
        Orbite(3, "LEO", 400, 51.6, "Zone Tempérée")
    )

    val satellites = listOf(
        Satellite(
            idSatellite = 1,
            nomSatellite = "SAT-001",
            statut = StatutSatellite.OPERATIONNEL,
            formatCubesat = "6U",
            idOrbite = 1,
            dateLancement = LocalDateTime.of(2023, 5, 12, 10, 0),
            masse = 12.5,
            battery = 85,
            missions = listOf(
                MissionDTO(missionId = 1, role = "Support Télécom"),
                MissionDTO(missionId = 2, role = "Imagerie Haute Résolution")
            )
        ),
        Satellite(
            idSatellite = 2,
            nomSatellite = "SAT-002",
            statut = StatutSatellite.OPERATIONNEL,
            formatCubesat = "12U",
            idOrbite = 1,
            dateLancement = LocalDateTime.of(2023, 8, 20, 14, 30),
            masse = 22.0,
            battery = 92,
            missions = listOf(
                MissionDTO(missionId = 3, role = "Analyse Spectrale")
            )
        ),
        Satellite(
            idSatellite = 3,
            nomSatellite = "SAT-003",
            statut = StatutSatellite.EN_VEILLE,
            formatCubesat = "3U",
            idOrbite = 2,
            dateLancement = LocalDateTime.of(2024, 1, 15, 0, 0),
            masse = 4.2,
            battery = 45,
            missions = emptyList() // Aucune mission pour celui-ci
        ),
        Satellite(
            idSatellite = 4,
            nomSatellite = "SAT-004",
            statut = StatutSatellite.DEFAILLANT,
            formatCubesat = "6U",
            idOrbite = 3,
            dateLancement = LocalDateTime.of(2022, 11, 3, 9, 15),
            masse = 11.8,
            battery = 8,
            missions = listOf(
                MissionDTO(missionId = 2, role = "Observateur de secours")
            )
        ),
        Satellite(
            idSatellite = 5,
            nomSatellite = "SAT-005",
            statut = StatutSatellite.DESORBITE,
            formatCubesat = "3U",
            idOrbite = 3,
            dateLancement = LocalDateTime.of(2020, 6, 10, 11, 0),
            masse = 3.9,
            battery = 0,
            missions = emptyList()
        )
    )

    val instruments = listOf(
        Instrument("INS-MSI-01", "Imagerie", "MSI-Gen2", "10m", 45.5, 1, StatutInstrument.NOMINAL),
        Instrument("INS-SAR-01", "Radar", "SAR-Light", "5m", 120.0, 1, StatutInstrument.DEGRADE),
        Instrument("INS-HYP-01", "Hyperspectral", "Hyper-X", "30m", 35.0, 1, StatutInstrument.HS),
        Instrument(
            "INS-MAG-01",
            "Magnétomètre",
            "Mag-Alpha",
            null,
            5.0,
            1,
            StatutInstrument.NOMINAL
        )
    )

    val stations = listOf(
        StationSol("KRN", "Kiruna", 67.855, 20.225, 15.0, 500.0),
        StationSol("VNC", "Vannes", 47.658, -2.759, 3.5, 100.0),
        StationSol("HBK", "Hartebeesthoek", -25.890, 27.707, 12.0, 450.0)
    )

    val fenetresCom = listOf(
        FenetreCom(
            101,
            LocalDateTime.now().minusDays(1),
            600,
            StatutFenetre.REALISEE,
            1,
            "KRN",
            450.5
        ),
        FenetreCom(
            102,
            LocalDateTime.now().minusHours(5),
            480,
            StatutFenetre.REALISEE,
            2,
            "VNC",
            210.0
        ),
        FenetreCom(
            103,
            LocalDateTime.now().minusHours(2),
            540,
            StatutFenetre.REALISEE,
            1,
            "HBK",
            380.2
        ),
        FenetreCom(
            104,
            LocalDateTime.now().plusHours(4),
            600,
            StatutFenetre.PLANIFIEE,
            3,
            "KRN",
            null
        ),
        FenetreCom(
            105,
            LocalDateTime.now().plusDays(1),
            300,
            StatutFenetre.PLANIFIEE,
            2,
            "VNC",
            null
        )
    )

    val missions = listOf(
        Mission(
            1,
            "Sentinel-ALTN",
            "Surveillance environnementale",
            LocalDateTime.of(2023, 1, 1, 0, 0),
            "ACTIVE"
        )
    )
}