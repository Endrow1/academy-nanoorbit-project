package com.example.myapplication.mock

import com.example.myapplication.models.FenetreCom
import com.example.myapplication.models.Instrument
import com.example.myapplication.models.Mission
import com.example.myapplication.models.Orbite
import com.example.myapplication.models.Satellite
import com.example.myapplication.models.StationSol
import com.example.myapplication.models.StatutFenetre
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
            1,
            "SAT-001",
            StatutSatellite.OPERATIONNEL,
            "6U",
            1,
            LocalDateTime.of(2023, 5, 12, 10, 0),
            12.5
        ), Satellite(
            2,
            "SAT-002",
            StatutSatellite.OPERATIONNEL,
            "12U",
            1,
            LocalDateTime.of(2023, 8, 20, 14, 30),
            22.0
        ), Satellite(
            3,
            "SAT-003",
            StatutSatellite.EN_VEILLE,
            "3U",
            2,
            LocalDateTime.of(2024, 1, 15, 0, 0),
            4.2
        ), Satellite(
            4,
            "SAT-004",
            StatutSatellite.DEFAILLANT,
            "6U",
            3,
            LocalDateTime.of(2022, 11, 3, 9, 15),
            11.8
        ), Satellite(
            5,
            "SAT-005",
            StatutSatellite.DESORBITE,
            "3U",
            3,
            LocalDateTime.of(2020, 6, 10, 11, 0),
            3.9
        )
    )

    val instruments = listOf(
        Instrument("INS-MSI-01", "Imagerie", "MSI-Gen2", "10m", 45.5),
        Instrument("INS-SAR-01", "Radar", "SAR-Light", "5m", 120.0),
        Instrument("INS-HYP-01", "Hyperspectral", "Hyper-X", "30m", 35.0),
        Instrument("INS-MAG-01", "Magnétomètre", "Mag-Alpha", null, 5.0)
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