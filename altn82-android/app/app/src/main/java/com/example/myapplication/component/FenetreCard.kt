package com.example.myapplication.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.model.FenetreCom

@Composable
fun FenetreCard(fenetre: FenetreCom, nomStation: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = nomStation, style = MaterialTheme.typography.labelLarge)
                Text(
                    text = fenetre.statut,
                    color = if (fenetre.statut == "REALISEE") Color(0xFF4CAF50) else Color(
                        0xFF2196F3
                    )
                )
            }
            Text(text = "Durée : ${fenetre.duree} sec", style = MaterialTheme.typography.bodyMedium)

            fenetre.volumeDonnees?.let {
                Text(
                    text = "Volume : $it GB",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}