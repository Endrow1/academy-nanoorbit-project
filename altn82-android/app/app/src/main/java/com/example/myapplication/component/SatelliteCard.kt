package com.example.myapplication.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.example.myapplication.mock.MockData
import com.example.myapplication.model.Satellite
import com.example.myapplication.model.StatutSatellite
@Composable
fun SatelliteCard(satellite: Satellite, onClick: () -> Unit) {
    val isDesorbite = satellite.statut == StatutSatellite.DESORBITE
    val orbiteAssociee = MockData.orbites.find { it.idOrbite == satellite.idOrbite }
    val nomAffiche = orbiteAssociee?.typeOrbite ?: "Inconnue"

    /*
    Q3
    L’application peut empêcher la planification d’une fenêtre en
    bloquant l’action dès l’interface :griser la carte et afficher un message explicite
    C’est une validation préventive pour guider l’utilisateur et
    éviter une action invalide.
    */

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .alpha(if (isDesorbite) 0.6f else 1f),
        onClick = { if (!isDesorbite) onClick() },
        enabled = !isDesorbite
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusBadge(statut = satellite.statut)

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = satellite.nomSatellite, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Format ${satellite.formatCubesat} - Orbite ${nomAffiche}",
                    style = MaterialTheme.typography.bodySmall
                )

                if (isDesorbite) {
                    Text(
                        text = "ACTION INDISPONIBLE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}