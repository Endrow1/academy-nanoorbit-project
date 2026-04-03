package com.example.myapplication.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.mock.MockData
import com.example.myapplication.models.Satellite
import com.example.myapplication.models.StatutSatellite

@Composable
fun SatelliteCard(satellite: Satellite, onClick: () -> Unit) {
    val isDesorbite = satellite.statut == StatutSatellite.DESORBITE
    val orbiteAssociee = MockData.orbites.find { it.idOrbite == satellite.idOrbite }
    val nomAffiche = orbiteAssociee?.typeOrbite ?: "Orbite Inconnue"

    /*
    Q3
    griser la carte et afficher un message explicite.
    C’est une validation préventive pour guider l’utilisateur et
    éviter une action invalide.
    */

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .alpha(if (isDesorbite) 0.6f else 1f),
        onClick = onClick,
        enabled = !isDesorbite,
        colors = CardDefaults.cardColors(
            containerColor = if (isDesorbite) MaterialTheme.colorScheme.surfaceVariant
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = satellite.nomSatellite,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isDesorbite) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "${satellite.formatCubesat} - $nomAffiche",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (isDesorbite) {
                    Text(
                        text = "INDISPONIBLE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Box(
                modifier = Modifier.padding(start = 8.dp)
            ) {
                StatusBadge(statut = satellite.statut)
            }
        }
    }
}