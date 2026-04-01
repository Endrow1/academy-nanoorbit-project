package com.example.myapplication.screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.myapplication.component.FenetreCard
import com.example.myapplication.component.SatelliteCard
import com.example.myapplication.mock.MockData
import com.example.myapplication.model.StatutSatellite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val filteredSatellites = remember(searchQuery) {
        MockData.satellites.filter { sat ->
            val orbite = MockData.orbites.find { it.idOrbite == sat.idOrbite }
            sat.nomSatellite.contains(searchQuery, ignoreCase = true) ||
                    orbite?.typeOrbite?.contains(searchQuery, ignoreCase = true) == true
        }
    }

    val totalSats = MockData.satellites.size
    val operationalSats = MockData.satellites.count { it.statut == StatutSatellite.OPERATIONNEL }

    Column(modifier = modifier.fillMaxSize()) {

        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("Rechercher par nom ou type d'orbite") },
            singleLine = true
        )

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
                Text(
                    "Chargement des données télémétriques...",
                    modifier = Modifier.padding(top = 80.dp)
                )
            }
        } else {
            /*
                Q1
                On utilise LazyColumn plutôt que Column car LazyColumn n’affiche
                que les éléments visibles à l’écran, plus une petite marge autour. C’est donc
                adapté aux longues listes.
                Avec Column, si on a 100 satellites, tous les composants seraient créés d’un coup.
                Cela consommerait plus de mémoire, ralentirait
                le rendu initial et rendrait le scroll moins fluide. LazyColumn améliore
                les performances pour les listes.
            */
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                item {
                    Text(
                        text = "$operationalSats/$totalSats satellites opérationnels",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                items(filteredSatellites) { satellite ->
                    SatelliteCard(
                        satellite = satellite,
                        onClick = {
                            Toast.makeText(
                                context,
                                "Satellite sélectionné : ${satellite.nomSatellite}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }

                if (filteredSatellites.isNotEmpty() && searchQuery.isEmpty()) {
                    item {
                        HorizontalDivider(modifier = Modifier.padding(16.dp))
                        Text(
                            "Dernières communications",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    items(MockData.fenetresCom) { fenetre ->
                        val station =
                            MockData.stations.find { it.codeStation == fenetre.codeStation }
                        FenetreCard(
                            fenetre = fenetre,
                            nomStation = station?.nomStation ?: fenetre.codeStation
                        )
                    }
                }

                if (filteredSatellites.isEmpty()) {
                    item {
                        Text(
                            "Aucun satellite trouvé.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}