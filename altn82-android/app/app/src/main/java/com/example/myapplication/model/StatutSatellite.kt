package com.example.myapplication.model;

import androidx.compose.ui.graphics.Color;

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

    OPERATIONNEL,
    EN_VEILLE,
    DEFAILLANT,
    DESORBITE;

    val color: Color
        get() = when (this) {
            OPERATIONNEL -> Color(0xFF4CAF50)
            EN_VEILLE -> Color(0xFFFF9800)
            DEFAILLANT -> Color(0xFFF44336)
            DESORBITE -> Color.Gray
        }

    fun getLabel(): String = name.lowercase().replaceFirstChar { it.uppercase() }.replace("_", " ")
}