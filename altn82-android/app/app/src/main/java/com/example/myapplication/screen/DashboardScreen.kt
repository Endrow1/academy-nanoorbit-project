package com.example.myapplication.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.component.SatelliteCard
import com.example.myapplication.models.StatutSatellite
import com.example.myapplication.viewmodel.NanoOrbitViewModel

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier, viewModel: NanoOrbitViewModel, onSatelliteClick: (Int) -> Unit
) {
    val satellites by viewModel.filteredSatellites.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedStatut by viewModel.selectedStatut.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val cacheInfo by viewModel.cacheInfo.collectAsStateWithLifecycle()
    val orbites by viewModel.orbites.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {

        if (cacheInfo.isOfflineMode) {
            Text(
                text = "Mode hors-ligne — ${viewModel.getCacheAgeText()}",
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(12.dp),
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium
            )
        } else if (cacheInfo.lastUpdatedAt != null) {
            Text(
                text = viewModel.getCacheAgeText(),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(12.dp),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                style = MaterialTheme.typography.bodyMedium
            )
        }


        TextField(
            value = searchQuery,
            onValueChange = { viewModel.onSearchQueryChange(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("Rechercher...") },
            singleLine = true
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedStatut == null,
                onClick = { viewModel.onStatutFilterChange(null) },
                label = { Text("Tous") })

            StatutSatellite.entries.forEach { statut ->
                FilterChip(
                    selected = selectedStatut == statut,
                    onClick = { viewModel.onStatutFilterChange(statut) },
                    label = { Text(statut.getLabel()) })
            }
        }

        val sattelitesOp = satellites.filter { it.statut == StatutSatellite.OPERATIONNEL }
        Text(
            text = "${sattelitesOp.size}/${satellites.size} opérationnel",
            modifier = Modifier.padding(16.dp)
        )

        Box(modifier = Modifier.weight(1f)) {

            when {
                isLoading && satellites.isEmpty() -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                errorMessage != null && satellites.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(errorMessage!!, textAlign = TextAlign.Center)
                        Button(onClick = { viewModel.refreshSatellites() }) {
                            Text("Réessayer")
                        }
                    }
                }

                else -> {
                    LazyColumn {
                        items(satellites) { satellite ->
                            val typeOrbite =
                                orbites.find { it.idOrbite == satellite.idOrbite }?.typeOrbite
                                    ?: "Orbite inconnue"

                            SatelliteCard(
                                satellite = satellite,
                                typeOrbite = typeOrbite,
                                onClick = { onSatelliteClick(satellite.idSatellite) })
                        }
                    }
                }
            }
        }
    }
}